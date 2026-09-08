package pe.ask.auth.core.port.in;

import pe.ask.auth.core.port.in.command.EmailVerificationRequestCommand;
import pe.ask.auth.core.port.in.result.EmailVerificationRequestResult;
import reactor.core.publisher.Mono;

public interface EmailVerificationRequestInputPort {
    Mono<EmailVerificationRequestResult> requestVerification(EmailVerificationRequestCommand command);
}
