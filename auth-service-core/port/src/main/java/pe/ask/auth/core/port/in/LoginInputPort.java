package pe.ask.auth.core.port.in;

import pe.ask.auth.core.port.in.command.LoginCommand;
import pe.ask.auth.core.port.in.result.LoginResult;
import reactor.core.publisher.Mono;

public interface LoginInputPort {
    Mono<LoginResult> login(LoginCommand command);
}
