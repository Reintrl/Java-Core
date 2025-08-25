package service;

import model.OperationResult;
import model.Transaction;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class ReportService {
    private final File reportFile;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ReportService(String reportFilePath) {
        this.reportFile = new File(reportFilePath);
    }

    public void saveOperationResults(List<OperationResult> results) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(reportFile, true))) {
            for (OperationResult result : results) {
                writer.println(formatOperationResult(result));
            }
        } catch (IOException e) {
            System.out.println("Ошибка при записи в отчет: " + e.getMessage());
        }
    }

    private String formatOperationResult(OperationResult result) {
        Transaction transaction = result.transaction();
        return transaction.timestamp().format(formatter) + " | " +
                result.filename() + " | " + "перевод с " +
                transaction.fromAccount() + " на " +
                transaction.toAccount() + transaction.amount() +
                " | " + result.status() + " | " + result.message();
    }

    public void showAllTransactions() {
        if (!reportFile.exists()) {
            System.out.println("Файл отчета не найден.");
            return;
        }

        System.out.println("\n--- Все операции из отчета ---");
        try (BufferedReader br = new BufferedReader(new FileReader(reportFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("Ошибка при чтении из отчета: " + e.getMessage());
        }
        System.out.println("-----------------------------");
    }

    public void showTransactionsByDate(String startDateStr, String endDateStr) {
        if (!reportFile.exists()) {
            System.out.println("Файл отчета не найден.");
            return;
        }

        try {
            LocalDateTime startDate = parseDate(startDateStr + " 00:00:00");
            LocalDateTime endDate = parseDate(endDateStr + " 23:59:59");

            System.out.println("\n--- Операции за период с " + startDateStr + " по " + endDateStr + " ---");

            List<String> filteredTransactions = filterTransactionsByDate(startDate, endDate);

            if (filteredTransactions.isEmpty()) {
                System.out.println("Операций за указанный период не найдено.");
            } else {
                for (String transaction : filteredTransactions) {
                    System.out.println(transaction);
                }
                System.out.println("Найдено операций: " + filteredTransactions.size());
            }

        } catch (DateTimeParseException e) {
            System.out.println("Неверный формат даты. Используйте формат: yyyy-MM-dd");
        }

        System.out.println("-----------------------------------------------------");
    }

    private List<String> filterTransactionsByDate(LocalDateTime startDate, LocalDateTime endDate) {
        List<String> filteredTransactions = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(reportFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (isLineInDateRange(line, startDate, endDate)) {
                    filteredTransactions.add(line);
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка при чтении отчета: " + e.getMessage());
        }

        return filteredTransactions;
    }

    private boolean isLineInDateRange(String line, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            String dateTimeStr = line.substring(0, 19);
            LocalDateTime transactionDate = parseDate(dateTimeStr);

            return !transactionDate.isBefore(startDate) && !transactionDate.isAfter(endDate);

        } catch (Exception e) {
            System.out.println("Ошибка при парсинге даты в строке: " + line);
            return false;
        }
    }

    private LocalDateTime parseDate(String dateTimeStr) throws DateTimeParseException {
        return LocalDateTime.parse(dateTimeStr, formatter);
    }
}