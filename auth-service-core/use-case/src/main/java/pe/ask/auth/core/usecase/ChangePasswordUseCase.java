package pe.ask.auth.core.usecase;

import pe.ask.auth.core.model.User;
import pe.ask.auth.core.model.constant.DomainConstants;
import pe.ask.auth.core.model.exception.DomainValidation;
import pe.ask.auth.core.model.exception.InvalidCredentialsException;
import pe.ask.auth.core.model.exception.PasswordPolicyException;
import pe.ask.auth.core.model.exception.UserNotFoundException;
import pe.ask.auth.core.port.in.ChangePasswordInputPort;
import pe.ask.auth.core.port.in.command.ChangePasswordCommand;
import pe.ask.auth.core.port.in.result.ChangePasswordResult;
import pe.ask.auth.core.port.out.ClockOutputPort;
import pe.ask.auth.core.port.out.PasswordHasherOutputPort;
import pe.ask.auth.core.port.out.UserRepositoryOutputPort;
import pe.ask.auth.core.usecase.annotation.UseCase;
import reactor.core.publisher.Mono;

import java.util.Map;

@UseCase
public final class ChangePasswordUseCase implements ChangePasswordInputPort {

    private static final int MIN_PASSWORD_LENGTH = 15;
    private static final int MAX_PASSWORD_LENGTH = 128;
    private static final String FIELD_NEW_PASSWORD = "newPassword";

    private final UserRepositoryOutputPort userRepository;
    private final PasswordHasherOutputPort passwordHasher;
    private final ClockOutputPort clockPort;

    public ChangePasswordUseCase(
            UserRepositoryOutputPort userRepository,
            PasswordHasherOutputPort passwordHasher,
            ClockOutputPort clockPort
    ) {
        this.userRepository = DomainValidation.requireNonNull(userRepository, "userRepository");
        this.passwordHasher = DomainValidation.requireNonNull(passwordHasher, "passwordHasher");
        this.clockPort = DomainValidation.requireNonNull(clockPort, "clockPort");
    }

    @Override
    public Mono<ChangePasswordResult> changePassword(ChangePasswordCommand command) {
        if (command.newPassword() == null || command.newPassword().length() < MIN_PASSWORD_LENGTH || command.newPassword().length() > MAX_PASSWORD_LENGTH) {
            return Mono.error(new PasswordPolicyException(Map.of(
                    FIELD_NEW_PASSWORD, DomainConstants.MSG_INVALID_PASSWORD_LENGTH
            )));
        }

        return clockPort.now()
                .flatMap(now -> userRepository.findById(command.userId())
                        .switchIfEmpty(Mono.error(new UserNotFoundException()))
                        .flatMap(user -> passwordHasher.verifyPassword(command.currentPassword(), user.passwordHash())
                                .filter(Boolean::booleanValue)
                                .switchIfEmpty(Mono.error(new InvalidCredentialsException()))
                                .flatMap(ignored -> passwordHasher.hashPassword(command.newPassword()))
                                .flatMap(newHash -> {
                                    User updated = user.changePassword(newHash, now);
                                    return userRepository.update(updated);
                                })))
                .thenReturn(new ChangePasswordResult(DomainConstants.MSG_PASSWORD_CHANGED_SUCCESS));
    }
}
