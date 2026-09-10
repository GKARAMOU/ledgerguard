package dev.karamousalis.ledgerguard.shared;

import java.time.Instant;
import java.util.Map;

public record ProblemResponse(String code, String message, int status, Instant timestamp, Map<String, String> validationErrors) {
}
