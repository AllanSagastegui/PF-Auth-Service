package pe.ask.auth.input.api.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import pe.ask.auth.input.api.dto.response.JwksResponse;
import pe.ask.auth.input.api.dto.response.LoginResponse;
import pe.ask.auth.input.api.dto.response.MeResponse;
import pe.ask.auth.input.api.dto.response.MessageResponse;
import pe.ask.auth.input.api.dto.response.RegisterResponse;
import pe.ask.auth.input.api.dto.response.SessionsResponse;
import pe.ask.auth.input.api.dto.response.TokenResponse;
import pe.ask.auth.input.api.error.ApiValidationException;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("AuthApiMapper tests")
class AuthApiMapperTest {

    private final AuthApiMapper mapper = AuthApiMapper.INSTANCE;

    @Test
    @DisplayName("Register mapping")
    void testRegisterMapping() {
        RegisterRequest req = new RegisterRequest("user@test.com", "Password123!");
        RegisterUserCommand cmd = mapper.toRegisterUserCommand(req);
        assertThat(cmd.email()).isEqualTo("user@test.com");
        assertThat(cmd.password()).isEqualTo("Password123!");

        UUID id = UUID.randomUUID();
        RegisterUserResult res = new RegisterUserResult(id, "user@test.com", "Registered");
        RegisterResponse resp = mapper.toRegisterResponse(res);
        assertThat(resp.userId()).isEqualTo(id);
        assertThat(resp.email()).isEqualTo("user@test.com");

        assertThat(AuthApiMapper.toCommand(req).email()).isEqualTo("user@test.com");
        assertThat(AuthApiMapper.toResponse(res).userId()).isEqualTo(id);
    }

    @Test
    @DisplayName("Login mapping with and without deviceId")
    void testLoginMapping() {
        UUID deviceId = UUID.randomUUID();
        LoginRequest req = new LoginRequest("user@test.com", "pass", deviceId);
        LoginCommand cmd = mapper.toLoginCommand(req, "1.2.3.4", "Mozilla");
        assertThat(cmd.deviceId()).isEqualTo(deviceId);
        assertThat(cmd.ipAddress()).isEqualTo("1.2.3.4");
        assertThat(cmd.userAgent()).isEqualTo("Mozilla");

        LoginRequest req2 = new LoginRequest("user@test.com", "pass", null);
        LoginCommand cmd2 = mapper.toLoginCommand(req2, null, "");
        assertThat(cmd2.deviceId()).isNotNull();
        assertThat(cmd2.ipAddress()).isNotBlank();
        assertThat(cmd2.userAgent()).isNotBlank();

        LoginResult resSuccess = LoginResult.success("acc", "ref", 900L);
        LoginResponse respSuccess = mapper.toLoginResponse(resSuccess);
        assertThat(respSuccess.mfaRequired()).isFalse();
        assertThat(respSuccess.tokens().accessToken()).isEqualTo("acc");

        LoginResult resMfa = LoginResult.mfaRequired("challenge");
        LoginResponse respMfa = mapper.toLoginResponse(resMfa);
        assertThat(respMfa.mfaRequired()).isTrue();
        assertThat(respMfa.mfaChallengeToken()).isEqualTo("challenge");

        assertThat(AuthApiMapper.toCommand(req, "1.2.3.4", "Mozilla").email()).isEqualTo("user@test.com");
        assertThat(AuthApiMapper.toResponse(resSuccess).tokens().accessToken()).isEqualTo("acc");
    }

    @Test
    @DisplayName("VerifyMfa mapping")
    void testVerifyMfaMapping() {
        UUID deviceId = UUID.randomUUID();
        VerifyMfaRequest req = new VerifyMfaRequest("chal", "123456", deviceId);
        VerifyMfaCommand cmd = mapper.toVerifyMfaCommand(req, "1.1.1.1", "Agent");
        assertThat(cmd.mfaChallengeToken()).isEqualTo("chal");
        assertThat(cmd.totpCode()).isEqualTo("123456");

        VerifyMfaResult res = new VerifyMfaResult("acc", "ref", 900L, "Bearer");
        TokenResponse resp = mapper.toVerifyMfaResponse(res);
        assertThat(resp.accessToken()).isEqualTo("acc");

        assertThat(AuthApiMapper.toCommand(req, "1.1.1.1", "Agent").mfaChallengeToken()).isEqualTo("chal");
        assertThat(AuthApiMapper.toResponse(res).accessToken()).isEqualTo("acc");
    }

    @Test
    @DisplayName("RefreshToken mapping")
    void testRefreshTokenMapping() {
        UUID deviceId = UUID.randomUUID();
        RefreshTokenRequest req = new RefreshTokenRequest("raw_ref", deviceId);
        RefreshTokenCommand cmd = mapper.toRefreshTokenCommand(req, "idem-key", "1.1.1.1", "Agent");
        assertThat(cmd.rawRefreshToken()).isEqualTo("raw_ref");
        assertThat(cmd.idempotencyKey()).isEqualTo("idem-key");

        RefreshTokenResult res = new RefreshTokenResult("new_acc", "new_ref", 900L, "Bearer");
        TokenResponse resp = mapper.toRefreshTokenResponse(res);
        assertThat(resp.accessToken()).isEqualTo("new_acc");

        assertThat(AuthApiMapper.toCommand(req, "idem-key", "1.1.1.1", "Agent").rawRefreshToken()).isEqualTo("raw_ref");
        assertThat(AuthApiMapper.toResponse(res).accessToken()).isEqualTo("new_acc");
    }

    @Test
    @DisplayName("Logout and LogoutAll mapping")
    void testLogoutMapping() {
        UUID deviceId = UUID.randomUUID();
        LogoutRequest req = new LogoutRequest("raw_ref", deviceId);
        LogoutCommand cmd = mapper.toLogoutCommand(req);
        assertThat(cmd.rawRefreshToken()).isEqualTo("raw_ref");

        LogoutResult res = new LogoutResult("Logged out");
        MessageResponse resp = mapper.toLogoutResponse(res);
        assertThat(resp.message()).isEqualTo("Logged out");

        UUID userId = UUID.randomUUID();
        LogoutAllCommand cmdAll = mapper.toLogoutAllCommand(userId);
        assertThat(cmdAll.userId()).isEqualTo(userId);

        LogoutAllResult resAll = new LogoutAllResult("All logged out");
        MessageResponse respAll = mapper.toLogoutAllResponse(resAll);
        assertThat(respAll.message()).isEqualTo("All logged out");

        assertThat(AuthApiMapper.toCommand(req).rawRefreshToken()).isEqualTo("raw_ref");
        assertThat(AuthApiMapper.toResponse(res).message()).isEqualTo("Logged out");
        assertThat(AuthApiMapper.toCommand(userId).userId()).isEqualTo(userId);
        assertThat(AuthApiMapper.toResponse(resAll).message()).isEqualTo("All logged out");
    }

    @Test
    @DisplayName("EmailVerification and Forgot/Reset Password mapping")
    void testEmailAndResetPasswordMapping() {
        EmailVerificationRequest emailReq = new EmailVerificationRequest("user@test.com");
        EmailVerificationRequestCommand emailCmd = mapper.toEmailVerificationRequestCommand(emailReq);
        assertThat(emailCmd.email()).isEqualTo("user@test.com");
        EmailVerificationRequestResult emailRes = new EmailVerificationRequestResult("Sent");
        assertThat(mapper.toEmailVerificationRequestResponse(emailRes).message()).isEqualTo("Sent");
        assertThat(AuthApiMapper.toCommand(emailReq).email()).isEqualTo("user@test.com");
        assertThat(AuthApiMapper.toResponse(emailRes).message()).isEqualTo("Sent");

        EmailVerificationConfirmRequest confReq = new EmailVerificationConfirmRequest("tok123");
        EmailVerificationConfirmCommand confCmd = mapper.toEmailVerificationConfirmCommand(confReq);
        assertThat(confCmd.token()).isEqualTo("tok123");
        EmailVerificationConfirmResult confRes = new EmailVerificationConfirmResult("Confirmed");
        assertThat(mapper.toEmailVerificationConfirmResponse(confRes).message()).isEqualTo("Confirmed");
        assertThat(AuthApiMapper.toCommand(confReq).token()).isEqualTo("tok123");
        assertThat(AuthApiMapper.toResponse(confRes).message()).isEqualTo("Confirmed");

        ForgotPasswordRequest forgotReq = new ForgotPasswordRequest("user@test.com");
        ForgotPasswordCommand forgotCmd = mapper.toForgotPasswordCommand(forgotReq);
        assertThat(forgotCmd.email()).isEqualTo("user@test.com");
        ForgotPasswordResult forgotRes = new ForgotPasswordResult("Reset sent");
        assertThat(mapper.toForgotPasswordResponse(forgotRes).message()).isEqualTo("Reset sent");
        assertThat(AuthApiMapper.toCommand(forgotReq).email()).isEqualTo("user@test.com");
        assertThat(AuthApiMapper.toResponse(forgotRes).message()).isEqualTo("Reset sent");

        ResetPasswordRequest resetReq = new ResetPasswordRequest("tok456", "NewPassword123!");
        ResetPasswordCommand resetCmd = mapper.toResetPasswordCommand(resetReq);
        assertThat(resetCmd.token()).isEqualTo("tok456");
        ResetPasswordResult resetRes = new ResetPasswordResult("Password reset");
        assertThat(mapper.toResetPasswordResponse(resetRes).message()).isEqualTo("Password reset");
        assertThat(AuthApiMapper.toCommand(resetReq).token()).isEqualTo("tok456");
        assertThat(AuthApiMapper.toResponse(resetRes).message()).isEqualTo("Password reset");
    }

    @Test
    @DisplayName("ChangePassword, GetMe, Sessions, and Revoke mapping")
    void testUserOperationsMapping() {
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();
        ChangePasswordRequest changeReq = new ChangePasswordRequest("Old123!", "New123!");
        ChangePasswordCommand changeCmd = mapper.toChangePasswordCommand(userId, changeReq);
        assertThat(changeCmd.userId()).isEqualTo(userId);
        assertThat(changeCmd.newPassword()).isEqualTo("New123!");
        ChangePasswordResult changeRes = new ChangePasswordResult("Changed");
        assertThat(mapper.toChangePasswordResponse(changeRes).message()).isEqualTo("Changed");
        assertThat(AuthApiMapper.toCommand(userId, changeReq).userId()).isEqualTo(userId);
        assertThat(AuthApiMapper.toResponse(changeRes).message()).isEqualTo("Changed");

        GetMeCommand meCmd = mapper.toGetMeCommand(userId);
        assertThat(meCmd.userId()).isEqualTo(userId);
        GetMeResult meRes = new GetMeResult(userId, "user@test.com", "ACTIVE", Set.of("ROLE_USER"), true, now);
        MeResponse meResp = mapper.toMeResponse(meRes);
        assertThat(meResp.id()).isEqualTo(userId);
        assertThat(AuthApiMapper.toResponse(meRes).email()).isEqualTo("user@test.com");

        GetSessionsCommand sessCmd = mapper.toGetSessionsCommand(userId);
        assertThat(sessCmd.userId()).isEqualTo(userId);
        UUID sessionId = UUID.randomUUID();
        SessionSummary summary = new SessionSummary(sessionId, UUID.randomUUID(), "1.1.1.1", "Agent", now, now, now);
        GetSessionsResult sessRes = new GetSessionsResult(List.of(summary));
        SessionsResponse sessResp = mapper.toSessionsResponse(sessRes);
        assertThat(sessResp.sessions()).hasSize(1);
        assertThat(AuthApiMapper.toResponse(sessRes).sessions()).hasSize(1);

        RevokeSessionCommand revCmd = mapper.toRevokeSessionCommand(userId, sessionId);
        assertThat(revCmd.userId()).isEqualTo(userId);
        assertThat(revCmd.sessionId()).isEqualTo(sessionId);
        RevokeSessionResult revRes = new RevokeSessionResult("Revoked");
        assertThat(mapper.toRevokeSessionResponse(revRes).message()).isEqualTo("Revoked");
        assertThat(AuthApiMapper.toResponse(revRes).message()).isEqualTo("Revoked");
    }

    @Test
    @DisplayName("MFA Enroll, Confirm, Disable and JWKS mapping")
    void testMfaAndJwksMapping() {
        UUID userId = UUID.randomUUID();
        EnrollMfaCommand enrollCmd = mapper.toEnrollMfaCommand(userId);
        assertThat(enrollCmd.userId()).isEqualTo(userId);
        EnrollMfaResult enrollRes = new EnrollMfaResult("secret123", "otpauth://uri", List.of("c1", "c2"));
        EnrollMfaResponse enrollResp = mapper.toEnrollMfaResponse(enrollRes);
        assertThat(enrollResp.secret()).isEqualTo("secret123");
        assertThat(AuthApiMapper.toResponse(enrollRes).qrCodeUri()).isEqualTo("otpauth://uri");

        ConfirmMfaRequest confirmReq = new ConfirmMfaRequest("123456");
        ConfirmMfaCommand confirmCmd = mapper.toConfirmMfaCommand(userId, confirmReq);
        assertThat(confirmCmd.userId()).isEqualTo(userId);
        ConfirmMfaResult confirmRes = new ConfirmMfaResult("Confirmed");
        assertThat(mapper.toConfirmMfaResponse(confirmRes).message()).isEqualTo("Confirmed");
        assertThat(AuthApiMapper.toCommand(userId, confirmReq).userId()).isEqualTo(userId);
        assertThat(AuthApiMapper.toResponse(confirmRes).message()).isEqualTo("Confirmed");

        DisableMfaRequest disableReq = new DisableMfaRequest("Password123!", "123456");
        DisableMfaCommand disableCmd = mapper.toDisableMfaCommand(userId, disableReq);
        assertThat(disableCmd.userId()).isEqualTo(userId);
        DisableMfaResult disableRes = new DisableMfaResult("Disabled");
        assertThat(mapper.toDisableMfaResponse(disableRes).message()).isEqualTo("Disabled");
        assertThat(AuthApiMapper.toCommand(userId, disableReq).userId()).isEqualTo(userId);
        assertThat(AuthApiMapper.toResponse(disableRes).message()).isEqualTo("Disabled");

        GetJwksCommand jwksCmd = mapper.toGetJwksCommand();
        assertThat(jwksCmd).isNotNull();
        JwksKeyResult keyResult = new JwksKeyResult("RSA", "P-256", "key-1", "sig", "RS256", "n", "e");
        GetJwksResult jwksRes = new GetJwksResult(List.of(keyResult));
        JwksResponse jwksResp = mapper.toJwksResponse(jwksRes);
        assertThat(jwksResp.keys()).hasSize(1);
        assertThat(AuthApiMapper.toResponse(jwksRes).keys()).hasSize(1);
    }

    @Test
    @DisplayName("Validation null argument checks for Auth and Tokens")
    void testAuthAndTokensNullValidations() {
        assertThrows(ApiValidationException.class, () -> mapper.toRegisterUserCommand(null));
        assertThrows(ApiValidationException.class, () -> mapper.toRegisterResponse(null));
        assertThrows(ApiValidationException.class, () -> mapper.toLoginCommand(null, "ip", "agent"));
        assertThrows(ApiValidationException.class, () -> mapper.toLoginResponse(null));
        assertThrows(ApiValidationException.class, () -> mapper.toVerifyMfaCommand(null, "ip", "agent"));
        assertThrows(ApiValidationException.class, () -> mapper.toVerifyMfaResponse(null));
        assertThrows(ApiValidationException.class, () -> mapper.toRefreshTokenCommand(null, "idem", "ip", "agent"));
        assertThrows(ApiValidationException.class, () -> mapper.toRefreshTokenResponse(null));
        assertThrows(ApiValidationException.class, () -> mapper.toLogoutCommand(null));
        assertThrows(ApiValidationException.class, () -> mapper.toLogoutResponse(null));
        assertThrows(ApiValidationException.class, () -> mapper.toLogoutAllCommand(null));
        assertThrows(ApiValidationException.class, () -> mapper.toLogoutAllResponse(null));
        assertThrows(ApiValidationException.class, () -> mapper.toEmailVerificationRequestCommand(null));
        assertThrows(ApiValidationException.class, () -> mapper.toEmailVerificationRequestResponse(null));
        assertThrows(ApiValidationException.class, () -> mapper.toEmailVerificationConfirmCommand(null));
        assertThrows(ApiValidationException.class, () -> mapper.toEmailVerificationConfirmResponse(null));
        assertThrows(ApiValidationException.class, () -> mapper.toForgotPasswordCommand(null));
        assertThrows(ApiValidationException.class, () -> mapper.toForgotPasswordResponse(null));
        assertThrows(ApiValidationException.class, () -> mapper.toResetPasswordCommand(null));
        assertThrows(ApiValidationException.class, () -> mapper.toResetPasswordResponse(null));
    }

    @Test
    @DisplayName("Validation null argument checks for User and MFA")
    void testUserAndMfaNullValidations() {
        ChangePasswordRequest changeReq = new ChangePasswordRequest("a", "b");
        UUID randId = UUID.randomUUID();
        ConfirmMfaRequest confirmReq = new ConfirmMfaRequest("123");
        DisableMfaRequest disableReq = new DisableMfaRequest("a", "b");

        assertThrows(ApiValidationException.class, () -> mapper.toChangePasswordCommand(null, changeReq));
        assertThrows(ApiValidationException.class, () -> mapper.toChangePasswordCommand(randId, null));
        assertThrows(ApiValidationException.class, () -> mapper.toChangePasswordResponse(null));
        assertThrows(ApiValidationException.class, () -> mapper.toGetMeCommand(null));
        assertThrows(ApiValidationException.class, () -> mapper.toMeResponse(null));
        assertThrows(ApiValidationException.class, () -> mapper.toGetSessionsCommand(null));
        assertThrows(ApiValidationException.class, () -> mapper.toSessionsResponse(null));
        assertThrows(ApiValidationException.class, () -> mapper.toRevokeSessionCommand(null, randId));
        assertThrows(ApiValidationException.class, () -> mapper.toRevokeSessionCommand(randId, null));
        assertThrows(ApiValidationException.class, () -> mapper.toRevokeSessionResponse(null));
        assertThrows(ApiValidationException.class, () -> mapper.toEnrollMfaCommand(null));
        assertThrows(ApiValidationException.class, () -> mapper.toEnrollMfaResponse(null));
        assertThrows(ApiValidationException.class, () -> mapper.toConfirmMfaCommand(null, confirmReq));
        assertThrows(ApiValidationException.class, () -> mapper.toConfirmMfaCommand(randId, null));
        assertThrows(ApiValidationException.class, () -> mapper.toConfirmMfaResponse(null));
        assertThrows(ApiValidationException.class, () -> mapper.toDisableMfaCommand(null, disableReq));
        assertThrows(ApiValidationException.class, () -> mapper.toDisableMfaCommand(randId, null));
        assertThrows(ApiValidationException.class, () -> mapper.toDisableMfaResponse(null));
        assertThrows(ApiValidationException.class, () -> mapper.toJwksResponse(null));
    }
}
