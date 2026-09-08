package pe.ask.auth.core.port.in;

import pe.ask.auth.core.port.in.command.DisableMfaCommand;
import pe.ask.auth.core.port.in.result.DisableMfaResult;
import reactor.core.publisher.Mono;

public interface DisableMfaInputPort {
    Mono<DisableMfaResult> disable(DisableMfaCommand command);
}
