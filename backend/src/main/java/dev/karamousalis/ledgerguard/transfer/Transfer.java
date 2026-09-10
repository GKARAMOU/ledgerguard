package dev.karamousalis.ledgerguard.transfer;

import dev.karamousalis.ledgerguard.wallet.Wallet;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transfers")
public class Transfer {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 72)
    private String idempotencyKey;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_wallet_id", nullable = false)
    private Wallet sourceWallet;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_wallet_id", nullable = false)
    private Wallet destinationWallet;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false, length = 100)
    private String reference;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransferStatus status;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Transfer() {
    }

    public Transfer(String idempotencyKey, Wallet sourceWallet, Wallet destinationWallet, BigDecimal amount, String reference) {
        this.idempotencyKey = idempotencyKey;
        this.sourceWallet = sourceWallet;
        this.destinationWallet = destinationWallet;
        this.amount = amount;
        this.reference = reference;
        this.status = TransferStatus.COMPLETED;
    }

    @PrePersist
    void onCreate() { createdAt = Instant.now(); }

    public UUID getId() { return id; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public Wallet getSourceWallet() { return sourceWallet; }
    public Wallet getDestinationWallet() { return destinationWallet; }
    public BigDecimal getAmount() { return amount; }
    public String getReference() { return reference; }
    public TransferStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
