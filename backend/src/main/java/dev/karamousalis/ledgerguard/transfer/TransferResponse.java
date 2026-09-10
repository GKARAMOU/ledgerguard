package dev.karamousalis.ledgerguard.transfer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferResponse(
        UUID id,
        UUID sourceWalletId,
        UUID destinationWalletId,
        BigDecimal amount,
        String currency,
        String reference,
        TransferStatus status,
        Instant createdAt
) {
    public static TransferResponse from(Transfer transfer) {
        return new TransferResponse(
                transfer.getId(), transfer.getSourceWallet().getId(), transfer.getDestinationWallet().getId(),
                transfer.getAmount(), transfer.getSourceWallet().getCurrency(), transfer.getReference(),
                transfer.getStatus(), transfer.getCreatedAt()
        );
    }
}
