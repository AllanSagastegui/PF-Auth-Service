package pe.ask.auth.core.port.in;

import pe.ask.auth.core.port.in.command.EmailVerificationConfirmCommand;
import pe.ask.auth.core.port.in.result.EmailVerificationConfirmResult;
import reactor.core.publisher.Mono;

public interface EmailVerificationConfirmInputPort {
    Mono<EmailVerificationConfirmResult> confirmVerification(EmailVerificationConfirmCommand command);
}
