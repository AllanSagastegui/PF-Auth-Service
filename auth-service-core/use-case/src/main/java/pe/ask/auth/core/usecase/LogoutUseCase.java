package pe.ask.auth.core.usecase;

import pe.ask.auth.core.model.exception.DomainValidation;
import pe.ask.auth.core.model.RefreshToken;
import pe.ask.auth.core.port.in.LogoutInputPort;
import pe.ask.auth.core.port.in.command.LogoutCommand;
import pe.ask.auth.core.port.in.result.LogoutResult;
import pe.ask.auth.core.port.out.ClockOutputPort;
import pe.ask.auth.core.port.out.RefreshTokenRepositoryOutputPort;
import pe.ask.auth.core.port.out.SessionRepositoryOutputPort;
import pe.ask.auth.core.port.out.TokenGeneratorOutputPort;
import pe.ask.auth.core.usecase.annotation.UseCase;
import reactor.core.publisher.Mono;


@UseCase
public final class LogoutUseCase implements LogoutInputPort {

    private final SessionRepositoryOutputPort sessionRepository;
    private final RefreshTokenRepositoryOutputPort refreshTokenRepository;
    private final TokenGeneratorOutputPort tokenGenerator;
    private final ClockOutputPort clockPort;

    public LogoutUseCase(
            SessionRepositoryOutputPort sessionRepository,
            RefreshTokenRepositoryOutputPort refreshTokenRepository,
            TokenGeneratorOutputPort tokenGenerator,
            ClockOutputPort clockPort
    ) {
        this.sessionRepository = DomainValidation.requireNonNull(sessionRepository, "sessionRepository");
        this.refreshTokenRepository = DomainValidation.requireNonNull(refreshTokenRepository, "refreshTokenRepository");
        this.tokenGenerator = DomainValidation.requireNonNull(tokenGenerator, "tokenGenerator");
        this.clockPort = DomainValidation.requireNonNull(clockPort, "clockPort");
    }

    @Override
    public Mono<LogoutResult> logout(LogoutCommand command) {
        return clockPort.now()
                .flatMap(now -> tokenGenerator.hashToken(command.rawRefreshToken())
                        .flatMap(tokenHash -> refreshTokenRepository.findByTokenHash(tokenHash)
                                .flatMap(token -> {
                                    if (token.deviceId().equals(command.deviceId())) {
                                        RefreshToken revoked = token.revoke(now);
                                        return refreshTokenRepository.update(revoked)
                                                .then(sessionRepository.revokeById(token.sessionId()));
                                    }
                                    return Mono.empty();
                                })))
                .then(Mono.just(new LogoutResult("Logged out successfully")));
    }
}
