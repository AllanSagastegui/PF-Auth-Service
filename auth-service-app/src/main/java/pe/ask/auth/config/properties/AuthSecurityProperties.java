package pe.ask.auth.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.security")
public record AuthSecurityProperties(
        Argon2Properties argon2,
        JwtProperties jwt,
        TotpProperties totp,
        RateLimitProperties rateLimit
) {
    public record Argon2Properties(
            int saltLength,
            int hashLength,
            int parallelism,
            int memoryKib,
            int iterations,
            int schedulerThreads,
            String dummyPassword
    ) {
        public Argon2Properties {
            if (saltLength <= 0) saltLength = 16;
            if (hashLength <= 0) hashLength = 32;
            if (parallelism <= 0) parallelism = 1;
            if (memoryKib <= 0) memoryKib = 19456;
            if (iterations <= 0) iterations = 2;
            if (schedulerThreads <= 0) schedulerThreads = 4;
            if (dummyPassword == null || dummyPassword.isBlank()) {
                dummyPassword = "dummy-password-for-anti-enumeration-timing";
            }
        }
    }

    public record JwtProperties(
            String issuer,
            Duration accessTokenTtl,
            Duration refreshTokenTtl
    ) {
        public JwtProperties {
            if (issuer == null) issuer = "pe.ask.auth";
            if (accessTokenTtl == null) accessTokenTtl = Duration.ofMinutes(15);
            if (refreshTokenTtl == null) refreshTokenTtl = Duration.ofDays(7);
        }
    }

    public record TotpProperties(
            String issuer,
            String encryptionKeyHex
    ) {
        public TotpProperties {
            if (issuer == null) issuer = "AskPlatform";
            if (encryptionKeyHex == null) encryptionKeyHex = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
        }
    }

    public record RateLimitProperties(
            int maxRequests,
            Duration window
    ) {
        public RateLimitProperties {
            if (maxRequests <= 0) maxRequests = 100;
            if (window == null) window = Duration.ofMinutes(1);
        }
    }
}
