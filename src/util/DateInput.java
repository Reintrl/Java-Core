package util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class DateInput {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String getDateStringFromUser(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt + " (формат: yyyy-MM-dd): ");
            String input = scanner.nextLine().trim();

            try {
                LocalDate.parse(input, formatter);
                return input;
            } catch (DateTimeParseException e) {
                System.out.println("Неверный формат даты. Пример: 2024-01-15");
            }
        }
    }
}