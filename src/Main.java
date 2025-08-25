import service.FileParserService;
import service.ReportService;
import util.DateInput;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {

    private static final Path BASE_DIR = Paths.get(System.getProperty("user.dir"));
    private static final Path INPUT_DIR = BASE_DIR.resolve("input");
    private static final Path ARCHIVE_DIR = BASE_DIR.resolve("archive");
    private static final Path FILES_DIR = BASE_DIR.resolve("files");
    private static final Path REPORT_FILE = FILES_DIR.resolve("report.txt");
    private static final Path ACCOUNTS_FILE = FILES_DIR.resolve("accounts.txt");

    private static final FileParserService fileParserService = new FileParserService(
            INPUT_DIR.toString(),
            ARCHIVE_DIR.toString(),
            ACCOUNTS_FILE.toString(),
            REPORT_FILE.toString());


    private static final ReportService reportService = new ReportService(REPORT_FILE.toString());
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        int choice = getUserChoice();
        processUserChoice(choice);
        scanner.close();
    }

    private static int getUserChoice() {
        int choice;
        while (true) {
            printMenu();

            if (!scanner.hasNextInt()) {
                System.out.println("Ошибка. Введите число!");
                scanner.nextLine();
                continue;
            }

            choice = scanner.nextInt();
            scanner.nextLine();

            if (choice >= 1 && choice <= 3) {
                break;
            } else {
                System.out.println("Неправильный ввод, повторите попытку");
            }
        }
        return choice;
    }

    private static void printMenu() {
        System.out.print("""
                1 - вызов операции парсинга файлов перевода из input
                2 - вызов операции вывода списка всех переводов из файла-отчета
                3 - вызов операции вывода списка переводов из файла-отчета по датам с... по...
                Ожидание ввода:\s""");
    }

    private static void processUserChoice(int choice) {
        switch (choice) {
            case 1 -> parseFiles();
            case 2 -> reportService.showAllTransactions();
            case 3 -> showTransactionsByDate();
            default -> System.out.println("Введен неизвестный вариант");
        }
    }

    private static void showTransactionsByDate() {
        System.out.println("\n--- фильтрация операций по датам ---");

        String startDate = DateInput.getDateStringFromUser(scanner, "Введите начальную дату");
        String endDate = DateInput.getDateStringFromUser(scanner, "Введите конечную дату");

        reportService.showTransactionsByDate(startDate, endDate);
    }

    private static void parseFiles() {
        fileParserService.parseAndProcessFiles();
    }
}
