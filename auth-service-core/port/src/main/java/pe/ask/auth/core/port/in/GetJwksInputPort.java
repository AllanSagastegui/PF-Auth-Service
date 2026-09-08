package pe.ask.auth.core.port.in;

import pe.ask.auth.core.port.in.command.GetJwksCommand;
import pe.ask.auth.core.port.in.result.GetJwksResult;
import reactor.core.publisher.Mono;

public interface GetJwksInputPort {
    Mono<GetJwksResult> getJwks(GetJwksCommand command);
}
