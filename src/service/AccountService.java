package service;

import model.BankAccount;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class AccountService {
    private final File accountsFile;
    private final Map<String, BankAccount> accounts = new HashMap<>();

    public AccountService(String accountsFilePath) {
        this.accountsFile = new File(accountsFilePath);
        loadAccounts();
    }

    private void loadAccounts() {
        if (!accountsFile.exists()) {
            System.out.println("Файл счетов не найден. Будет создан новый.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(accountsFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 2) {
                    String accountNumber = parts[0].trim();
                    double balance = Double.parseDouble((parts[1].trim()));
                    accounts.put(accountNumber, new BankAccount(accountNumber, balance));
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла счетов: " + e.getMessage());
        }
    }

    public void saveAccounts() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(accountsFile))) {
            for (BankAccount account : accounts.values()) {
                bw.write(account.getAccountNumber() + " | " + account.getBalance());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Ошибка при сохранении счетов: " + e.getMessage());
        }
    }

    public BankAccount getAccount(String accountNumber) {
        return accounts.computeIfAbsent(accountNumber, accNum -> new BankAccount(accNum, 0.0));
    }

    public boolean transferMoney(String fromAccount, String toAccount, double amount) {
        BankAccount from = getAccount(fromAccount);
        BankAccount to = getAccount(toAccount);

        if (from.getBalance() < amount) {
            return false;
        }

        from.setBalance(from.getBalance() - amount);
        to.setBalance(to.getBalance() + amount);

        return true;
    }

    public void printAllAccounts() {
        System.out.println("--- Текущие балансы счетов ---");
        for (BankAccount account : accounts.values()) {
            System.out.println(account);
        }
        System.out.println("-----------------------------");
    }
}
