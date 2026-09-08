package pe.ask.auth.core.port.in.result;

public record LoginResult(
        boolean mfaRequired,
        String mfaChallengeToken,
        String accessToken,
        String refreshToken,
        long expiresInSeconds,
        String tokenType
) {
    public static LoginResult mfaRequired(String mfaChallengeToken) {
        return new LoginResult(true, mfaChallengeToken, null, null, 0, null);
    }

    public static LoginResult success(String accessToken, String refreshToken, long expiresInSeconds) {
        return new LoginResult(false, null, accessToken, refreshToken, expiresInSeconds, "Bearer");
    }
}
