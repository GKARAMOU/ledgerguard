package dev.karamousalis.ledgerguard.wallet;

import dev.karamousalis.ledgerguard.shared.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wallets")
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(name = "account_number", nullable = false, unique = true, length = 24)
    private String accountNumber;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(nullable = false)
    private boolean active;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Wallet() {
    }

    public Wallet(String name, String accountNumber, String currency, BigDecimal balance) {
        this.name = name;
        this.accountNumber = accountNumber;
        this.currency = currency;
        this.balance = balance;
        this.active = true;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public void debit(BigDecimal amount) {
        assertActive();
        if (amount.signum() <= 0) {
            throw new DomainException("INVALID_AMOUNT", "Transfer amount must be greater than zero.");
        }
        if (balance.compareTo(amount) < 0) {
            throw new DomainException("INSUFFICIENT_FUNDS", "The source wallet does not have enough available funds.");
        }
        balance = balance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        assertActive();
        if (amount.signum() <= 0) {
            throw new DomainException("INVALID_AMOUNT", "Transfer amount must be greater than zero.");
        }
        balance = balance.add(amount);
    }

    private void assertActive() {
        if (!active) {
            throw new DomainException("WALLET_INACTIVE", "The selected wallet is not active.");
        }
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getAccountNumber() { return accountNumber; }
    public String getCurrency() { return currency; }
    public BigDecimal getBalance() { return balance; }
    public boolean isActive() { return active; }
    public long getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
