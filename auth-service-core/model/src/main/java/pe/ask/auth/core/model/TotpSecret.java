package pe.ask.auth.core.model;

import pe.ask.auth.core.model.constant.DomainFieldEnum;
import pe.ask.auth.core.model.exception.DomainValidation;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public record TotpSecret(
        UUID userId,
        String secretEncrypted,
        List<String> recoveryCodesHashed,
        boolean confirmed
) {

    public TotpSecret {
        DomainValidation.requireNonNull(userId, DomainFieldEnum.USER_ID.value());
        DomainValidation.requireNonNull(secretEncrypted, DomainFieldEnum.SECRET_ENCRYPTED.value());
        recoveryCodesHashed = recoveryCodesHashed != null ? Collections.unmodifiableList(recoveryCodesHashed) : Collections.emptyList();
    }
}
