import java.util.HashMap;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.Scanner;

public class SaccoManager {
    private static final HashMap<String, Member> membersDb = new HashMap<>();
    private static final HashSet<String> registeredNationalIds = new HashSet<>();

    public static void main(String[] args) {
        // Phase 2 Verification Harness (Menu loop added in Phase 3)
        System.out.println("=== Phase 2 Engine Test ===");
        Member m1 = new Member("M-001", "Brian Otieno", "38112233");
        membersDb.put(m1.getMemberId(), m1);
        registeredNationalIds.add(m1.getNationalId());

        m1.addAccount("Savings", new SavingsAccount("M-001-1010", 10000));
        m1.addAccount("Loan", new LoanAccount("M-001-2020", 5000));

        printMemberSummary(m1);
        System.out.println("Duplicate National ID check (38112233): "
                + registeredNationalIds.contains("38112233"));
    }

    // --- Core Business Methods ---

    static void registerMember(Scanner scanner) {
        System.out.print("Enter Member ID (e.g., M-001): ");
        String id = scanner.nextLine().trim().toUpperCase();

        if (id.isEmpty()) {
            System.out.println("Error: Member ID cannot be empty.");
            return;
        }
        if (membersDb.containsKey(id)) {
            System.out.println("Error: Member ID '" + id + "' already exists.");
            return;
        }

        System.out.print("Enter Full Name: ");
        String name = scanner.nextLine().trim().replace(",", " ");
        if (name.isEmpty()) {
            System.out.println("Error: Name cannot be empty.");
            return;
        }

        System.out.print("Enter National ID: ");
        String nationalId = scanner.nextLine().trim();
        if (nationalId.isEmpty()) {
            System.out.println("Error: National ID cannot be empty.");
            return;
        }
        if (registeredNationalIds.contains(nationalId)) {
            System.out.println("Error: A member with National ID '" + nationalId + "' is already registered.");
            return;
        }

        Member newMember = new Member(id, name, nationalId);
        membersDb.put(id, newMember);
        registeredNationalIds.add(nationalId);
        System.out.println("Success: Member " + name + " (" + id + ") registered.");
    }

    static void openAccount(Scanner scanner) {
        System.out.print("Enter Member ID: ");
        String id = scanner.nextLine().trim().toUpperCase();
        Member member = membersDb.get(id);

        if (member == null) {
            System.out.println("Error: Member not found.");
            return;
        }

        System.out.println("1. Savings Account (5% interest)");
        System.out.println("2. Loan Account (12% interest)");
        int typeChoice = readInt(scanner, "Choose account type (1 or 2): ");

        String accType;
        if (typeChoice == 1) {
            accType = "Savings";
        } else if (typeChoice == 2) {
            accType = "Loan";
        } else {
            System.out.println("Error: Invalid account type selection.");
            return;
        }

        if (member.getAccount(accType) != null) {
            System.out.println("Error: Member already holds a " + accType + " account.");
            return;
        }

        String accountNumber = id + "-" + (1000 + (int) (System.currentTimeMillis() % 9000));
        Account account = accType.equals("Savings")
                ? new SavingsAccount(accountNumber, 0.0)
                : new LoanAccount(accountNumber, 0.0);

        member.addAccount(accType, account);
        System.out.println("Success: " + accType + " Account (" + accountNumber + ") opened.");
    }

    static void depositMoney(Scanner scanner) {
        Account account = findAccount(scanner);
        if (account == null) return;

        double amount = readDouble(scanner, "Enter amount to deposit (KSh): ");
        account.deposit(amount);
        System.out.printf("Current Balance: KSh %.2f%n", account.getBalance());
    }

    static void withdrawMoney(Scanner scanner) {
        Account account = findAccount(scanner);
        if (account == null) return;

        double amount = readDouble(scanner, "Enter amount to withdraw (KSh): ");
        try {
            account.withdraw(amount);
            System.out.printf("Withdrawal successful. New Balance: KSh %.2f%n", account.getBalance());
        } catch (InsufficientFundsException e) {
            System.out.println("TRANSACTION FAILED: " + e.getMessage());
        }
    }

    static void viewMemberDetails(Scanner scanner) {
        System.out.print("Enter Member ID: ");
        String id = scanner.nextLine().trim().toUpperCase();
        Member member = membersDb.get(id);

        if (member == null) {
            System.out.println("Error: Member not found.");
            return;
        }
        printMemberSummary(member);
    }

    // --- Helper Methods ---

    private static void printMemberSummary(Member member) {
        System.out.println("\n--- MEMBER DETAILS ---");
        System.out.println("Member ID   : " + member.getMemberId());
        System.out.println("Full Name   : " + member.getName());
        System.out.println("National ID : " + member.getNationalId());

        Map<String, Account> accounts = member.getAccounts();
        if (accounts.isEmpty()) {
            System.out.println("Accounts    : None opened yet.");
        } else {
            for (Map.Entry<String, Account> entry : accounts.entrySet()) {
                Account acc = entry.getValue();
                System.out.printf("  [%s] Acc No: %s | Balance: KSh %.2f | Calculated Interest: KSh %.2f%n",
                        entry.getKey(), acc.getAccountNumber(), acc.getBalance(), acc.calculateInterest());
            }
        }
    }

    private static Account findAccount(Scanner scanner) {
        System.out.print("Enter Member ID: ");
        String id = scanner.nextLine().trim().toUpperCase();
        Member member = membersDb.get(id);

        if (member == null) {
            System.out.println("Error: Member not found.");
            return null;
        }

        System.out.print("Enter account type (Savings/Loan): ");
        String rawType = scanner.nextLine().trim();
        String normalizedType = rawType.equalsIgnoreCase("Savings") ? "Savings"
                : rawType.equalsIgnoreCase("Loan") ? "Loan" : rawType;

        Account account = member.getAccount(normalizedType);
        if (account == null) {
            System.out.println("Error: " + rawType + " account not found for member " + id + ".");
            return null;
        }
        return account;
    }

    static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = scanner.nextInt();
                scanner.nextLine(); // consume trailing newline
                return value;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a whole number.");
                scanner.nextLine(); // clear invalid token
            }
        }
    }

    static double readDouble(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                double value = scanner.nextDouble();
                scanner.nextLine(); // consume trailing newline
                return value;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a numeric amount.");
                scanner.nextLine(); // clear invalid token
            }
        }
    }
}