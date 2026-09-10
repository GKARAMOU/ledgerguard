package dev.karamousalis.ledgerguard.transfer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transfers")
@SecurityRequirement(name = "bearerAuth")
public class TransferController {
    private final TransferService transferService;

    public TransferController(TransferService transferService) { this.transferService = transferService; }

    @PostMapping
    @Operation(summary = "Create an atomic transfer", description = "Moves funds once for a unique Idempotency-Key.")
    public ResponseEntity<TransferResponse> create(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreateTransferRequest request
    ) {
        TransferResponse response = transferService.create(idempotencyKey, request);
        return ResponseEntity.created(URI.create("/api/transfers/" + response.id())).body(response);
    }

    @GetMapping
    @Operation(summary = "List transfers")
    public Page<TransferResponse> list(
            @RequestParam(required = false) UUID walletId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        var pageable = PageRequest.of(Math.max(page, 0), safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        return transferService.list(walletId, pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transfer")
    public TransferResponse get(@PathVariable UUID id) { return transferService.get(id); }
}
