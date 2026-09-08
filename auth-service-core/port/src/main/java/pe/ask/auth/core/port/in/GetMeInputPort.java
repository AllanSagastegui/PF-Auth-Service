package pe.ask.auth.core.port.in;

import pe.ask.auth.core.port.in.command.GetMeCommand;
import pe.ask.auth.core.port.in.result.GetMeResult;
import reactor.core.publisher.Mono;

public interface GetMeInputPort {
    Mono<GetMeResult> getMe(GetMeCommand command);
}
