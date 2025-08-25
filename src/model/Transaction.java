package model;

import java.time.LocalDateTime;

public record Transaction(String fromAccount, String toAccount, double amount, String filename,
                          LocalDateTime timestamp) {
}