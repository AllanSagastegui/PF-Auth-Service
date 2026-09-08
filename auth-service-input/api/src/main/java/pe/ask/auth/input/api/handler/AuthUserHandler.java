package pe.ask.auth.input.api.handler;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import pe.ask.auth.core.port.in.GetMeInputPort;
import pe.ask.auth.core.port.in.GetSessionsInputPort;
import pe.ask.auth.core.port.in.RevokeSessionInputPort;
import pe.ask.auth.input.api.dto.response.ApiResponse;
import pe.ask.auth.input.api.filter.ApiMessageEnum;
import pe.ask.auth.input.api.filter.ApiValidation;
import pe.ask.auth.input.api.filter.SecurityRequestExtractor;
import pe.ask.auth.input.api.mapper.AuthApiMapper;
import reactor.core.publisher.Mono;

import java.util.UUID;

public final class AuthUserHandler {

    private static final String PATH_VAR_SESSION_ID = "sessionId";

    private final GetMeInputPort getMePort;
    private final GetSessionsInputPort getSessionsPort;
    private final RevokeSessionInputPort revokeSessionPort;
    private final AuthApiMapper mapper;

    public AuthUserHandler(
            GetMeInputPort getMePort,
            GetSessionsInputPort getSessionsPort,
            RevokeSessionInputPort revokeSessionPort,
            AuthApiMapper mapper
    ) {
        this.getMePort = ApiValidation.requireNonNull(getMePort, ApiMessageEnum.PARAM_ARGUMENT.value());
        this.getSessionsPort = ApiValidation.requireNonNull(getSessionsPort, ApiMessageEnum.PARAM_ARGUMENT.value());
        this.revokeSessionPort = ApiValidation.requireNonNull(revokeSessionPort, ApiMessageEnum.PARAM_ARGUMENT.value());
        this.mapper = ApiValidation.requireNonNull(mapper, ApiMessageEnum.PARAM_ARGUMENT.value());
    }

    public AuthUserHandler(
            GetMeInputPort getMePort,
            GetSessionsInputPort getSessionsPort,
            RevokeSessionInputPort revokeSessionPort
    ) {
        this(getMePort, getSessionsPort, revokeSessionPort, AuthApiMapper.INSTANCE);
    }

    public Mono<ServerResponse> getMe(ServerRequest request) {
        UUID userId = SecurityRequestExtractor.extractUserId(request);
        return getMePort.getMe(mapper.toGetMeCommand(userId))
                .map(mapper::toMeResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponse.success(response)));
    }

    public Mono<ServerResponse> getSessions(ServerRequest request) {
        UUID userId = SecurityRequestExtractor.extractUserId(request);
        return getSessionsPort.getSessions(mapper.toGetSessionsCommand(userId))
                .map(mapper::toSessionsResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponse.success(response)));
    }

    public Mono<ServerResponse> revokeSession(ServerRequest request) {
        UUID userId = SecurityRequestExtractor.extractUserId(request);
        UUID sessionId = UUID.fromString(request.pathVariable(PATH_VAR_SESSION_ID));
        return revokeSessionPort.revokeSession(mapper.toRevokeSessionCommand(userId, sessionId))
                .map(mapper::toRevokeSessionResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponse.success(response)));
    }
}
