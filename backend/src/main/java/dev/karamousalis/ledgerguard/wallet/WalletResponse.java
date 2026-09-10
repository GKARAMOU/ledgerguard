package dev.karamousalis.ledgerguard.wallet;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WalletResponse(
        UUID id,
        String name,
        String accountNumber,
        String currency,
        BigDecimal balance,
        boolean active,
        long version,
        Instant updatedAt
) {
    public static WalletResponse from(Wallet wallet) {
        return new WalletResponse(
                wallet.getId(), wallet.getName(), wallet.getAccountNumber(), wallet.getCurrency(),
                wallet.getBalance(), wallet.isActive(), wallet.getVersion(), wallet.getUpdatedAt()
        );
    }
}
