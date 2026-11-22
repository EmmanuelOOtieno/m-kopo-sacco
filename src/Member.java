import java.util.HashMap;
import java.util.Map;

public class Member {
    private String memberId;
    private String name;
    private String nationalId;
    private Map<String, Account> accounts;

    public Member(String memberId, String name, String nationalId) {
        this.memberId = memberId;
        this.name = name;
        this.nationalId = nationalId;
        this.accounts = new HashMap<>();
    }

    public String getMemberId() { return memberId; }
    public String getName() { return name; }
    public String getNationalId() { return nationalId; }
    public Map<String, Account> getAccounts() { return accounts; }

    public void setName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
    }

    public void addAccount(String type, Account account) {
        accounts.put(type, account);
    }

    public Account getAccount(String type) {
        return accounts.get(type);
    }
}