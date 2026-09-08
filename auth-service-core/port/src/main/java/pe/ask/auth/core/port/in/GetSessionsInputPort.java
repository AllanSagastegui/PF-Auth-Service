package pe.ask.auth.core.port.in;

import pe.ask.auth.core.port.in.command.GetSessionsCommand;
import pe.ask.auth.core.port.in.result.GetSessionsResult;
import reactor.core.publisher.Mono;

public interface GetSessionsInputPort {
    Mono<GetSessionsResult> getSessions(GetSessionsCommand command);
}
