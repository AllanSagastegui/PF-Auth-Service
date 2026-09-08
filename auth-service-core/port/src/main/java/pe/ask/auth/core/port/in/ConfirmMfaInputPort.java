package pe.ask.auth.core.port.in;

import pe.ask.auth.core.port.in.command.ConfirmMfaCommand;
import pe.ask.auth.core.port.in.result.ConfirmMfaResult;
import reactor.core.publisher.Mono;

public interface ConfirmMfaInputPort {
    Mono<ConfirmMfaResult> confirm(ConfirmMfaCommand command);
}
