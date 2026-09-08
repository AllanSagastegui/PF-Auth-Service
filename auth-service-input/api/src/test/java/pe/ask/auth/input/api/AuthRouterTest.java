package pe.ask.auth.input.api;

import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import pe.ask.auth.core.port.in.ChangePasswordInputPort;
import pe.ask.auth.core.port.in.ConfirmMfaInputPort;
import pe.ask.auth.core.port.in.DisableMfaInputPort;
import pe.ask.auth.core.port.in.EmailVerificationConfirmInputPort;
import pe.ask.auth.core.port.in.EmailVerificationRequestInputPort;
import pe.ask.auth.core.port.in.EnrollMfaInputPort;
import pe.ask.auth.core.port.in.ForgotPasswordInputPort;
import pe.ask.auth.core.port.in.GetJwksInputPort;
import pe.ask.auth.core.port.in.GetMeInputPort;
import pe.ask.auth.core.port.in.GetSessionsInputPort;
import pe.ask.auth.core.port.in.LoginInputPort;
import pe.ask.auth.core.port.in.LogoutAllInputPort;
import pe.ask.auth.core.port.in.LogoutInputPort;
import pe.ask.auth.core.port.in.RefreshTokenInputPort;
import pe.ask.auth.core.port.in.RegisterUserInputPort;
import pe.ask.auth.core.port.in.ResetPasswordInputPort;
import pe.ask.auth.core.port.in.RevokeSessionInputPort;
import pe.ask.auth.core.port.in.VerifyMfaInputPort;
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
import pe.ask.auth.input.api.filter.ApiHeaderEnum;
import pe.ask.auth.input.api.filter.ApiPathEnum;
import pe.ask.auth.input.api.filter.CustomRequestValidator;
import pe.ask.auth.input.api.filter.TraceabilityWebFilter;
import pe.ask.auth.input.api.handler.AuthAuthenticationHandler;
import pe.ask.auth.input.api.handler.AuthJwksHandler;
import pe.ask.auth.input.api.handler.AuthMfaHandler;
import pe.ask.auth.input.api.handler.AuthPasswordHandler;
import pe.ask.auth.input.api.handler.AuthRegistrationHandler;
import pe.ask.auth.input.api.handler.AuthUserHandler;
import pe.ask.auth.input.api.router.AuthRouter;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthRouterTest {

    @Mock
    private RegisterUserInputPort registerPort;
    @Mock
    private EmailVerificationRequestInputPort emailReqPort;
    @Mock
    private EmailVerificationConfirmInputPort emailConfPort;
    @Mock
    private LoginInputPort loginPort;
    @Mock
    private VerifyMfaInputPort verifyMfaPort;
    @Mock
    private RefreshTokenInputPort refreshPort;
    @Mock
    private LogoutInputPort logoutPort;
    @Mock
    private LogoutAllInputPort logoutAllPort;
    @Mock
    private ForgotPasswordInputPort forgotPort;
    @Mock
    private ResetPasswordInputPort resetPort;
    @Mock
    private ChangePasswordInputPort changePort;
    @Mock
    private GetMeInputPort mePort;
    @Mock
    private GetSessionsInputPort sessionsPort;
    @Mock
    private RevokeSessionInputPort revokePort;
    @Mock
    private EnrollMfaInputPort enrollPort;
    @Mock
    private ConfirmMfaInputPort confirmPort;
    @Mock
    private DisableMfaInputPort disablePort;
    @Mock
    private GetJwksInputPort jwksPort;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        CustomRequestValidator validator = new CustomRequestValidator(
                Validation.buildDefaultValidatorFactory().getValidator()
        );

        AuthRegistrationHandler registrationHandler = new AuthRegistrationHandler(registerPort, emailReqPort, emailConfPort, validator);
        AuthAuthenticationHandler authenticationHandler = new AuthAuthenticationHandler(loginPort, verifyMfaPort, refreshPort, logoutPort, logoutAllPort, validator);
        AuthPasswordHandler passwordHandler = new AuthPasswordHandler(forgotPort, resetPort, changePort, validator);
        AuthUserHandler userHandler = new AuthUserHandler(mePort, sessionsPort, revokePort);
        AuthMfaHandler mfaHandler = new AuthMfaHandler(enrollPort, confirmPort, disablePort, validator);
        AuthJwksHandler jwksHandler = new AuthJwksHandler(jwksPort);

        AuthRouter authRouter = new AuthRouter(
                registrationHandler,
                authenticationHandler,
                passwordHandler,
                userHandler,
                mfaHandler,
                jwksHandler
        );

        webTestClient = WebTestClient.bindToRouterFunction(authRouter.routes())
                .webFilter(new TraceabilityWebFilter())
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/auth/register should return 201 Created on valid input with ApiResponse envelope and headers")
    void shouldRegisterUser() {
        UUID userId = UUID.randomUUID();
        when(registerPort.register(any(RegisterUserCommand.class)))
                .thenReturn(Mono.just(new RegisterUserResult(userId, "user@example.com", "Registration successful. Please verify your email.")));

        RegisterRequest req = new RegisterRequest("user@example.com", "SecurePassword123!");

        webTestClient.post()
                .uri(ApiPathEnum.REGISTER.value())
                .header(ApiHeaderEnum.CORRELATION_ID.value(), "custom-corr-id")
                .header(ApiHeaderEnum.REQUEST_ID.value(), "custom-req-id")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().valueEquals(ApiHeaderEnum.LOCATION.value(), ApiPathEnum.ME.value())
                .expectHeader().valueEquals(ApiHeaderEnum.CORRELATION_ID.value(), "custom-corr-id")
                .expectHeader().valueEquals(ApiHeaderEnum.REQUEST_ID.value(), "custom-req-id")
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.error").doesNotExist()
                .jsonPath("$.timestamp").exists()
                .jsonPath("$.data.userId").isEqualTo(userId.toString())
                .jsonPath("$.data.email").isEqualTo("user@example.com");
    }

    @Test
    @DisplayName("POST /api/v1/auth/email-verification/requests should return 200 OK with ApiResponse envelope")
    void shouldRequestEmailVerification() {
        when(emailReqPort.requestVerification(any(EmailVerificationRequestCommand.class)))
                .thenReturn(Mono.just(new EmailVerificationRequestResult("If the account exists, an email was sent")));

        EmailVerificationRequest req = new EmailVerificationRequest("user@example.com");

        webTestClient.post()
                .uri(ApiPathEnum.EMAIL_VERIFY_REQUEST.value())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists(ApiHeaderEnum.CORRELATION_ID.value())
                .expectHeader().exists(ApiHeaderEnum.REQUEST_ID.value())
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.error").doesNotExist()
                .jsonPath("$.timestamp").exists()
                .jsonPath("$.data.message").isEqualTo("If the account exists, an email was sent");
    }

    @Test
    @DisplayName("POST /api/v1/auth/email-verification/confirm should return 200 OK")
    void shouldConfirmEmailVerification() {
        when(emailConfPort.confirmVerification(any(EmailVerificationConfirmCommand.class)))
                .thenReturn(Mono.just(new EmailVerificationConfirmResult("Email verified")));

        EmailVerificationConfirmRequest req = new EmailVerificationConfirmRequest("tok-123");

        webTestClient.post()
                .uri(ApiPathEnum.EMAIL_VERIFY_CONFIRM.value())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data.message").isEqualTo("Email verified");
    }

    @Test
    @DisplayName("POST /api/v1/auth/login should return 200 OK when MFA not required with ApiResponse envelope")
    void shouldLoginSuccessfully() {
        when(loginPort.login(any(LoginCommand.class)))
                .thenReturn(Mono.just(LoginResult.success("acc_tok", "ref_tok", 900L)));

        LoginRequest req = new LoginRequest("user@example.com", "Password123!", UUID.randomUUID());

        webTestClient.post()
                .uri(ApiPathEnum.LOGIN.value())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists(ApiHeaderEnum.CORRELATION_ID.value())
                .expectHeader().exists(ApiHeaderEnum.REQUEST_ID.value())
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.error").doesNotExist()
                .jsonPath("$.timestamp").exists()
                .jsonPath("$.data.mfaRequired").isEqualTo(false)
                .jsonPath("$.data.tokens.accessToken").isEqualTo("acc_tok")
                .jsonPath("$.data.tokens.refreshToken").isEqualTo("ref_tok");
    }

    @Test
    @DisplayName("POST /api/v1/auth/login should return 202 Accepted when MFA is required with ApiResponse envelope")
    void shouldReturnAcceptedWhenMfaRequired() {
        when(loginPort.login(any(LoginCommand.class)))
                .thenReturn(Mono.just(LoginResult.mfaRequired("challenge_jwt")));

        LoginRequest req = new LoginRequest("user@example.com", "Password123!", UUID.randomUUID());

        webTestClient.post()
                .uri(ApiPathEnum.LOGIN.value())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(202)
                .expectHeader().exists(ApiHeaderEnum.CORRELATION_ID.value())
                .expectHeader().exists(ApiHeaderEnum.REQUEST_ID.value())
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.error").doesNotExist()
                .jsonPath("$.timestamp").exists()
                .jsonPath("$.data.mfaRequired").isEqualTo(true)
                .jsonPath("$.data.mfaChallengeToken").isEqualTo("challenge_jwt");
    }

    @Test
    @DisplayName("POST /api/v1/auth/mfa/verify should return 200 OK")
    void shouldVerifyMfa() {
        when(verifyMfaPort.verifyMfa(any(VerifyMfaCommand.class)))
                .thenReturn(Mono.just(new VerifyMfaResult("mfa_acc", "mfa_ref", 900L, "Bearer")));

        VerifyMfaRequest req = new VerifyMfaRequest("chal_tok", "123456", UUID.randomUUID());

        webTestClient.post()
                .uri(ApiPathEnum.VERIFY_MFA.value())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data.accessToken").isEqualTo("mfa_acc");
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh should return 200 OK with refreshed tokens and echo Idempotency-Key")
    void shouldRefreshToken() {
        when(refreshPort.refresh(any(RefreshTokenCommand.class)))
                .thenReturn(Mono.just(new RefreshTokenResult("new_acc", "new_ref", 900L, "Bearer")));

        RefreshTokenRequest req = new RefreshTokenRequest("raw_ref_tok", UUID.randomUUID());

        webTestClient.post()
                .uri(ApiPathEnum.REFRESH.value())
                .header(ApiHeaderEnum.IDEMPOTENCY_KEY.value(), "idem-uuid")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(ApiHeaderEnum.IDEMPOTENCY_KEY.value(), "idem-uuid")
                .expectHeader().exists(ApiHeaderEnum.CORRELATION_ID.value())
                .expectHeader().exists(ApiHeaderEnum.REQUEST_ID.value())
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.error").doesNotExist()
                .jsonPath("$.timestamp").exists()
                .jsonPath("$.data.accessToken").isEqualTo("new_acc")
                .jsonPath("$.data.refreshToken").isEqualTo("new_ref");
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout should return 200 OK")
    void shouldLogout() {
        when(logoutPort.logout(any(LogoutCommand.class)))
                .thenReturn(Mono.just(new LogoutResult("Successfully logged out")));

        LogoutRequest req = new LogoutRequest("raw_ref_tok", UUID.randomUUID());

        webTestClient.post()
                .uri(ApiPathEnum.LOGOUT.value())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data.message").isEqualTo("Successfully logged out");
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout-all should return 200 OK")
    void shouldLogoutAll() {
        UUID userId = UUID.randomUUID();
        when(logoutAllPort.logoutAll(any(LogoutAllCommand.class)))
                .thenReturn(Mono.just(new LogoutAllResult("All sessions revoked")));

        webTestClient.post()
                .uri(ApiPathEnum.LOGOUT_ALL.value())
                .header(ApiHeaderEnum.USER_ID.value(), userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.data.message").isEqualTo("All sessions revoked");
    }

    @Test
    @DisplayName("POST /api/v1/auth/password/forgot and /reset should return 200 OK")
    void shouldHandleForgotAndResetPassword() {
        when(forgotPort.forgotPassword(any(ForgotPasswordCommand.class)))
                .thenReturn(Mono.just(new ForgotPasswordResult("Password reset email sent")));
        when(resetPort.resetPassword(any(ResetPasswordCommand.class)))
                .thenReturn(Mono.just(new ResetPasswordResult("Password reset successfully")));

        ForgotPasswordRequest forgotReq = new ForgotPasswordRequest("user@example.com");
        webTestClient.post()
                .uri(ApiPathEnum.FORGOT_PASSWORD.value())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(forgotReq)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.message").isEqualTo("Password reset email sent");

        ResetPasswordRequest resetReq = new ResetPasswordRequest("token-xyz", "NewPassword123!");
        webTestClient.post()
                .uri(ApiPathEnum.RESET_PASSWORD.value())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(resetReq)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.message").isEqualTo("Password reset successfully");
    }

    @Test
    @DisplayName("POST /api/v1/auth/password/change should return 200 OK")
    void shouldChangePassword() {
        UUID userId = UUID.randomUUID();
        when(changePort.changePassword(any(ChangePasswordCommand.class)))
                .thenReturn(Mono.just(new ChangePasswordResult("Password changed successfully")));

        ChangePasswordRequest req = new ChangePasswordRequest("OldPassword123!", "NewPassword123!");

        webTestClient.post()
                .uri(ApiPathEnum.CHANGE_PASSWORD.value())
                .header(ApiHeaderEnum.USER_ID.value(), userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.message").isEqualTo("Password changed successfully");
    }

    @Test
    @DisplayName("GET /api/v1/auth/me, /sessions and DELETE /sessions/{sessionId}")
    void shouldHandleUserEndpoints() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        Instant now = Instant.now();

        when(mePort.getMe(any(GetMeCommand.class)))
                .thenReturn(Mono.just(new GetMeResult(userId, "user@test.com", "ACTIVE", Set.of("ROLE_USER"), false, now)));
        when(sessionsPort.getSessions(any(GetSessionsCommand.class)))
                .thenReturn(Mono.just(new GetSessionsResult(List.of(new SessionSummary(sessionId, UUID.randomUUID(), "127.0.0.1", "Agent", now, now, now)))));
        when(revokePort.revokeSession(any(RevokeSessionCommand.class)))
                .thenReturn(Mono.just(new RevokeSessionResult("Session revoked")));

        webTestClient.get()
                .uri(ApiPathEnum.ME.value())
                .header(ApiHeaderEnum.USER_ID.value(), userId.toString())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.id").isEqualTo(userId.toString());

        webTestClient.get()
                .uri(ApiPathEnum.SESSIONS.value())
                .header(ApiHeaderEnum.USER_ID.value(), userId.toString())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.sessions").isArray();

        webTestClient.delete()
                .uri(ApiPathEnum.SESSIONS.value() + "/" + sessionId)
                .header(ApiHeaderEnum.USER_ID.value(), userId.toString())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.message").isEqualTo("Session revoked");
    }

    @Test
    @DisplayName("POST /api/v1/auth/mfa/enroll, /confirm, and DELETE /mfa/disable")
    void shouldHandleMfaEndpoints() {
        UUID userId = UUID.randomUUID();

        when(enrollPort.enroll(any(EnrollMfaCommand.class)))
                .thenReturn(Mono.just(new EnrollMfaResult("mfa-secret", "otpauth://uri", List.of("c1"))));
        when(confirmPort.confirm(any(ConfirmMfaCommand.class)))
                .thenReturn(Mono.just(new ConfirmMfaResult("MFA confirmed")));
        when(disablePort.disable(any(DisableMfaCommand.class)))
                .thenReturn(Mono.just(new DisableMfaResult("MFA disabled")));

        webTestClient.post()
                .uri(ApiPathEnum.MFA_ENROLL.value())
                .header(ApiHeaderEnum.USER_ID.value(), userId.toString())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.secret").isEqualTo("mfa-secret");

        ConfirmMfaRequest confirmReq = new ConfirmMfaRequest("123456");
        webTestClient.post()
                .uri(ApiPathEnum.MFA_CONFIRM.value())
                .header(ApiHeaderEnum.USER_ID.value(), userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(confirmReq)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.message").isEqualTo("MFA confirmed");

        DisableMfaRequest disableReq = new DisableMfaRequest("Password123!", "123456");
        webTestClient.method(HttpMethod.DELETE)
                .uri(ApiPathEnum.MFA_DISABLE.value())
                .header(ApiHeaderEnum.USER_ID.value(), userId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(disableReq)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.message").isEqualTo("MFA disabled");
    }

    @Test
    @DisplayName("GET /.well-known/jwks.json should return JWKS keys")
    void shouldGetJwks() {
        when(jwksPort.getJwks(any(GetJwksCommand.class)))
                .thenReturn(Mono.just(new GetJwksResult(List.of(new JwksKeyResult("RSA", "P-256", "key-1", "sig", "RS256", "n", "e")))));

        webTestClient.get()
                .uri(ApiPathEnum.JWKS.value())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.keys").isArray();
    }
}
