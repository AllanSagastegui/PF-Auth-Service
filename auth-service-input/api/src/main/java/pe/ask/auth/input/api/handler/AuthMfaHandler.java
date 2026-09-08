package pe.ask.auth.input.api.handler;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import pe.ask.auth.core.port.in.ConfirmMfaInputPort;
import pe.ask.auth.core.port.in.DisableMfaInputPort;
import pe.ask.auth.core.port.in.EnrollMfaInputPort;
import pe.ask.auth.input.api.dto.request.ConfirmMfaRequest;
import pe.ask.auth.input.api.dto.request.DisableMfaRequest;
import pe.ask.auth.input.api.dto.response.ApiResponse;
import pe.ask.auth.input.api.filter.ApiMessageEnum;
import pe.ask.auth.input.api.filter.ApiValidation;
import pe.ask.auth.input.api.filter.CustomRequestValidator;
import pe.ask.auth.input.api.filter.SecurityRequestExtractor;
import pe.ask.auth.input.api.mapper.AuthApiMapper;
import reactor.core.publisher.Mono;

import java.util.UUID;

public final class AuthMfaHandler {

    private final EnrollMfaInputPort enrollPort;
    private final ConfirmMfaInputPort confirmPort;
    private final DisableMfaInputPort disablePort;
    private final CustomRequestValidator validator;
    private final AuthApiMapper mapper;

    public AuthMfaHandler(
            EnrollMfaInputPort enrollPort,
            ConfirmMfaInputPort confirmPort,
            DisableMfaInputPort disablePort,
            CustomRequestValidator validator,
            AuthApiMapper mapper
    ) {
        this.enrollPort = ApiValidation.requireNonNull(enrollPort, ApiMessageEnum.PARAM_ARGUMENT.value());
        this.confirmPort = ApiValidation.requireNonNull(confirmPort, ApiMessageEnum.PARAM_ARGUMENT.value());
        this.disablePort = ApiValidation.requireNonNull(disablePort, ApiMessageEnum.PARAM_ARGUMENT.value());
        this.validator = ApiValidation.requireNonNull(validator, ApiMessageEnum.PARAM_ARGUMENT.value());
        this.mapper = ApiValidation.requireNonNull(mapper, ApiMessageEnum.PARAM_ARGUMENT.value());
    }

    public AuthMfaHandler(
            EnrollMfaInputPort enrollPort,
            ConfirmMfaInputPort confirmPort,
            DisableMfaInputPort disablePort,
            CustomRequestValidator validator
    ) {
        this(enrollPort, confirmPort, disablePort, validator, AuthApiMapper.INSTANCE);
    }

    public Mono<ServerResponse> enroll(ServerRequest request) {
        UUID userId = SecurityRequestExtractor.extractUserId(request);
        return enrollPort.enroll(mapper.toEnrollMfaCommand(userId))
                .map(mapper::toEnrollMfaResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponse.success(response)));
    }

    public Mono<ServerResponse> confirm(ServerRequest request) {
        UUID userId = SecurityRequestExtractor.extractUserId(request);
        return request.bodyToMono(ConfirmMfaRequest.class)
                .flatMap(validator::validate)
                .map(req -> mapper.toConfirmMfaCommand(userId, req))
                .flatMap(confirmPort::confirm)
                .map(mapper::toConfirmMfaResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponse.success(response)));
    }

    public Mono<ServerResponse> disable(ServerRequest request) {
        UUID userId = SecurityRequestExtractor.extractUserId(request);
        return request.bodyToMono(DisableMfaRequest.class)
                .flatMap(validator::validate)
                .map(req -> mapper.toDisableMfaCommand(userId, req))
                .flatMap(disablePort::disable)
                .map(mapper::toDisableMfaResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponse.success(response)));
    }
}
