import java.math.BigDecimal;
import java.sql.Timestamp;

public class Transaction {
    private final int transactionId;
    private final int accountId;
    private final String type; // DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT
    private final BigDecimal amount;
    private final Integer relatedAccountId;
    private final Timestamp createdAt;

    public Transaction(int transactionId, int accountId, String type, BigDecimal amount,
                        Integer relatedAccountId, Timestamp createdAt) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.relatedAccountId = relatedAccountId;
        this.createdAt = createdAt;
    }

    public int getTransactionId() { return transactionId; }
    public int getAccountId() { return accountId; }
    public String getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public Integer getRelatedAccountId() { return relatedAccountId; }
    public Timestamp getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        String rel = relatedAccountId != null ? " (acct #" + relatedAccountId + ")" : "";
        return createdAt + " | " + type + rel + " | Rs. " + amount;
    }
}
