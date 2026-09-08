package pe.ask.auth.input.api.handler;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import pe.ask.auth.core.port.in.ChangePasswordInputPort;
import pe.ask.auth.core.port.in.ForgotPasswordInputPort;
import pe.ask.auth.core.port.in.ResetPasswordInputPort;
import pe.ask.auth.input.api.dto.request.ChangePasswordRequest;
import pe.ask.auth.input.api.dto.request.ForgotPasswordRequest;
import pe.ask.auth.input.api.dto.request.ResetPasswordRequest;
import pe.ask.auth.input.api.dto.response.ApiResponse;
import pe.ask.auth.input.api.filter.ApiMessageEnum;
import pe.ask.auth.input.api.filter.ApiValidation;
import pe.ask.auth.input.api.filter.CustomRequestValidator;
import pe.ask.auth.input.api.filter.SecurityRequestExtractor;
import pe.ask.auth.input.api.mapper.AuthApiMapper;
import reactor.core.publisher.Mono;

import java.util.UUID;

public final class AuthPasswordHandler {

    private final ForgotPasswordInputPort forgotPasswordPort;
    private final ResetPasswordInputPort resetPasswordPort;
    private final ChangePasswordInputPort changePasswordPort;
    private final CustomRequestValidator validator;
    private final AuthApiMapper mapper;

    public AuthPasswordHandler(
            ForgotPasswordInputPort forgotPasswordPort,
            ResetPasswordInputPort resetPasswordPort,
            ChangePasswordInputPort changePasswordPort,
            CustomRequestValidator validator,
            AuthApiMapper mapper
    ) {
        this.forgotPasswordPort = ApiValidation.requireNonNull(forgotPasswordPort, ApiMessageEnum.PARAM_ARGUMENT.value());
        this.resetPasswordPort = ApiValidation.requireNonNull(resetPasswordPort, ApiMessageEnum.PARAM_ARGUMENT.value());
        this.changePasswordPort = ApiValidation.requireNonNull(changePasswordPort, ApiMessageEnum.PARAM_ARGUMENT.value());
        this.validator = ApiValidation.requireNonNull(validator, ApiMessageEnum.PARAM_ARGUMENT.value());
        this.mapper = ApiValidation.requireNonNull(mapper, ApiMessageEnum.PARAM_ARGUMENT.value());
    }

    public AuthPasswordHandler(
            ForgotPasswordInputPort forgotPasswordPort,
            ResetPasswordInputPort resetPasswordPort,
            ChangePasswordInputPort changePasswordPort,
            CustomRequestValidator validator
    ) {
        this(forgotPasswordPort, resetPasswordPort, changePasswordPort, validator, AuthApiMapper.INSTANCE);
    }

    public Mono<ServerResponse> forgotPassword(ServerRequest request) {
        return request.bodyToMono(ForgotPasswordRequest.class)
                .flatMap(validator::validate)
                .map(mapper::toForgotPasswordCommand)
                .flatMap(forgotPasswordPort::forgotPassword)
                .map(mapper::toForgotPasswordResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponse.success(response)));
    }

    public Mono<ServerResponse> resetPassword(ServerRequest request) {
        return request.bodyToMono(ResetPasswordRequest.class)
                .flatMap(validator::validate)
                .map(mapper::toResetPasswordCommand)
                .flatMap(resetPasswordPort::resetPassword)
                .map(mapper::toResetPasswordResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponse.success(response)));
    }

    public Mono<ServerResponse> changePassword(ServerRequest request) {
        UUID userId = SecurityRequestExtractor.extractUserId(request);
        return request.bodyToMono(ChangePasswordRequest.class)
                .flatMap(validator::validate)
                .map(req -> mapper.toChangePasswordCommand(userId, req))
                .flatMap(changePasswordPort::changePassword)
                .map(mapper::toChangePasswordResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponse.success(response)));
    }
}
