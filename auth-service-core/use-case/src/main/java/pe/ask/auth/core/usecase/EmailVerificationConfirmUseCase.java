package pe.ask.auth.core.usecase;

import pe.ask.auth.core.model.exception.DomainValidation;
import pe.ask.auth.core.model.OneTimeToken;
import pe.ask.auth.core.model.OneTimeTokenType;
import pe.ask.auth.core.model.User;
import pe.ask.auth.core.model.exception.InvalidTokenException;
import pe.ask.auth.core.model.exception.TokenExpiredException;
import pe.ask.auth.core.port.in.EmailVerificationConfirmInputPort;
import pe.ask.auth.core.port.in.command.EmailVerificationConfirmCommand;
import pe.ask.auth.core.port.in.result.EmailVerificationConfirmResult;
import pe.ask.auth.core.port.out.ClockOutputPort;
import pe.ask.auth.core.port.out.OneTimeTokenRepositoryOutputPort;
import pe.ask.auth.core.port.out.TokenGeneratorOutputPort;
import pe.ask.auth.core.port.out.UserRepositoryOutputPort;
import pe.ask.auth.core.usecase.annotation.UseCase;
import reactor.core.publisher.Mono;

import java.time.Instant;

@UseCase
public final class EmailVerificationConfirmUseCase implements EmailVerificationConfirmInputPort {

    private final UserRepositoryOutputPort userRepository;
    private final OneTimeTokenRepositoryOutputPort oneTimeTokenRepository;
    private final TokenGeneratorOutputPort tokenGenerator;
    private final ClockOutputPort clockPort;

    public EmailVerificationConfirmUseCase(
            UserRepositoryOutputPort userRepository,
            OneTimeTokenRepositoryOutputPort oneTimeTokenRepository,
            TokenGeneratorOutputPort tokenGenerator,
            ClockOutputPort clockPort
    ) {
        this.userRepository = DomainValidation.requireNonNull(userRepository, "userRepository");
        this.oneTimeTokenRepository = DomainValidation.requireNonNull(oneTimeTokenRepository, "oneTimeTokenRepository");
        this.tokenGenerator = DomainValidation.requireNonNull(tokenGenerator, "tokenGenerator");
        this.clockPort = DomainValidation.requireNonNull(clockPort, "clockPort");
    }

    @Override
    public Mono<EmailVerificationConfirmResult> confirmVerification(EmailVerificationConfirmCommand command) {
        return clockPort.now()
                .flatMap(now -> tokenGenerator.hashToken(command.token())
                        .flatMap(tokenHash -> oneTimeTokenRepository.findByTokenHashAndType(tokenHash, OneTimeTokenType.EMAIL_VERIFICATION)
                                .switchIfEmpty(Mono.error(new InvalidTokenException()))
                                .flatMap(token -> validateAndConsumeToken(token, now))
                                .flatMap(token -> userRepository.findById(token.userId())
                                        .switchIfEmpty(Mono.error(new InvalidTokenException()))
                                        .flatMap(user -> {
                                            User verifiedUser = user.verifyEmail(now);
                                            return userRepository.update(verifiedUser);
                                        }))
                                .thenReturn(new EmailVerificationConfirmResult("Email verified successfully. You can now login."))));
    }

    private Mono<OneTimeToken> validateAndConsumeToken(OneTimeToken token, Instant now) {
        if (token.used()) {
            return Mono.error(new InvalidTokenException());
        }
        if (token.expiresAt().isBefore(now)) {
            return Mono.error(new TokenExpiredException());
        }
        OneTimeToken usedToken = token.markUsed(now);
        return oneTimeTokenRepository.update(usedToken);
    }
}
