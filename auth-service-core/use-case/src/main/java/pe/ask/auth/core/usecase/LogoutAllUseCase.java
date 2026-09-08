package pe.ask.auth.core.usecase;

import pe.ask.auth.core.model.exception.DomainValidation;
import pe.ask.auth.core.model.User;
import pe.ask.auth.core.port.in.LogoutAllInputPort;
import pe.ask.auth.core.port.in.command.LogoutAllCommand;
import pe.ask.auth.core.port.in.result.LogoutAllResult;
import pe.ask.auth.core.port.out.ClockOutputPort;
import pe.ask.auth.core.port.out.SessionRepositoryOutputPort;
import pe.ask.auth.core.port.out.UserRepositoryOutputPort;
import pe.ask.auth.core.usecase.annotation.UseCase;
import reactor.core.publisher.Mono;


@UseCase
public final class LogoutAllUseCase implements LogoutAllInputPort {

    private final UserRepositoryOutputPort userRepository;
    private final SessionRepositoryOutputPort sessionRepository;
    private final ClockOutputPort clockPort;

    public LogoutAllUseCase(
            UserRepositoryOutputPort userRepository,
            SessionRepositoryOutputPort sessionRepository,
            ClockOutputPort clockPort
    ) {
        this.userRepository = DomainValidation.requireNonNull(userRepository, "userRepository");
        this.sessionRepository = DomainValidation.requireNonNull(sessionRepository, "sessionRepository");
        this.clockPort = DomainValidation.requireNonNull(clockPort, "clockPort");
    }

    @Override
    public Mono<LogoutAllResult> logoutAll(LogoutAllCommand command) {
        return clockPort.now()
                .flatMap(now -> sessionRepository.revokeAllByUserId(command.userId(), now)
                        .then(userRepository.findById(command.userId()))
                        .flatMap(user -> {
                            User updated = user.incrementAuthVersion(now);
                            return userRepository.update(updated);
                        }))
                .thenReturn(new LogoutAllResult("All sessions revoked successfully"));
    }
}
