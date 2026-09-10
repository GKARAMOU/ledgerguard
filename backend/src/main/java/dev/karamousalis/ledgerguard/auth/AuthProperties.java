package dev.karamousalis.ledgerguard.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("ledgerguard.auth")
public record AuthProperties(String username, String password) {
}
