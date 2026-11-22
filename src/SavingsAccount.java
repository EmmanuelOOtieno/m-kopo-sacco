public class SavingsAccount extends Account {
    private static final double INTEREST_RATE = 0.05; // 5% interest

    public SavingsAccount(String accountNumber, double initialBalance) {
        super(accountNumber, initialBalance);
    }

    @Override
    public double calculateInterest() {
        return getBalance() * INTEREST_RATE;
    }
}