package pe.ask.auth.core.port.in;

import pe.ask.auth.core.port.in.command.RegisterUserCommand;
import pe.ask.auth.core.port.in.result.RegisterUserResult;
import reactor.core.publisher.Mono;

public interface RegisterUserInputPort {
    Mono<RegisterUserResult> register(RegisterUserCommand command);
}
