package service;

import model.OperationResult;
import model.Transaction;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileParserService {
    private static final Pattern FROM_PATTERN = Pattern.compile("from:\\s*(\\d{5}-\\d{5}|\\S+)");
    private static final Pattern TO_PATTERN = Pattern.compile("to:\\s*(\\d{5}-\\d{5}|\\S+)");
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("amount:\\s*(.+)");
    private static final Pattern VALID_ACCOUNT_PATTERN = Pattern.compile("\\d{5}-\\d{5}");

    private final File inputFolder;
    private final File archiveFolder;
    private final AccountService accountService;
    private final ReportService reportService;

    public FileParserService(String inputPath, String archivePath, String accountsPath, String reportPath) {
        this.inputFolder = new File(inputPath);
        this.archiveFolder = new File(archivePath);
        this.accountService = new AccountService(accountsPath);
        this.reportService = new ReportService(reportPath);

        createDirectoriesIfNotExist();
    }

    private void createDirectoriesIfNotExist() {
        if (!inputFolder.exists()) inputFolder.mkdirs();
        if (!archiveFolder.exists()) archiveFolder.mkdirs();
    }

    public void parseAndProcessFiles() {
        File[] files = getTxtFiles();

        if (files.length == 0) {
            System.out.println("В директории input нет txt файлов для обработки");
            return;
        }

        List<OperationResult> allResults = new ArrayList<>();

        for (File file : files) {
            try {
                List<OperationResult> fileResults = processFile(file);
                allResults.addAll(fileResults);
                moveFileToArchive(file);
            } catch (IOException e) {
                System.out.println("Ошибка при обработке файла " + file.getName() + ": " + e.getMessage());
            }
        }

        saveResults(allResults);
    }

    private File[] getTxtFiles() {
        return inputFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
    }

    private List<OperationResult> processFile(File file) throws IOException {
        List<OperationResult> results = new ArrayList<>();
        List<String> lines = Files.readAllLines(file.toPath());
        LocalDateTime fileTime = LocalDateTime.now();

        TransactionData currentData = new TransactionData();

        for (String line : lines) {
            line = line.trim();

            parseLine(line, currentData);

            if (line.isEmpty()) {
                processCompletedTransaction(currentData, file.getName(), fileTime, results);
                currentData = new TransactionData();
            }
        }

        processCompletedTransaction(currentData, file.getName(), fileTime, results);

        return results;
    }

    private void parseLine(String line, TransactionData currentData) {
        Matcher fromMatcher = FROM_PATTERN.matcher(line);
        if (fromMatcher.find()) {
            currentData.fromAccount = fromMatcher.group(1);
            return;
        }

        Matcher toMatcher = TO_PATTERN.matcher(line);
        if (toMatcher.find()) {
            currentData.toAccount = toMatcher.group(1);
            return;
        }

        Matcher amountMatcher = AMOUNT_PATTERN.matcher(line);
        if (amountMatcher.find()) {
            currentData.amountStr = amountMatcher.group(1).trim();
        }
    }

    private void processCompletedTransaction(TransactionData data, String filename,
                                             LocalDateTime timestamp, List<OperationResult> results) {
        if (hasTransactionData(data)) {
            OperationResult result = createOperationResult(data, filename, timestamp);
            results.add(result);
        }
    }

    private boolean hasTransactionData(TransactionData data) {
        return data.fromAccount != null || data.toAccount != null || data.amountStr != null;
    }

    private OperationResult createOperationResult(TransactionData data, String filename, LocalDateTime timestamp) {
        String errorMessage = validateTransactionData(data);

        if (errorMessage != null) {
            return createErrorResult(data, filename, timestamp, errorMessage);
        }

        double amount;
        try {
            amount = Double.parseDouble(data.amountStr);
        } catch (NumberFormatException e) {
            return createErrorResult(data, filename, timestamp, "неверный формат суммы: " + data.amountStr);
        }

        Transaction transaction = new Transaction(data.fromAccount, data.toAccount, amount, filename, timestamp);
        return processValidTransaction(transaction, filename);
    }

    private String validateTransactionData(TransactionData data) {
        if (data.fromAccount == null) return "не указан счет отправителя";
        if (data.toAccount == null) return "не указан счет получателя";
        if (data.amountStr == null) return "не указана сумма";
        if (data.amountStr.isEmpty()) return "пустая сумма";

        if (!VALID_ACCOUNT_PATTERN.matcher(data.fromAccount).matches()) {
            return "неверный формат счета отправителя: " + data.fromAccount;
        }
        if (!VALID_ACCOUNT_PATTERN.matcher(data.toAccount).matches()) {
            return "неверный формат счета получателя: " + data.toAccount;
        }
        if (data.fromAccount.equals(data.toAccount)) {
            return "нельзя переводить на тот же счет: " + data.fromAccount;
        }

        return null;
    }

    private OperationResult createErrorResult(TransactionData data, String filename,
                                              LocalDateTime timestamp, String errorMessage) {
        double amount = 0.0;
        try {
            if (data.amountStr != null) {
                amount = Double.parseDouble(data.amountStr);
            }
        } catch (NumberFormatException e) {
            // Оставляем amount = 0.0
        }

        Transaction transaction = new Transaction(
                data.fromAccount != null ? data.fromAccount : "НЕ_УКАЗАН",
                data.toAccount != null ? data.toAccount : "НЕ_УКАЗАН",
                amount,
                filename,
                timestamp
        );

        return new OperationResult(filename, transaction, OperationResult.Status.ERROR, errorMessage);
    }

    private OperationResult processValidTransaction(Transaction transaction, String filename) {
        try {
            double amount = transaction.amount();
            if (amount <= 0) {
                return new OperationResult(
                        filename, transaction, OperationResult.Status.ERROR,
                        "неверная сумма перевода: " + amount
                );
            }

            boolean success = accountService.transferMoney(
                    transaction.fromAccount(), transaction.toAccount(), amount
            );

            if (success) {
                return new OperationResult(
                        filename, transaction, OperationResult.Status.SUCCESS, "успешно обработан"
                );
            } else {
                return new OperationResult(
                        filename, transaction, OperationResult.Status.ERROR,
                        "недостаточно средств на счете " + transaction.fromAccount()
                );
            }

        } catch (Exception e) {
            return new OperationResult(
                    filename, transaction, OperationResult.Status.ERROR,
                    "ошибка во время обработки: " + e.getMessage()
            );
        }
    }

    private void moveFileToArchive(File file) throws IOException {
        Path source = file.toPath();
        Path target = archiveFolder.toPath().resolve(file.getName());
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    private void saveResults(List<OperationResult> results) {
        reportService.saveOperationResults(results);
        accountService.saveAccounts();
        accountService.printAllAccounts();
        System.out.println("Обработано операций: " + results.size());
    }

    private static class TransactionData {
        String fromAccount;
        String toAccount;
        String amountStr;
    }
}