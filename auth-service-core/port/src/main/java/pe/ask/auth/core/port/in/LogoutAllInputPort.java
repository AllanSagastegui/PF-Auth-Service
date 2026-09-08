package pe.ask.auth.core.port.in;

import pe.ask.auth.core.port.in.command.LogoutAllCommand;
import pe.ask.auth.core.port.in.result.LogoutAllResult;
import reactor.core.publisher.Mono;

public interface LogoutAllInputPort {
    Mono<LogoutAllResult> logoutAll(LogoutAllCommand command);
}
