package pe.ask.auth.input.api.handler;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import pe.ask.auth.core.port.in.GetJwksInputPort;
import pe.ask.auth.input.api.filter.ApiMessageEnum;
import pe.ask.auth.input.api.filter.ApiValidation;
import pe.ask.auth.input.api.mapper.AuthApiMapper;
import reactor.core.publisher.Mono;

public final class AuthJwksHandler {

    private final GetJwksInputPort jwksPort;
    private final AuthApiMapper mapper;

    public AuthJwksHandler(GetJwksInputPort jwksPort, AuthApiMapper mapper) {
        this.jwksPort = ApiValidation.requireNonNull(jwksPort, ApiMessageEnum.PARAM_ARGUMENT.value());
        this.mapper = ApiValidation.requireNonNull(mapper, ApiMessageEnum.PARAM_ARGUMENT.value());
    }

    public AuthJwksHandler(GetJwksInputPort jwksPort) {
        this(jwksPort, AuthApiMapper.INSTANCE);
    }

    @SuppressWarnings("java:S1172")
    public Mono<ServerResponse> getJwks(ServerRequest request) {
        return jwksPort.getJwks(mapper.toGetJwksCommand())
                .map(mapper::toJwksResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(response));
    }
}
