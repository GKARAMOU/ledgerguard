package dev.karamousalis.ledgerguard.wallet;

import dev.karamousalis.ledgerguard.shared.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallets")
@SecurityRequirement(name = "bearerAuth")
public class WalletController {
    private final WalletRepository walletRepository;

    public WalletController(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @GetMapping
    @Operation(summary = "List wallets", description = "Returns every wallet ordered by account name.")
    public List<WalletResponse> list() {
        return walletRepository.findAll().stream()
                .sorted(Comparator.comparing(Wallet::getName))
                .map(WalletResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get wallet")
    public WalletResponse get(@PathVariable UUID id) {
        return walletRepository.findById(id)
                .map(WalletResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet", id));
    }
}
