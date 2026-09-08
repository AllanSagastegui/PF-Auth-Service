package pe.ask.auth.input.api.handler;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import pe.ask.auth.core.port.in.EmailVerificationConfirmInputPort;
import pe.ask.auth.core.port.in.EmailVerificationRequestInputPort;
import pe.ask.auth.core.port.in.RegisterUserInputPort;
import pe.ask.auth.input.api.dto.request.EmailVerificationConfirmRequest;
import pe.ask.auth.input.api.dto.request.EmailVerificationRequest;
import pe.ask.auth.input.api.dto.request.RegisterRequest;
import pe.ask.auth.input.api.dto.response.ApiResponse;
import pe.ask.auth.input.api.filter.ApiMessageEnum;
import pe.ask.auth.input.api.filter.ApiPathEnum;
import pe.ask.auth.input.api.filter.ApiValidation;
import pe.ask.auth.input.api.filter.CustomRequestValidator;
import pe.ask.auth.input.api.mapper.AuthApiMapper;
import reactor.core.publisher.Mono;

import java.net.URI;

public final class AuthRegistrationHandler {

    private final RegisterUserInputPort registerPort;
    private final EmailVerificationRequestInputPort emailVerificationRequestPort;
    private final EmailVerificationConfirmInputPort emailVerificationConfirmPort;
    private final CustomRequestValidator validator;
    private final AuthApiMapper mapper;

    public AuthRegistrationHandler(
            RegisterUserInputPort registerPort,
            EmailVerificationRequestInputPort emailVerificationRequestPort,
            EmailVerificationConfirmInputPort emailVerificationConfirmPort,
            CustomRequestValidator validator,
            AuthApiMapper mapper
    ) {
        this.registerPort = ApiValidation.requireNonNull(registerPort, ApiMessageEnum.PARAM_ARGUMENT.value());
        this.emailVerificationRequestPort = ApiValidation.requireNonNull(emailVerificationRequestPort, ApiMessageEnum.PARAM_ARGUMENT.value());
        this.emailVerificationConfirmPort = ApiValidation.requireNonNull(emailVerificationConfirmPort, ApiMessageEnum.PARAM_ARGUMENT.value());
        this.validator = ApiValidation.requireNonNull(validator, ApiMessageEnum.PARAM_ARGUMENT.value());
        this.mapper = ApiValidation.requireNonNull(mapper, ApiMessageEnum.PARAM_ARGUMENT.value());
    }

    public AuthRegistrationHandler(
            RegisterUserInputPort registerPort,
            EmailVerificationRequestInputPort emailVerificationRequestPort,
            EmailVerificationConfirmInputPort emailVerificationConfirmPort,
            CustomRequestValidator validator
    ) {
        this(registerPort, emailVerificationRequestPort, emailVerificationConfirmPort, validator, AuthApiMapper.INSTANCE);
    }

    public Mono<ServerResponse> register(ServerRequest request) {
        return request.bodyToMono(RegisterRequest.class)
                .flatMap(validator::validate)
                .map(mapper::toRegisterUserCommand)
                .flatMap(registerPort::register)
                .map(mapper::toRegisterResponse)
                .flatMap(response -> ServerResponse.created(URI.create(ApiPathEnum.ME.value()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponse.success(response)));
    }

    public Mono<ServerResponse> requestEmailVerification(ServerRequest request) {
        return request.bodyToMono(EmailVerificationRequest.class)
                .flatMap(validator::validate)
                .map(mapper::toEmailVerificationRequestCommand)
                .flatMap(emailVerificationRequestPort::requestVerification)
                .map(mapper::toEmailVerificationRequestResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponse.success(response)));
    }

    public Mono<ServerResponse> confirmEmailVerification(ServerRequest request) {
        return request.bodyToMono(EmailVerificationConfirmRequest.class)
                .flatMap(validator::validate)
                .map(mapper::toEmailVerificationConfirmCommand)
                .flatMap(emailVerificationConfirmPort::confirmVerification)
                .map(mapper::toEmailVerificationConfirmResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponse.success(response)));
    }
}
