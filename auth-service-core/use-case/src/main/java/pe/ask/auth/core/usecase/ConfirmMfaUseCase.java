package pe.ask.auth.core.usecase;

import pe.ask.auth.core.model.constant.DomainConstants;
import pe.ask.auth.core.model.exception.DomainValidation;
import pe.ask.auth.core.model.exception.InvalidCredentialsException;
import pe.ask.auth.core.model.exception.UserNotFoundException;
import pe.ask.auth.core.port.in.ConfirmMfaInputPort;
import pe.ask.auth.core.port.in.command.ConfirmMfaCommand;
import pe.ask.auth.core.port.in.result.ConfirmMfaResult;
import pe.ask.auth.core.port.out.ClockOutputPort;
import pe.ask.auth.core.port.out.TotpOutputPort;
import pe.ask.auth.core.port.out.UserRepositoryOutputPort;
import pe.ask.auth.core.usecase.annotation.UseCase;
import reactor.core.publisher.Mono;

@UseCase
public final class ConfirmMfaUseCase implements ConfirmMfaInputPort {

    private final UserRepositoryOutputPort userRepository;
    private final TotpOutputPort totpPort;
    private final ClockOutputPort clockPort;

    public ConfirmMfaUseCase(
            UserRepositoryOutputPort userRepository,
            TotpOutputPort totpPort,
            ClockOutputPort clockPort
    ) {
        this.userRepository = DomainValidation.requireNonNull(userRepository, "userRepository");
        this.totpPort = DomainValidation.requireNonNull(totpPort, "totpPort");
        this.clockPort = DomainValidation.requireNonNull(clockPort, "clockPort");
    }

    @Override
    public Mono<ConfirmMfaResult> confirm(ConfirmMfaCommand command) {
        return clockPort.now()
                .flatMap(now -> userRepository.findById(command.userId())
                        .switchIfEmpty(Mono.error(new UserNotFoundException()))
                        .flatMap(user -> Mono.justOrEmpty(user.totpSecretEncrypted())
                                .switchIfEmpty(Mono.error(new InvalidCredentialsException()))
                                .flatMap(totpPort::decryptSecret)
                                .flatMap(rawSecret -> totpPort.verifyCode(rawSecret, command.totpCode()))
                                .filter(Boolean::booleanValue)
                                .switchIfEmpty(Mono.error(new InvalidCredentialsException()))
                                .thenReturn(new ConfirmMfaResult(DomainConstants.MSG_MFA_ACTIVATED_SUCCESS))));
    }
}
