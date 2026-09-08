package pe.ask.auth.input.api.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import pe.ask.auth.core.port.in.LoginInputPort;
import pe.ask.auth.core.port.in.LogoutAllInputPort;
import pe.ask.auth.core.port.in.LogoutInputPort;
import pe.ask.auth.core.port.in.RefreshTokenInputPort;
import pe.ask.auth.core.port.in.VerifyMfaInputPort;
import pe.ask.auth.input.api.dto.request.LoginRequest;
import pe.ask.auth.input.api.dto.request.LogoutRequest;
import pe.ask.auth.input.api.dto.request.RefreshTokenRequest;
import pe.ask.auth.input.api.dto.request.VerifyMfaRequest;
import pe.ask.auth.input.api.dto.response.ApiResponse;
import pe.ask.auth.input.api.filter.ApiHeaderEnum;
import pe.ask.auth.input.api.filter.ApiMessageEnum;
import pe.ask.auth.input.api.filter.ApiValidation;
import pe.ask.auth.input.api.filter.CustomRequestValidator;
import pe.ask.auth.input.api.filter.SecurityRequestExtractor;
import pe.ask.auth.input.api.mapper.AuthApiMapper;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.UUID;

public final class AuthAuthenticationHandler {

    private static final String COMMA = ",";

    private final LoginInputPort loginPort;
    private final VerifyMfaInputPort verifyMfaPort;
    private final RefreshTokenInputPort refreshPort;
    private final LogoutInputPort logoutPort;
    private final LogoutAllInputPort logoutAllPort;
    private final CustomRequestValidator validator;
    private final AuthApiMapper mapper;

    public AuthAuthenticationHandler(
            LoginInputPort loginPort,
            VerifyMfaInputPort verifyMfaPort,
            RefreshTokenInputPort refreshPort,
            LogoutInputPort logoutPort,
            LogoutAllInputPort logoutAllPort,
            CustomRequestValidator validator,
            AuthApiMapper mapper
    ) {
        this.loginPort = ApiValidation.requireNonNull(loginPort, ApiMessageEnum.PARAM_ARGUMENT.value());
        this.verifyMfaPort = ApiValidation.requireNonNull(verifyMfaPort, ApiMessageEnum.PARAM_ARGUMENT.value());
        this.refreshPort = ApiValidation.requireNonNull(refreshPort, ApiMessageEnum.PARAM_ARGUMENT.value());
        this.logoutPort = ApiValidation.requireNonNull(logoutPort, ApiMessageEnum.PARAM_ARGUMENT.value());
        this.logoutAllPort = ApiValidation.requireNonNull(logoutAllPort, ApiMessageEnum.PARAM_ARGUMENT.value());
        this.validator = ApiValidation.requireNonNull(validator, ApiMessageEnum.PARAM_ARGUMENT.value());
        this.mapper = ApiValidation.requireNonNull(mapper, ApiMessageEnum.PARAM_ARGUMENT.value());
    }

    public AuthAuthenticationHandler(
            LoginInputPort loginPort,
            VerifyMfaInputPort verifyMfaPort,
            RefreshTokenInputPort refreshPort,
            LogoutInputPort logoutPort,
            LogoutAllInputPort logoutAllPort,
            CustomRequestValidator validator
    ) {
        this(loginPort, verifyMfaPort, refreshPort, logoutPort, logoutAllPort, validator, AuthApiMapper.INSTANCE);
    }

    public Mono<ServerResponse> login(ServerRequest request) {
        String ipAddress = extractIpAddress(request);
        String userAgent = extractUserAgent(request);

        return request.bodyToMono(LoginRequest.class)
                .flatMap(validator::validate)
                .map(req -> mapper.toLoginCommand(req, ipAddress, userAgent))
                .flatMap(loginPort::login)
                .map(mapper::toLoginResponse)
                .flatMap(response -> {
                    HttpStatus status = response.mfaRequired() ? HttpStatus.ACCEPTED : HttpStatus.OK;
                    return ServerResponse.status(status)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(ApiResponse.success(response));
                });
    }

    public Mono<ServerResponse> verifyMfa(ServerRequest request) {
        String ipAddress = extractIpAddress(request);
        String userAgent = extractUserAgent(request);

        return request.bodyToMono(VerifyMfaRequest.class)
                .flatMap(validator::validate)
                .map(req -> mapper.toVerifyMfaCommand(req, ipAddress, userAgent))
                .flatMap(verifyMfaPort::verifyMfa)
                .map(mapper::toVerifyMfaResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponse.success(response)));
    }

    public Mono<ServerResponse> refresh(ServerRequest request) {
        String ipAddress = extractIpAddress(request);
        String userAgent = extractUserAgent(request);
        String idempotencyKey = request.headers().firstHeader(ApiHeaderEnum.IDEMPOTENCY_KEY.value());

        return request.bodyToMono(RefreshTokenRequest.class)
                .flatMap(validator::validate)
                .map(req -> mapper.toRefreshTokenCommand(req, idempotencyKey, ipAddress, userAgent))
                .flatMap(refreshPort::refresh)
                .map(mapper::toRefreshTokenResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponse.success(response)));
    }

    public Mono<ServerResponse> logout(ServerRequest request) {
        return request.bodyToMono(LogoutRequest.class)
                .flatMap(validator::validate)
                .map(mapper::toLogoutCommand)
                .flatMap(logoutPort::logout)
                .map(mapper::toLogoutResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponse.success(response)));
    }

    public Mono<ServerResponse> logoutAll(ServerRequest request) {
        UUID userId = SecurityRequestExtractor.extractUserId(request);
        return logoutAllPort.logoutAll(mapper.toLogoutAllCommand(userId))
                .map(mapper::toLogoutAllResponse)
                .flatMap(response -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(ApiResponse.success(response)));
    }

    private String extractIpAddress(ServerRequest request) {
        String xForwardedFor = request.headers().firstHeader(ApiHeaderEnum.FORWARDED_FOR.value());
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(COMMA)[0].trim();
        }
        return request.remoteAddress()
                .map(InetSocketAddress::getHostString)
                .orElse(ApiHeaderEnum.DEFAULT_IP.value());
    }

    private String extractUserAgent(ServerRequest request) {
        String userAgent = request.headers().firstHeader(ApiHeaderEnum.USER_AGENT.value());
        return userAgent != null ? userAgent : ApiHeaderEnum.DEFAULT_USER_AGENT.value();
    }
}
