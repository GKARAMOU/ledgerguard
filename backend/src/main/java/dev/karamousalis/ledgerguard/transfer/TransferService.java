package dev.karamousalis.ledgerguard.transfer;

import dev.karamousalis.ledgerguard.shared.DomainException;
import dev.karamousalis.ledgerguard.shared.ResourceNotFoundException;
import dev.karamousalis.ledgerguard.wallet.Wallet;
import dev.karamousalis.ledgerguard.wallet.WalletRepository;
import java.math.RoundingMode;
import java.util.UUID;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferService {
    private final TransferRepository transferRepository;
    private final WalletRepository walletRepository;

    public TransferService(TransferRepository transferRepository, WalletRepository walletRepository) {
        this.transferRepository = transferRepository;
        this.walletRepository = walletRepository;
    }

    @Transactional
    public TransferResponse create(String idempotencyKey, CreateTransferRequest request) {
        validateIdempotencyKey(idempotencyKey);
        return transferRepository.findByIdempotencyKey(idempotencyKey)
                .map(TransferResponse::from)
                .orElseGet(() -> executeTransfer(idempotencyKey, request));
    }

    private TransferResponse executeTransfer(String idempotencyKey, CreateTransferRequest request) {
        if (request.sourceWalletId().equals(request.destinationWalletId())) {
            throw new DomainException("SAME_WALLET", "Source and destination wallets must be different.");
        }
        Wallet source = walletRepository.findById(request.sourceWalletId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet", request.sourceWalletId()));
        Wallet destination = walletRepository.findById(request.destinationWalletId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet", request.destinationWalletId()));

        if (!source.getCurrency().equals(destination.getCurrency())) {
            throw new DomainException("CURRENCY_MISMATCH", "Transfers require wallets with the same currency.");
        }

        var normalizedAmount = request.amount().setScale(2, RoundingMode.UNNECESSARY);
        source.debit(normalizedAmount);
        destination.credit(normalizedAmount);

        try {
            walletRepository.saveAllAndFlush(java.util.List.of(source, destination));
        } catch (OptimisticLockingFailureException exception) {
            throw new DomainException("CONCURRENT_UPDATE", "A wallet changed during this transfer. Retry with the same idempotency key.");
        }

        Transfer transfer = new Transfer(idempotencyKey, source, destination, normalizedAmount, request.reference().trim());
        return TransferResponse.from(transferRepository.save(transfer));
    }

    @Transactional(readOnly = true)
    public Page<TransferResponse> list(UUID walletId, Pageable pageable) {
        if (walletId == null) return transferRepository.findAll(pageable).map(TransferResponse::from);
        return transferRepository.findBySourceWalletIdOrDestinationWalletId(walletId, walletId, pageable)
                .map(TransferResponse::from);
    }

    @Transactional(readOnly = true)
    public TransferResponse get(UUID id) {
        return transferRepository.findById(id)
                .map(TransferResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer", id));
    }

    private void validateIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 72) {
            throw new DomainException("INVALID_IDEMPOTENCY_KEY", "Idempotency-Key must contain between 1 and 72 characters.");
        }
    }
}
