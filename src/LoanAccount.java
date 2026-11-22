public class LoanAccount extends Account {
    private static final double INTEREST_RATE = 0.12; // 12% interest

    public LoanAccount(String accountNumber, double initialBalance) {
        super(accountNumber, initialBalance);
    }

    @Override
    public double calculateInterest() {
        return getBalance() * INTEREST_RATE;
    }
}