package pe.ask.auth.output.security.adapter;

import com.nimbusds.jose.jwk.ECKey;
import pe.ask.auth.core.model.JwksKey;
import pe.ask.auth.core.model.exception.DomainValidation;
import pe.ask.auth.core.port.out.JwksOutputPort;
import reactor.core.publisher.Mono;

import java.util.List;

public final class JwksAdapter implements JwksOutputPort {

    private static final String USE_SIG = "sig";
    private static final String ALG_ES256 = "ES256";

    private final ECKey ecJwk;

    public JwksAdapter(ECKey ecJwk) {
        this.ecJwk = DomainValidation.requireNonNull(ecJwk, "ecJwk");
    }

    @Override
    public Mono<List<JwksKey>> getPublicKeys() {
        return Mono.fromCallable(() -> {
            ECKey pub = ecJwk.toPublicJWK();
            return List.of(new JwksKey(
                    pub.getKeyType().getValue(),
                    pub.getCurve().getName(),
                    pub.getKeyID(),
                    USE_SIG,
                    pub.getAlgorithm() != null ? pub.getAlgorithm().getName() : ALG_ES256,
                    pub.getX().toString(),
                    pub.getY().toString()
            ));
        });
    }
}
