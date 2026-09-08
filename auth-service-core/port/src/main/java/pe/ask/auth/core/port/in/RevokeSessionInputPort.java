package pe.ask.auth.core.port.in;

import pe.ask.auth.core.port.in.command.RevokeSessionCommand;
import pe.ask.auth.core.port.in.result.RevokeSessionResult;
import reactor.core.publisher.Mono;

public interface RevokeSessionInputPort {
    Mono<RevokeSessionResult> revokeSession(RevokeSessionCommand command);
}
