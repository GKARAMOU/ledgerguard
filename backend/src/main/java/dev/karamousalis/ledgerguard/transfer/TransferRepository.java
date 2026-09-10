package dev.karamousalis.ledgerguard.transfer;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferRepository extends JpaRepository<Transfer, UUID> {
    Optional<Transfer> findByIdempotencyKey(String idempotencyKey);
    Page<Transfer> findBySourceWalletIdOrDestinationWalletId(UUID sourceWalletId, UUID destinationWalletId, Pageable pageable);
}
