package dev.karamousalis.ledgerguard.auth;

public record TokenResponse(String accessToken, String tokenType, long expiresInSeconds) {
}
