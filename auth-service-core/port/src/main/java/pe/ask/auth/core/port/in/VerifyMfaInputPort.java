package pe.ask.auth.core.port.in;

import pe.ask.auth.core.port.in.command.VerifyMfaCommand;
import pe.ask.auth.core.port.in.result.VerifyMfaResult;
import reactor.core.publisher.Mono;

public interface VerifyMfaInputPort {
    Mono<VerifyMfaResult> verifyMfa(VerifyMfaCommand command);
}
