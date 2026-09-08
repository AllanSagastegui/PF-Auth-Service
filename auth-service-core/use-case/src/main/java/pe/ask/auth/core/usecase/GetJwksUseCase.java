package pe.ask.auth.core.usecase;

import pe.ask.auth.core.model.exception.DomainValidation;
import pe.ask.auth.core.port.in.GetJwksInputPort;
import pe.ask.auth.core.port.in.command.GetJwksCommand;
import pe.ask.auth.core.port.in.result.GetJwksResult;
import pe.ask.auth.core.port.in.result.JwksKeyResult;
import pe.ask.auth.core.port.out.JwksOutputPort;
import pe.ask.auth.core.usecase.annotation.UseCase;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@UseCase
public final class GetJwksUseCase implements GetJwksInputPort {

    private final JwksOutputPort jwksPort;

    public GetJwksUseCase(JwksOutputPort jwksPort) {
        this.jwksPort = DomainValidation.requireNonNull(jwksPort, "jwksPort");
    }

    @Override
    public Mono<GetJwksResult> getJwks(GetJwksCommand command) {
        return jwksPort.getPublicKeys()
                .map(keys -> new GetJwksResult(
                        keys.stream()
                                .map(k -> new JwksKeyResult(k.kty(), k.crv(), k.kid(), k.use(), k.alg(), k.x(), k.y()))
                                .collect(Collectors.toList())
                ));
    }
}
