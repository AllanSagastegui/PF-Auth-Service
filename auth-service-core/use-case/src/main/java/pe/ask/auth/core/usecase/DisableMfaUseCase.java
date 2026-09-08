package pe.ask.auth.core.usecase;

import pe.ask.auth.core.model.User;
import pe.ask.auth.core.model.constant.DomainConstants;
import pe.ask.auth.core.model.exception.DomainValidation;
import pe.ask.auth.core.model.exception.InvalidCredentialsException;
import pe.ask.auth.core.model.exception.UserNotFoundException;
import pe.ask.auth.core.port.in.DisableMfaInputPort;
import pe.ask.auth.core.port.in.command.DisableMfaCommand;
import pe.ask.auth.core.port.in.result.DisableMfaResult;
import pe.ask.auth.core.port.out.ClockOutputPort;
import pe.ask.auth.core.port.out.PasswordHasherOutputPort;
import pe.ask.auth.core.port.out.TotpOutputPort;
import pe.ask.auth.core.port.out.UserRepositoryOutputPort;
import pe.ask.auth.core.usecase.annotation.UseCase;
import reactor.core.publisher.Mono;

@UseCase
public final class DisableMfaUseCase implements DisableMfaInputPort {

    private final UserRepositoryOutputPort userRepository;
    private final PasswordHasherOutputPort passwordHasher;
    private final TotpOutputPort totpPort;
    private final ClockOutputPort clockPort;

    public DisableMfaUseCase(
            UserRepositoryOutputPort userRepository,
            PasswordHasherOutputPort passwordHasher,
            TotpOutputPort totpPort,
            ClockOutputPort clockPort
    ) {
        this.userRepository = DomainValidation.requireNonNull(userRepository, "userRepository");
        this.passwordHasher = DomainValidation.requireNonNull(passwordHasher, "passwordHasher");
        this.totpPort = DomainValidation.requireNonNull(totpPort, "totpPort");
        this.clockPort = DomainValidation.requireNonNull(clockPort, "clockPort");
    }

    @Override
    public Mono<DisableMfaResult> disable(DisableMfaCommand command) {
        return clockPort.now()
                .flatMap(now -> userRepository.findById(command.userId())
                        .switchIfEmpty(Mono.error(new UserNotFoundException()))
                        .flatMap(user -> passwordHasher.verifyPassword(command.password(), user.passwordHash())
                                .filter(Boolean::booleanValue)
                                .switchIfEmpty(Mono.error(new InvalidCredentialsException()))
                                .filter(ignored -> user.mfaEnabled() && user.totpSecretEncrypted() != null)
                                .switchIfEmpty(Mono.error(new InvalidCredentialsException()))
                                .flatMap(ignored -> totpPort.decryptSecret(user.totpSecretEncrypted()))
                                .flatMap(rawSecret -> totpPort.verifyCode(rawSecret, command.totpCode()))
                                .filter(Boolean::booleanValue)
                                .switchIfEmpty(Mono.error(new InvalidCredentialsException()))
                                .flatMap(ignored -> {
                                    User updated = user.disableMfa(now);
                                    return userRepository.update(updated);
                                })
                                .thenReturn(new DisableMfaResult(DomainConstants.MSG_MFA_DISABLED_SUCCESS))));
    }
}
