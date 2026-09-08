package pe.ask.auth.core.port.in;

import pe.ask.auth.core.port.in.command.EnrollMfaCommand;
import pe.ask.auth.core.port.in.result.EnrollMfaResult;
import reactor.core.publisher.Mono;

public interface EnrollMfaInputPort {
    Mono<EnrollMfaResult> enroll(EnrollMfaCommand command);
}
