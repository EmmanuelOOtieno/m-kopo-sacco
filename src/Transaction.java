import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final LocalDateTime timestamp;
    private final String type;
    private final double amount;
    private final double balanceAfter;

    public Transaction(String type, double amount, double balanceAfter) {
        this.timestamp = LocalDateTime.now();
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public double getBalanceAfter() { return balanceAfter; }

    @Override
    public String toString() {
        return String.format("[%s] %s: KSh %.2f | New Balance: KSh %.2f",
                timestamp.format(FORMATTER), type, amount, balanceAfter);
    }
}