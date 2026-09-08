package pe.ask.auth.core.port.in;

import pe.ask.auth.core.port.in.command.ChangePasswordCommand;
import pe.ask.auth.core.port.in.result.ChangePasswordResult;
import reactor.core.publisher.Mono;

public interface ChangePasswordInputPort {
    Mono<ChangePasswordResult> changePassword(ChangePasswordCommand command);
}
