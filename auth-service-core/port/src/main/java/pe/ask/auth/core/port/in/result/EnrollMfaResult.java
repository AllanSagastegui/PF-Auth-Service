package pe.ask.auth.core.port.in.result;

import pe.ask.auth.core.model.exception.DomainValidation;
import java.util.Collections;
import java.util.List;

public record EnrollMfaResult(
        String secret,
        String qrCodeUri,
        List<String> recoveryCodes
) {
    public EnrollMfaResult {
        DomainValidation.requireNonNull(secret, "secret");
        DomainValidation.requireNonNull(qrCodeUri, "qrCodeUri");
        recoveryCodes = recoveryCodes != null ? Collections.unmodifiableList(recoveryCodes) : Collections.emptyList();
    }
}
