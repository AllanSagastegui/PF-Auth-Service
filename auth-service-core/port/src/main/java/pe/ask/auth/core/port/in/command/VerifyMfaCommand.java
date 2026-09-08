package pe.ask.auth.core.port.in.command;

import pe.ask.auth.core.model.exception.DomainValidation;
import java.util.UUID;

public record VerifyMfaCommand(
        String mfaChallengeToken,
        String totpCode,
        UUID deviceId,
        String ipAddress,
        String userAgent
) {
    public VerifyMfaCommand {
        DomainValidation.requireNonNull(mfaChallengeToken, "mfaChallengeToken");
        DomainValidation.requireNonNull(totpCode, "totpCode");
        DomainValidation.requireNonNull(deviceId, "deviceId");
    }
}
