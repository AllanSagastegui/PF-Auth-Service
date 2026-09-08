package pe.ask.auth.core.model;

import pe.ask.auth.core.model.constant.DomainFieldEnum;
import pe.ask.auth.core.model.exception.DomainValidation;

public record AuthTokens(
        String accessToken,
        String rawRefreshToken,
        long expiresInSeconds,
        String tokenType
) {

    public AuthTokens {
        DomainValidation.requireNonNull(accessToken, DomainFieldEnum.ACCESS_TOKEN.value());
        DomainValidation.requireNonNull(rawRefreshToken, DomainFieldEnum.RAW_REFRESH_TOKEN.value());
        DomainValidation.requireNonNull(tokenType, DomainFieldEnum.TOKEN_TYPE.value());
    }

    public static AuthTokens ofBearer(String accessToken, String rawRefreshToken, long expiresInSeconds) {
        return new AuthTokens(accessToken, rawRefreshToken, expiresInSeconds, DomainFieldEnum.BEARER.value());
    }
}
