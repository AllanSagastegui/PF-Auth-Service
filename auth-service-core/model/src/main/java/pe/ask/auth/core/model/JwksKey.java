package pe.ask.auth.core.model;

import pe.ask.auth.core.model.exception.DomainValidation;

public record JwksKey(
        String kty,
        String crv,
        String kid,
        String use,
        String alg,
        String x,
        String y
) {

    public JwksKey {
        DomainValidation.requireNonNull(kty, "kty");
        DomainValidation.requireNonNull(crv, "crv");
        DomainValidation.requireNonNull(kid, "kid");
        DomainValidation.requireNonNull(use, "use");
        DomainValidation.requireNonNull(alg, "alg");
        DomainValidation.requireNonNull(x, "x");
        DomainValidation.requireNonNull(y, "y");
    }
}
