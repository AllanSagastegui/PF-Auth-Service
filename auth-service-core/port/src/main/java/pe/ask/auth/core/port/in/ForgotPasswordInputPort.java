package pe.ask.auth.core.port.in;

import pe.ask.auth.core.port.in.command.ForgotPasswordCommand;
import pe.ask.auth.core.port.in.result.ForgotPasswordResult;
import reactor.core.publisher.Mono;

public interface ForgotPasswordInputPort {
    Mono<ForgotPasswordResult> forgotPassword(ForgotPasswordCommand command);
}
