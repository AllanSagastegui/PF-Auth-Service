package pe.ask.auth.core.port.in.result;

import pe.ask.auth.core.model.exception.DomainValidation;

public record RefreshTokenResult(
        String accessToken,
        String refreshToken,
        long expiresInSeconds,
        String tokenType
) {
    public RefreshTokenResult {
        DomainValidation.requireNonNull(accessToken, "accessToken");
        DomainValidation.requireNonNull(refreshToken, "refreshToken");
        DomainValidation.requireNonNull(tokenType, "tokenType");
    }
}
