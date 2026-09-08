package pe.ask.auth.core.usecase;

import pe.ask.auth.core.model.exception.DomainValidation;
import pe.ask.auth.core.port.in.RevokeSessionInputPort;
import pe.ask.auth.core.port.in.command.RevokeSessionCommand;
import pe.ask.auth.core.port.in.result.RevokeSessionResult;
import pe.ask.auth.core.port.out.ClockOutputPort;
import pe.ask.auth.core.port.out.RefreshTokenRepositoryOutputPort;
import pe.ask.auth.core.port.out.SessionRepositoryOutputPort;
import pe.ask.auth.core.usecase.annotation.UseCase;
import reactor.core.publisher.Mono;


@UseCase
public final class RevokeSessionUseCase implements RevokeSessionInputPort {

    private final SessionRepositoryOutputPort sessionRepository;
    private final RefreshTokenRepositoryOutputPort refreshTokenRepository;
    private final ClockOutputPort clockPort;

    public RevokeSessionUseCase(
            SessionRepositoryOutputPort sessionRepository,
            RefreshTokenRepositoryOutputPort refreshTokenRepository,
            ClockOutputPort clockPort
    ) {
        this.sessionRepository = DomainValidation.requireNonNull(sessionRepository, "sessionRepository");
        this.refreshTokenRepository = DomainValidation.requireNonNull(refreshTokenRepository, "refreshTokenRepository");
        this.clockPort = DomainValidation.requireNonNull(clockPort, "clockPort");
    }

    @Override
    public Mono<RevokeSessionResult> revokeSession(RevokeSessionCommand command) {
        return clockPort.now()
                .flatMap(now -> sessionRepository.findById(command.sessionId())
                        .flatMap(session -> {
                            if (session.userId().equals(command.userId())) {
                                return sessionRepository.revokeById(session.id())
                                        .then(refreshTokenRepository.revokeBySessionId(session.id(), now));
                            }
                            return Mono.empty();
                        }))
                .then(Mono.just(new RevokeSessionResult("Session revoked successfully")));
    }
}
