import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public abstract class Account implements Auditable {
    private String accountNumber;
    private double balance;
    private ArrayList<Transaction> transactionHistory;

    public Account(String accountNumber, double initialBalance) {
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative.");
        }
        this.accountNumber = accountNumber;
        this.balance = initialBalance;
        this.transactionHistory = new ArrayList<>();
    }

    public String getAccountNumber() { return accountNumber; }
    public double getBalance() { return balance; }
    public ArrayList<Transaction> getTransactionHistory() { return transactionHistory; }

    public void deposit(double amount) {
        if (amount <= 0) {
            System.out.println("Error: Deposit amount must be greater than zero.");
            return;
        }
        balance += amount;
        Transaction tx = new Transaction("DEPOSIT", amount, balance);
        transactionHistory.add(tx);
        logTransaction(tx.toString());
    }

    public void withdraw(double amount) throws InsufficientFundsException {
        if (amount <= 0) {
            System.out.println("Error: Withdrawal amount must be greater than zero.");
            return;
        }
        if (amount > balance) {
            throw new InsufficientFundsException(
                    String.format("Insufficient funds. Available balance: KSh %.2f", balance));
        }
        balance -= amount;
        Transaction tx = new Transaction("WITHDRAWAL", amount, balance);
        transactionHistory.add(tx);
        logTransaction(tx.toString());
    }

    public abstract double calculateInterest();

    @Override
    public void logTransaction(String message) {
        try (FileWriter fw = new FileWriter("transactions.txt", true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(accountNumber + " | " + message);
        } catch (IOException e) {
            System.out.println("Error writing to transactions.txt: " + e.getMessage());
        }
    }
}