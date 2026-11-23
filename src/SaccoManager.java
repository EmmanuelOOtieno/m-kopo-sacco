import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.Map;
import java.util.Scanner;

public class SaccoManager {
    private static final HashMap<String, Member> membersDb = new HashMap<>();
    private static final HashSet<String> registeredNationalIds = new HashSet<>();
    private static final String MEMBERS_FILE = "members.csv";
    private static final String TRANSACTIONS_FILE = "transactions.txt";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Load persisted member & account records on startup
        loadDataFromFile();

        int choice;
        do {
            System.out.println("\n=== M-KOPO SACCO MANAGEMENT SYSTEM ===");
            System.out.println("1. Register New Member");
            System.out.println("2. Open an Account");
            System.out.println("3. Deposit Money");
            System.out.println("4. Withdraw Money");
            System.out.println("5. View Member Details");
            System.out.println("6. Exit & Save");

            choice = readInt(scanner, "Enter choice (1-6): ");

            switch (choice) {
                case 1:
                    registerMember(scanner);
                    break;
                case 2:
                    openAccount(scanner);
                    break;
                case 3:
                    depositMoney(scanner);
                    break;
                case 4:
                    withdrawMoney(scanner);
                    break;
                case 5:
                    viewMemberDetails(scanner);
                    break;
                case 6:
                    saveDataToFile();
                    System.out.println("Exiting M-Kopo SACCO System. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please select an option between 1 and 6.");
            }
        } while (choice != 6);

        scanner.close();
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
        logSystemEvent("NEW MEMBER REGISTERED | ID: " + id + " | Name: " + name);
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
        logSystemEvent("ACCOUNT OPENED | Member: " + id + " | Type: " + accType + " | AccNo: " + accountNumber);
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

    // --- File I/O Methods ---

    private static void loadDataFromFile() {
        File file = new File(MEMBERS_FILE);
        if (!file.exists()) {
            System.out.println("No existing " + MEMBERS_FILE + " found. Starting fresh.");
            return;
        }

        try (Scanner fileScanner = new Scanner(file)) {
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine().trim();
                if (line.isEmpty()) continue;

                // CSV Format: MemberId,Name,NationalId,AccountType,AccountNumber,Balance
                String[] parts = line.split(",");
                if (parts.length == 6) {
                    String memberId = parts[0];
                    String name = parts[1];
                    String nationalId = parts[2];
                    String accType = parts[3];
                    String accNumber = parts[4];
                    double balance = Double.parseDouble(parts[5]);

                    Member member = membersDb.getOrDefault(memberId, new Member(memberId, name, nationalId));
                    membersDb.put(memberId, member);
                    registeredNationalIds.add(nationalId);

                    if (!accType.equals("NONE")) {
                        Account account = accType.equals("Savings")
                                ? new SavingsAccount(accNumber, balance)
                                : new LoanAccount(accNumber, balance);
                        member.addAccount(accType, account);
                    }
                }
            }
            System.out.println("Loaded " + membersDb.size() + " member(s) from " + MEMBERS_FILE + ".");
        } catch (FileNotFoundException | NumberFormatException e) {
            System.out.println("Error loading data: " + e.getMessage());
        }
    }

    private static void saveDataToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(MEMBERS_FILE))) {
            for (Member member : membersDb.values()) {
                if (member.getAccounts().isEmpty()) {
                    pw.println(member.getMemberId() + "," + member.getName() + ","
                            + member.getNationalId() + ",NONE,NONE,0.0");
                } else {
                    for (Map.Entry<String, Account> entry : member.getAccounts().entrySet()) {
                        Account acc = entry.getValue();
                        pw.println(member.getMemberId() + ","
                                + member.getName() + ","
                                + member.getNationalId() + ","
                                + entry.getKey() + ","
                                + acc.getAccountNumber() + ","
                                + acc.getBalance());
                    }
                }
            }
            System.out.println("Data saved successfully to " + MEMBERS_FILE + ".");
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    private static void logSystemEvent(String message) {
        try (FileWriter fw = new FileWriter(TRANSACTIONS_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println("SYSTEM | " + message);
        } catch (IOException e) {
            System.out.println("Error logging system event: " + e.getMessage());
        }
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
                scanner.nextLine();
                return value;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a whole number.");
                scanner.nextLine();
            }
        }
    }

    static double readDouble(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                double value = scanner.nextDouble();
                scanner.nextLine();
                return value;
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a numeric amount.");
                scanner.nextLine();
            }
        }
    }
}