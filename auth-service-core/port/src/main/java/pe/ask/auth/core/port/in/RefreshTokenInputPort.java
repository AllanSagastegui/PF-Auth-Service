package pe.ask.auth.core.port.in;

import pe.ask.auth.core.port.in.command.RefreshTokenCommand;
import pe.ask.auth.core.port.in.result.RefreshTokenResult;
import reactor.core.publisher.Mono;

public interface RefreshTokenInputPort {
    Mono<RefreshTokenResult> refresh(RefreshTokenCommand command);
}
