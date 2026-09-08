package pe.ask.auth.core.port.in.result;

import pe.ask.auth.core.model.exception.DomainValidation;

public record VerifyMfaResult(
        String accessToken,
        String refreshToken,
        long expiresInSeconds,
        String tokenType
) {
    public VerifyMfaResult {
        DomainValidation.requireNonNull(accessToken, "accessToken");
        DomainValidation.requireNonNull(refreshToken, "refreshToken");
        DomainValidation.requireNonNull(tokenType, "tokenType");
    }
}
