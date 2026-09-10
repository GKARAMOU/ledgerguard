package dev.karamousalis.ledgerguard.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("ledgerguard.jwt")
public record JwtProperties(String secret, long expirationMinutes) {
}
