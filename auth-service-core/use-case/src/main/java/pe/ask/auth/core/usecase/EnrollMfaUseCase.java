package pe.ask.auth.core.usecase;

import pe.ask.auth.core.model.exception.DomainValidation;
import pe.ask.auth.core.model.User;
import pe.ask.auth.core.model.exception.UserNotFoundException;
import pe.ask.auth.core.port.in.EnrollMfaInputPort;
import pe.ask.auth.core.port.in.command.EnrollMfaCommand;
import pe.ask.auth.core.port.in.result.EnrollMfaResult;
import pe.ask.auth.core.port.out.ClockOutputPort;
import pe.ask.auth.core.port.out.TokenGeneratorOutputPort;
import pe.ask.auth.core.port.out.TotpOutputPort;
import pe.ask.auth.core.port.out.UserRepositoryOutputPort;
import pe.ask.auth.core.usecase.annotation.UseCase;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@UseCase
public final class EnrollMfaUseCase implements EnrollMfaInputPort {

    private final UserRepositoryOutputPort userRepository;
    private final TotpOutputPort totpPort;
    private final TokenGeneratorOutputPort tokenGenerator;
    private final ClockOutputPort clockPort;

    public EnrollMfaUseCase(
            UserRepositoryOutputPort userRepository,
            TotpOutputPort totpPort,
            TokenGeneratorOutputPort tokenGenerator,
            ClockOutputPort clockPort
    ) {
        this.userRepository = DomainValidation.requireNonNull(userRepository, "userRepository");
        this.totpPort = DomainValidation.requireNonNull(totpPort, "totpPort");
        this.tokenGenerator = DomainValidation.requireNonNull(tokenGenerator, "tokenGenerator");
        this.clockPort = DomainValidation.requireNonNull(clockPort, "clockPort");
    }

    @Override
    public Mono<EnrollMfaResult> enroll(EnrollMfaCommand command) {
        return clockPort.now()
                .flatMap(now -> userRepository.findById(command.userId())
                        .switchIfEmpty(Mono.error(new UserNotFoundException()))
                        .flatMap(user -> totpPort.generateSecret()
                                .flatMap(rawSecret -> totpPort.encryptSecret(rawSecret)
                                        .flatMap(encryptedSecret -> generateRecoveryCodes()
                                                .flatMap(codes -> totpPort.generateTotpUri(user.rawEmail(), rawSecret)
                                                        .flatMap(qrUri -> {
                                                            User updated = user.enableMfa(encryptedSecret, now);
                                                            return userRepository.update(updated)
                                                                    .thenReturn(new EnrollMfaResult(
                                                                            rawSecret,
                                                                            qrUri,
                                                                            codes
                                                                    ));
                                                        }))))));
    }

    private Mono<List<String>> generateRecoveryCodes() {
        return Flux.range(1, 8)
                .flatMap(i -> tokenGenerator.generateSecureToken().map(token -> token.substring(0, 10).toUpperCase()))
                .collectList();
    }
}
