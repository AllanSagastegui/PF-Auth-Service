package pe.ask.auth.input.api.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;
import pe.ask.auth.core.port.in.command.ChangePasswordCommand;
import pe.ask.auth.core.port.in.command.ConfirmMfaCommand;
import pe.ask.auth.core.port.in.command.DisableMfaCommand;
import pe.ask.auth.core.port.in.command.EmailVerificationConfirmCommand;
import pe.ask.auth.core.port.in.command.EmailVerificationRequestCommand;
import pe.ask.auth.core.port.in.command.EnrollMfaCommand;
import pe.ask.auth.core.port.in.command.ForgotPasswordCommand;
import pe.ask.auth.core.port.in.command.GetJwksCommand;
import pe.ask.auth.core.port.in.command.GetMeCommand;
import pe.ask.auth.core.port.in.command.GetSessionsCommand;
import pe.ask.auth.core.port.in.command.LoginCommand;
import pe.ask.auth.core.port.in.command.LogoutAllCommand;
import pe.ask.auth.core.port.in.command.LogoutCommand;
import pe.ask.auth.core.port.in.command.RefreshTokenCommand;
import pe.ask.auth.core.port.in.command.RegisterUserCommand;
import pe.ask.auth.core.port.in.command.ResetPasswordCommand;
import pe.ask.auth.core.port.in.command.RevokeSessionCommand;
import pe.ask.auth.core.port.in.command.VerifyMfaCommand;
import pe.ask.auth.core.port.in.result.ChangePasswordResult;
import pe.ask.auth.core.port.in.result.ConfirmMfaResult;
import pe.ask.auth.core.port.in.result.DisableMfaResult;
import pe.ask.auth.core.port.in.result.EmailVerificationConfirmResult;
import pe.ask.auth.core.port.in.result.EmailVerificationRequestResult;
import pe.ask.auth.core.port.in.result.EnrollMfaResult;
import pe.ask.auth.core.port.in.result.ForgotPasswordResult;
import pe.ask.auth.core.port.in.result.GetJwksResult;
import pe.ask.auth.core.port.in.result.GetMeResult;
import pe.ask.auth.core.port.in.result.GetSessionsResult;
import pe.ask.auth.core.port.in.result.JwksKeyResult;
import pe.ask.auth.core.port.in.result.LoginResult;
import pe.ask.auth.core.port.in.result.LogoutAllResult;
import pe.ask.auth.core.port.in.result.LogoutResult;
import pe.ask.auth.core.port.in.result.RefreshTokenResult;
import pe.ask.auth.core.port.in.result.RegisterUserResult;
import pe.ask.auth.core.port.in.result.ResetPasswordResult;
import pe.ask.auth.core.port.in.result.RevokeSessionResult;
import pe.ask.auth.core.port.in.result.SessionSummary;
import pe.ask.auth.core.port.in.result.VerifyMfaResult;
import pe.ask.auth.input.api.dto.request.ChangePasswordRequest;
import pe.ask.auth.input.api.dto.request.ConfirmMfaRequest;
import pe.ask.auth.input.api.dto.request.DisableMfaRequest;
import pe.ask.auth.input.api.dto.request.EmailVerificationConfirmRequest;
import pe.ask.auth.input.api.dto.request.EmailVerificationRequest;
import pe.ask.auth.input.api.dto.request.ForgotPasswordRequest;
import pe.ask.auth.input.api.dto.request.LoginRequest;
import pe.ask.auth.input.api.dto.request.LogoutRequest;
import pe.ask.auth.input.api.dto.request.RefreshTokenRequest;
import pe.ask.auth.input.api.dto.request.RegisterRequest;
import pe.ask.auth.input.api.dto.request.ResetPasswordRequest;
import pe.ask.auth.input.api.dto.request.VerifyMfaRequest;
import pe.ask.auth.input.api.dto.response.EnrollMfaResponse;
import pe.ask.auth.input.api.dto.response.JwksKeyResponse;
import pe.ask.auth.input.api.dto.response.JwksResponse;
import pe.ask.auth.input.api.dto.response.LoginResponse;
import pe.ask.auth.input.api.dto.response.MeResponse;
import pe.ask.auth.input.api.dto.response.MessageResponse;
import pe.ask.auth.input.api.dto.response.RegisterResponse;
import pe.ask.auth.input.api.dto.response.SessionResponse;
import pe.ask.auth.input.api.dto.response.SessionsResponse;
import pe.ask.auth.input.api.dto.response.TokenResponse;
import pe.ask.auth.input.api.filter.ApiHeaderEnum;
import pe.ask.auth.input.api.filter.ApiMessageEnum;
import pe.ask.auth.input.api.filter.ApiValidation;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, implementationName = "<CLASS_NAME>SupportMapper")
public abstract class AuthApiMapper {

    public static final AuthApiMapper INSTANCE = Mappers.getMapper(AuthApiMapper.class);

    public abstract RegisterUserCommand mapToRegisterUserCommand(RegisterRequest request);
    public abstract RegisterResponse mapToRegisterResponse(RegisterUserResult result);

    @Mapping(target = "expiresIn", source = "expiresInSeconds")
    public abstract TokenResponse mapToTokenResponse(LoginResult result);

    @Mapping(target = "expiresIn", source = "expiresInSeconds")
    public abstract TokenResponse mapToVerifyMfaResponse(VerifyMfaResult result);

    @Mapping(target = "expiresIn", source = "expiresInSeconds")
    public abstract TokenResponse mapToRefreshTokenResponse(RefreshTokenResult result);

    public abstract MessageResponse mapToLogoutResponse(LogoutResult result);
    public abstract MessageResponse mapToLogoutAllResponse(LogoutAllResult result);
    public abstract EmailVerificationRequestCommand mapToEmailVerificationRequestCommand(EmailVerificationRequest request);
    public abstract MessageResponse mapToEmailVerificationRequestResponse(EmailVerificationRequestResult result);
    public abstract EmailVerificationConfirmCommand mapToEmailVerificationConfirmCommand(EmailVerificationConfirmRequest request);
    public abstract MessageResponse mapToEmailVerificationConfirmResponse(EmailVerificationConfirmResult result);
    public abstract ForgotPasswordCommand mapToForgotPasswordCommand(ForgotPasswordRequest request);
    public abstract MessageResponse mapToForgotPasswordResponse(ForgotPasswordResult result);
    public abstract ResetPasswordCommand mapToResetPasswordCommand(ResetPasswordRequest request);
    public abstract MessageResponse mapToResetPasswordResponse(ResetPasswordResult result);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "currentPassword", source = "request.currentPassword")
    @Mapping(target = "newPassword", source = "request.newPassword")
    public abstract ChangePasswordCommand mapToChangePasswordCommand(UUID userId, ChangePasswordRequest request);

    public abstract MessageResponse mapToChangePasswordResponse(ChangePasswordResult result);
    public abstract MeResponse mapToMeResponse(GetMeResult result);

    public abstract SessionResponse mapToSessionResponse(SessionSummary session);
    public abstract List<SessionResponse> mapToSessionResponseList(List<SessionSummary> sessions);
    public abstract SessionsResponse mapToSessionsResponse(GetSessionsResult result);

    public abstract MessageResponse mapToRevokeSessionResponse(RevokeSessionResult result);
    public abstract EnrollMfaResponse mapToEnrollMfaResponse(EnrollMfaResult result);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "totpCode", source = "request.totpCode")
    public abstract ConfirmMfaCommand mapToConfirmMfaCommand(UUID userId, ConfirmMfaRequest request);

    public abstract MessageResponse mapToConfirmMfaResponse(ConfirmMfaResult result);

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "password", source = "request.password")
    @Mapping(target = "totpCode", source = "request.totpCode")
    public abstract DisableMfaCommand mapToDisableMfaCommand(UUID userId, DisableMfaRequest request);

    public abstract MessageResponse mapToDisableMfaResponse(DisableMfaResult result);

    public abstract JwksKeyResponse mapToJwksKeyResponse(JwksKeyResult key);
    public abstract List<JwksKeyResponse> mapToJwksKeyResponseList(List<JwksKeyResult> keys);
    public abstract JwksResponse mapToJwksResponse(GetJwksResult result);

    public RegisterUserCommand toRegisterUserCommand(RegisterRequest request) {
        ApiValidation.requireNonNull(request, ApiMessageEnum.PARAM_ARGUMENT.value());
        return mapToRegisterUserCommand(request);
    }

    public RegisterResponse toRegisterResponse(RegisterUserResult result) {
        ApiValidation.requireNonNull(result, ApiMessageEnum.PARAM_ARGUMENT.value());
        return mapToRegisterResponse(result);
    }

    public LoginCommand toLoginCommand(LoginRequest request, String ipAddress, String userAgent) {
        ApiValidation.requireNonNull(request, ApiMessageEnum.PARAM_ARGUMENT.value());
        return new LoginCommand(
                request.email(),
                request.password(),
                resolveDeviceId(request.deviceId()),
                resolveIpAddress(ipAddress),
                resolveUserAgent(userAgent)
        );
    }

    public LoginResponse toLoginResponse(LoginResult result) {
        ApiValidation.requireNonNull(result, ApiMessageEnum.PARAM_ARGUMENT.value());
        if (result.mfaRequired()) {
            return LoginResponse.mfa(result.mfaChallengeToken());
        }
        TokenResponse tokens = mapToTokenResponse(result);
        return LoginResponse.success(tokens);
    }

    public VerifyMfaCommand toVerifyMfaCommand(VerifyMfaRequest request, String ipAddress, String userAgent) {
        ApiValidation.requireNonNull(request, ApiMessageEnum.PARAM_ARGUMENT.value());
        return new VerifyMfaCommand(
                request.mfaChallengeToken(),
                request.totpCode(),
                resolveDeviceId(request.deviceId()),
                resolveIpAddress(ipAddress),
                resolveUserAgent(userAgent)
        );
    }

    public TokenResponse toVerifyMfaResponse(VerifyMfaResult result) {
        ApiValidation.requireNonNull(result, ApiMessageEnum.PARAM_ARGUMENT.value());
        return mapToVerifyMfaResponse(result);
    }

    public RefreshTokenCommand toRefreshTokenCommand(RefreshTokenRequest request, String idempotencyKey, String ipAddress, String userAgent) {
        ApiValidation.requireNonNull(request, ApiMessageEnum.PARAM_ARGUMENT.value());
        return new RefreshTokenCommand(
                request.refreshToken(),
                resolveDeviceId(request.deviceId()),
                idempotencyKey,
                "",
                resolveIpAddress(ipAddress),
                resolveUserAgent(userAgent)
        );
    }

    public TokenResponse toRefreshTokenResponse(RefreshTokenResult result) {
        ApiValidation.requireNonNull(result, ApiMessageEnum.PARAM_ARGUMENT.value());
        return mapToRefreshTokenResponse(result);
    }

    public LogoutCommand toLogoutCommand(LogoutRequest request) {
        ApiValidation.requireNonNull(request, ApiMessageEnum.PARAM_ARGUMENT.value());
        return new LogoutCommand(
                request.refreshToken(),
                resolveDeviceId(request.deviceId())
        );
    }

    public MessageResponse toLogoutResponse(LogoutResult result) {
        ApiValidation.requireNonNull(result, ApiMessageEnum.PARAM_ARGUMENT.value());
        return mapToLogoutResponse(result);
    }

    public LogoutAllCommand toLogoutAllCommand(UUID userId) {
        ApiValidation.requireNonNull(userId, ApiMessageEnum.PARAM_ARGUMENT.value());
        return new LogoutAllCommand(userId);
    }

    public MessageResponse toLogoutAllResponse(LogoutAllResult result) {
        ApiValidation.requireNonNull(result, ApiMessageEnum.PARAM_ARGUMENT.value());
        return mapToLogoutAllResponse(result);
    }

    public EmailVerificationRequestCommand toEmailVerificationRequestCommand(EmailVerificationRequest request) {
        ApiValidation.requireNonNull(request, ApiMessageEnum.PARAM_ARGUMENT.value());
        return mapToEmailVerificationRequestCommand(request);
    }

    public MessageResponse toEmailVerificationRequestResponse(EmailVerificationRequestResult result) {
        ApiValidation.requireNonNull(result, ApiMessageEnum.PARAM_ARGUMENT.value());
        return mapToEmailVerificationRequestResponse(result);
    }

    public EmailVerificationConfirmCommand toEmailVerificationConfirmCommand(EmailVerificationConfirmRequest request) {
        ApiValidation.requireNonNull(request, ApiMessageEnum.PARAM_ARGUMENT.value());
        return mapToEmailVerificationConfirmCommand(request);
    }

    public MessageResponse toEmailVerificationConfirmResponse(EmailVerificationConfirmResult result) {
        ApiValidation.requireNonNull(result, ApiMessageEnum.PARAM_ARGUMENT.value());
        return mapToEmailVerificationConfirmResponse(result);
    }

    public ForgotPasswordCommand toForgotPasswordCommand(ForgotPasswordRequest request) {
        ApiValidation.requireNonNull(request, ApiMessageEnum.PARAM_ARGUMENT.value());
        return mapToForgotPasswordCommand(request);
    }

    public MessageResponse toForgotPasswordResponse(ForgotPasswordResult result) {
        ApiValidation.requireNonNull(result, ApiMessageEnum.PARAM_ARGUMENT.value());
        return mapToForgotPasswordResponse(result);
    }

    public ResetPasswordCommand toResetPasswordCommand(ResetPasswordRequest request) {
        ApiValidation.requireNonNull(request, ApiMessageEnum.PARAM_ARGUMENT.value());
        return mapToResetPasswordCommand(request);
    }

    public MessageResponse toResetPasswordResponse(ResetPasswordResult result) {
        ApiValidation.requireNonNull(result, ApiMessageEnum.PARAM_ARGUMENT.value());
        return mapToResetPasswordResponse(result);
    }

    public ChangePasswordCommand toChangePasswordCommand(UUID userId, ChangePasswordRequest request) {
        ApiValidation.requireNonNull(userId, ApiMessageEnum.PARAM_ARGUMENT.value());
        ApiValidation.requireNonNull(request, ApiMessageEnum.PARAM_ARGUMENT.value());
        return mapToChangePasswordCommand(userId, request);
    }

    public MessageResponse toChangePasswordResponse(ChangePasswordResult result) {
        ApiValidation.requireNonNull(result, ApiMessageEnum.PARAM_ARGUMENT.value());
        return mapToChangePasswordResponse(result);
    }

    public GetMeCommand toGetMeCommand(UUID userId) {
        ApiValidation.requireNonNull(userId, ApiMessageEnum.PARAM_ARGUMENT.value());
        return new GetMeCommand(userId);
    }

    public MeResponse toMeResponse(GetMeResult result) {
        ApiValidation.requireNonNull(result, ApiMessageEnum.PARAM_ARGUMENT.value());
        return mapToMeResponse(result);
    }

    public GetSessionsCommand toGetSessionsCommand(UUID userId) {
        ApiValidation.requireNonNull(userId, ApiMessageEnum.PARAM_ARGUMENT.value());
        return new GetSessionsCommand(userId);
    }

    public SessionsResponse toSessionsResponse(GetSessionsResult result) {
        ApiValidation.requireNonNull(result, ApiMessageEnum.PARAM_ARGUMENT.value());
        return mapToSessionsResponse(result);
    }

    public RevokeSessionCommand toRevokeSessionCommand(UUID userId, UUID sessionId) {
        ApiValidation.requireNonNull(userId, ApiMessageEnum.PARAM_ARGUMENT.value());
        ApiValidation.requireNonNull(sessionId, ApiMessageEnum.PARAM_ARGUMENT.value());
        return new RevokeSessionCommand(userId, sessionId);
    }

    public MessageResponse toRevokeSessionResponse(RevokeSessionResult result) {
        ApiValidation.requireNonNull(result, ApiMessageEnum.PARAM_ARGUMENT.value());
        return mapToRevokeSessionResponse(result);
    }

    public EnrollMfaCommand toEnrollMfaCommand(UUID userId) {
        ApiValidation.requireNonNull(userId, ApiMessageEnum.PARAM_ARGUMENT.value());
        return new EnrollMfaCommand(userId);
    }

    public EnrollMfaResponse toEnrollMfaResponse(EnrollMfaResult result) {
        ApiValidation.requireNonNull(result, ApiMessageEnum.PARAM_ARGUMENT.value());
        return mapToEnrollMfaResponse(result);
    }

    public ConfirmMfaCommand toConfirmMfaCommand(UUID userId, ConfirmMfaRequest request) {
        ApiValidation.requireNonNull(userId, ApiMessageEnum.PARAM_ARGUMENT.value());
        ApiValidation.requireNonNull(request, ApiMessageEnum.PARAM_ARGUMENT.value());
        return mapToConfirmMfaCommand(userId, request);
    }

    public MessageResponse toConfirmMfaResponse(ConfirmMfaResult result) {
        ApiValidation.requireNonNull(result, ApiMessageEnum.PARAM_ARGUMENT.value());
        return mapToConfirmMfaResponse(result);
    }

    public DisableMfaCommand toDisableMfaCommand(UUID userId, DisableMfaRequest request) {
        ApiValidation.requireNonNull(userId, ApiMessageEnum.PARAM_ARGUMENT.value());
        ApiValidation.requireNonNull(request, ApiMessageEnum.PARAM_ARGUMENT.value());
        return mapToDisableMfaCommand(userId, request);
    }

    public MessageResponse toDisableMfaResponse(DisableMfaResult result) {
        ApiValidation.requireNonNull(result, ApiMessageEnum.PARAM_ARGUMENT.value());
        return mapToDisableMfaResponse(result);
    }

    public GetJwksCommand toGetJwksCommand() {
        return new GetJwksCommand();
    }

    public JwksResponse toJwksResponse(GetJwksResult result) {
        ApiValidation.requireNonNull(result, ApiMessageEnum.PARAM_ARGUMENT.value());
        return mapToJwksResponse(result);
    }

    private static UUID resolveDeviceId(UUID deviceId) {
        if (deviceId != null) {
            return deviceId;
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return new UUID(random.nextLong(), random.nextLong());
    }

    private static String resolveIpAddress(String ipAddress) {
        return ipAddress != null && !ipAddress.isBlank() ? ipAddress : ApiHeaderEnum.DEFAULT_IP.value();
    }

    private static String resolveUserAgent(String userAgent) {
        return userAgent != null && !userAgent.isBlank() ? userAgent : ApiHeaderEnum.DEFAULT_USER_AGENT.value();
    }

    // Static delegating methods for backward compatibility
    public static RegisterUserCommand toCommand(RegisterRequest request) {
        return INSTANCE.toRegisterUserCommand(request);
    }

    public static RegisterResponse toResponse(RegisterUserResult result) {
        return INSTANCE.toRegisterResponse(result);
    }

    public static LoginCommand toCommand(LoginRequest request, String ipAddress, String userAgent) {
        return INSTANCE.toLoginCommand(request, ipAddress, userAgent);
    }

    public static LoginResponse toResponse(LoginResult result) {
        return INSTANCE.toLoginResponse(result);
    }

    public static VerifyMfaCommand toCommand(VerifyMfaRequest request, String ipAddress, String userAgent) {
        return INSTANCE.toVerifyMfaCommand(request, ipAddress, userAgent);
    }

    public static TokenResponse toResponse(VerifyMfaResult result) {
        return INSTANCE.toVerifyMfaResponse(result);
    }

    public static RefreshTokenCommand toCommand(RefreshTokenRequest request, String idempotencyKey, String ipAddress, String userAgent) {
        return INSTANCE.toRefreshTokenCommand(request, idempotencyKey, ipAddress, userAgent);
    }

    public static TokenResponse toResponse(RefreshTokenResult result) {
        return INSTANCE.toRefreshTokenResponse(result);
    }

    public static LogoutCommand toCommand(LogoutRequest request) {
        return INSTANCE.toLogoutCommand(request);
    }

    public static MessageResponse toResponse(LogoutResult result) {
        return INSTANCE.toLogoutResponse(result);
    }

    public static LogoutAllCommand toCommand(UUID userId) {
        return INSTANCE.toLogoutAllCommand(userId);
    }

    public static MessageResponse toResponse(LogoutAllResult result) {
        return INSTANCE.toLogoutAllResponse(result);
    }

    public static EmailVerificationRequestCommand toCommand(EmailVerificationRequest request) {
        return INSTANCE.toEmailVerificationRequestCommand(request);
    }

    public static MessageResponse toResponse(EmailVerificationRequestResult result) {
        return INSTANCE.toEmailVerificationRequestResponse(result);
    }

    public static EmailVerificationConfirmCommand toCommand(EmailVerificationConfirmRequest request) {
        return INSTANCE.toEmailVerificationConfirmCommand(request);
    }

    public static MessageResponse toResponse(EmailVerificationConfirmResult result) {
        return INSTANCE.toEmailVerificationConfirmResponse(result);
    }

    public static ForgotPasswordCommand toCommand(ForgotPasswordRequest request) {
        return INSTANCE.toForgotPasswordCommand(request);
    }

    public static MessageResponse toResponse(ForgotPasswordResult result) {
        return INSTANCE.toForgotPasswordResponse(result);
    }

    public static ResetPasswordCommand toCommand(ResetPasswordRequest request) {
        return INSTANCE.toResetPasswordCommand(request);
    }

    public static MessageResponse toResponse(ResetPasswordResult result) {
        return INSTANCE.toResetPasswordResponse(result);
    }

    public static ChangePasswordCommand toCommand(UUID userId, ChangePasswordRequest request) {
        return INSTANCE.toChangePasswordCommand(userId, request);
    }

    public static MessageResponse toResponse(ChangePasswordResult result) {
        return INSTANCE.toChangePasswordResponse(result);
    }

    public static MeResponse toResponse(GetMeResult result) {
        return INSTANCE.toMeResponse(result);
    }

    public static SessionsResponse toResponse(GetSessionsResult result) {
        return INSTANCE.toSessionsResponse(result);
    }

    public static MessageResponse toResponse(RevokeSessionResult result) {
        return INSTANCE.toRevokeSessionResponse(result);
    }

    public static EnrollMfaResponse toResponse(EnrollMfaResult result) {
        return INSTANCE.toEnrollMfaResponse(result);
    }

    public static ConfirmMfaCommand toCommand(UUID userId, ConfirmMfaRequest request) {
        return INSTANCE.toConfirmMfaCommand(userId, request);
    }

    public static MessageResponse toResponse(ConfirmMfaResult result) {
        return INSTANCE.toConfirmMfaResponse(result);
    }

    public static DisableMfaCommand toCommand(UUID userId, DisableMfaRequest request) {
        return INSTANCE.toDisableMfaCommand(userId, request);
    }

    public static MessageResponse toResponse(DisableMfaResult result) {
        return INSTANCE.toDisableMfaResponse(result);
    }

    public static JwksResponse toResponse(GetJwksResult result) {
        return INSTANCE.toJwksResponse(result);
    }
}
