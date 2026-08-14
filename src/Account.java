import java.math.BigDecimal;
import java.sql.Timestamp;

public class Account {
    private final int accountId;
    private final String holderName;
    private final BigDecimal balance;
    private final Timestamp createdAt;

    public Account(int accountId, String holderName, BigDecimal balance, Timestamp createdAt) {
        this.accountId = accountId;
        this.holderName = holderName;
        this.balance = balance;
        this.createdAt = createdAt;
    }

    public int getAccountId() { return accountId; }
    public String getHolderName() { return holderName; }
    public BigDecimal getBalance() { return balance; }
    public Timestamp getCreatedAt() { return createdAt; }
}
