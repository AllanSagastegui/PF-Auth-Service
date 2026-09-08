package pe.ask.auth.core.port.in;

import pe.ask.auth.core.port.in.command.LogoutCommand;
import pe.ask.auth.core.port.in.result.LogoutResult;
import reactor.core.publisher.Mono;

public interface LogoutInputPort {
    Mono<LogoutResult> logout(LogoutCommand command);
}
