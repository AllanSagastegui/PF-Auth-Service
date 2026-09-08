package pe.ask.auth.core.usecase;

import pe.ask.auth.core.model.exception.DomainValidation;
import pe.ask.auth.core.model.OneTimeToken;
import pe.ask.auth.core.model.OneTimeTokenType;
import pe.ask.auth.core.model.User;
import pe.ask.auth.core.model.exception.InvalidTokenException;
import pe.ask.auth.core.model.exception.PasswordPolicyException;
import pe.ask.auth.core.model.exception.TokenExpiredException;
import pe.ask.auth.core.port.in.ResetPasswordInputPort;
import pe.ask.auth.core.port.in.command.ResetPasswordCommand;
import pe.ask.auth.core.port.in.result.ResetPasswordResult;
import pe.ask.auth.core.port.out.ClockOutputPort;
import pe.ask.auth.core.port.out.OneTimeTokenRepositoryOutputPort;
import pe.ask.auth.core.port.out.PasswordHasherOutputPort;
import pe.ask.auth.core.port.out.SessionRepositoryOutputPort;
import pe.ask.auth.core.port.out.TokenGeneratorOutputPort;
import pe.ask.auth.core.port.out.UserRepositoryOutputPort;
import pe.ask.auth.core.usecase.annotation.UseCase;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

@UseCase
public final class ResetPasswordUseCase implements ResetPasswordInputPort {

    private static final int MIN_PASSWORD_LENGTH = 15;
    private static final int MAX_PASSWORD_LENGTH = 128;

    private final UserRepositoryOutputPort userRepository;
    private final SessionRepositoryOutputPort sessionRepository;
    private final OneTimeTokenRepositoryOutputPort oneTimeTokenRepository;
    private final PasswordHasherOutputPort passwordHasher;
    private final TokenGeneratorOutputPort tokenGenerator;
    private final ClockOutputPort clockPort;

    public ResetPasswordUseCase(
            UserRepositoryOutputPort userRepository,
            SessionRepositoryOutputPort sessionRepository,
            OneTimeTokenRepositoryOutputPort oneTimeTokenRepository,
            PasswordHasherOutputPort passwordHasher,
            TokenGeneratorOutputPort tokenGenerator,
            ClockOutputPort clockPort
    ) {
        this.userRepository = DomainValidation.requireNonNull(userRepository, "userRepository");
        this.sessionRepository = DomainValidation.requireNonNull(sessionRepository, "sessionRepository");
        this.oneTimeTokenRepository = DomainValidation.requireNonNull(oneTimeTokenRepository, "oneTimeTokenRepository");
        this.passwordHasher = DomainValidation.requireNonNull(passwordHasher, "passwordHasher");
        this.tokenGenerator = DomainValidation.requireNonNull(tokenGenerator, "tokenGenerator");
        this.clockPort = DomainValidation.requireNonNull(clockPort, "clockPort");
    }

    @Override
    public Mono<ResetPasswordResult> resetPassword(ResetPasswordCommand command) {
        if (command.newPassword() == null || command.newPassword().length() < MIN_PASSWORD_LENGTH || command.newPassword().length() > MAX_PASSWORD_LENGTH) {
            return Mono.error(new PasswordPolicyException(Map.of(
                    "newPassword", "Password must be between " + MIN_PASSWORD_LENGTH + " and " + MAX_PASSWORD_LENGTH + " characters"
            )));
        }

        return clockPort.now()
                .flatMap(now -> tokenGenerator.hashToken(command.token())
                        .flatMap(tokenHash -> oneTimeTokenRepository.findByTokenHashAndType(tokenHash, OneTimeTokenType.PASSWORD_RESET)
                                .switchIfEmpty(Mono.error(new InvalidTokenException()))
                                .flatMap(token -> validateAndConsumeToken(token, now))
                                .flatMap(token -> userRepository.findById(token.userId())
                                        .switchIfEmpty(Mono.error(new InvalidTokenException()))
                                        .flatMap(user -> passwordHasher.hashPassword(command.newPassword())
                                                .flatMap(newHash -> {
                                                    User updated = user.changePassword(newHash, now);
                                                    return userRepository.update(updated)
                                                            .then(sessionRepository.revokeAllByUserId(user.id(), now));
                                                })))))
                .thenReturn(new ResetPasswordResult("Password reset successfully. You can now login with your new password."));
    }

    private Mono<OneTimeToken> validateAndConsumeToken(OneTimeToken token, Instant now) {
        if (token.used()) {
            return Mono.error(new InvalidTokenException());
        }
        if (token.expiresAt().isBefore(now)) {
            return Mono.error(new TokenExpiredException());
        }
        OneTimeToken used = token.markUsed(now);
        return oneTimeTokenRepository.update(used);
    }
}
