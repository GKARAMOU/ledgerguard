package dev.karamousalis.ledgerguard.transfer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.karamousalis.ledgerguard.shared.DomainException;
import dev.karamousalis.ledgerguard.wallet.Wallet;
import dev.karamousalis.ledgerguard.wallet.WalletRepository;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class TransferServiceIntegrationTest {
    @Autowired private TransferService transferService;
    @Autowired private TransferRepository transferRepository;
    @Autowired private WalletRepository walletRepository;
    private UUID sourceId;
    private UUID destinationId;

    @BeforeEach
    void setUp() {
        transferRepository.deleteAll();
        walletRepository.deleteAll();
        Wallet source = walletRepository.save(new Wallet("Operating", "LG-TEST-001", "EUR", new BigDecimal("1000.00")));
        Wallet destination = walletRepository.save(new Wallet("Reserve", "LG-TEST-002", "EUR", new BigDecimal("250.00")));
        sourceId = source.getId();
        destinationId = destination.getId();
    }

    @Test
    void movesFundsAtomically() {
        TransferResponse response = transferService.create("request-001", new CreateTransferRequest(sourceId, destinationId, new BigDecimal("125.50"), "Operations reserve"));
        assertThat(response.status()).isEqualTo(TransferStatus.COMPLETED);
        assertThat(walletRepository.findById(sourceId).orElseThrow().getBalance()).isEqualByComparingTo("874.50");
        assertThat(walletRepository.findById(destinationId).orElseThrow().getBalance()).isEqualByComparingTo("375.50");
    }

    @Test
    void returnsTheExistingTransferForTheSameIdempotencyKey() {
        var request = new CreateTransferRequest(sourceId, destinationId, new BigDecimal("100.00"), "One payment");
        TransferResponse first = transferService.create("request-once", request);
        TransferResponse repeated = transferService.create("request-once", request);
        assertThat(repeated.id()).isEqualTo(first.id());
        assertThat(transferRepository.count()).isEqualTo(1);
        assertThat(walletRepository.findById(sourceId).orElseThrow().getBalance()).isEqualByComparingTo("900.00");
    }

    @Test
    void rejectsInsufficientFundsWithoutChangingBalances() {
        var request = new CreateTransferRequest(sourceId, destinationId, new BigDecimal("1500.00"), "Too large");
        assertThatThrownBy(() -> transferService.create("request-fail", request))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("enough available funds");
        assertThat(transferRepository.count()).isZero();
        assertThat(walletRepository.findById(sourceId).orElseThrow().getBalance()).isEqualByComparingTo("1000.00");
    }
}
