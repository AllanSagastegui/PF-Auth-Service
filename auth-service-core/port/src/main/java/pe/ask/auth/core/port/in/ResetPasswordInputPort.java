package pe.ask.auth.core.port.in;

import pe.ask.auth.core.port.in.command.ResetPasswordCommand;
import pe.ask.auth.core.port.in.result.ResetPasswordResult;
import reactor.core.publisher.Mono;

public interface ResetPasswordInputPort {
    Mono<ResetPasswordResult> resetPassword(ResetPasswordCommand command);
}
