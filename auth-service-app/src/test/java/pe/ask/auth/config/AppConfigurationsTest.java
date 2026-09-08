package pe.ask.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.ECKey;
import io.r2dbc.spi.ConnectionFactory;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import pe.ask.auth.config.properties.AuthKafkaProperties;
import pe.ask.auth.config.properties.AuthOpenApiProperties;
import pe.ask.auth.config.properties.AuthSecurityProperties;
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
import pe.ask.auth.core.port.out.ClockOutputPort;
import pe.ask.auth.core.port.out.IdGeneratorOutputPort;
import pe.ask.auth.core.port.out.IdempotencyOutputPort;
import pe.ask.auth.core.port.out.JwksOutputPort;
import pe.ask.auth.core.port.out.NotificationOutputPort;
import pe.ask.auth.core.port.out.OneTimeTokenRepositoryOutputPort;
import pe.ask.auth.core.port.out.OutboxRepositoryOutputPort;
import pe.ask.auth.core.port.out.PasswordHasherOutputPort;
import pe.ask.auth.core.port.out.RefreshTokenRepositoryOutputPort;
import pe.ask.auth.core.port.out.SecurityAuditOutputPort;
import pe.ask.auth.core.port.out.SessionRepositoryOutputPort;
import pe.ask.auth.core.port.out.TokenGeneratorOutputPort;
import pe.ask.auth.core.port.out.TotpOutputPort;
import pe.ask.auth.core.port.out.UserRepositoryOutputPort;
import pe.ask.auth.input.api.filter.CustomRequestValidator;
import pe.ask.auth.input.api.filter.TraceabilityWebFilter;
import pe.ask.auth.input.api.handler.AuthAuthenticationHandler;
import pe.ask.auth.input.api.handler.AuthJwksHandler;
import pe.ask.auth.input.api.handler.AuthMfaHandler;
import pe.ask.auth.input.api.handler.AuthPasswordHandler;
import pe.ask.auth.input.api.handler.AuthRegistrationHandler;
import pe.ask.auth.input.api.handler.AuthUserHandler;
import pe.ask.auth.input.api.mapper.AuthApiMapper;
import pe.ask.auth.input.api.router.AuthRouter;
import pe.ask.auth.output.database.repository.OneTimeTokenR2dbcRepository;
import pe.ask.auth.output.database.repository.OutboxMessageR2dbcRepository;
import pe.ask.auth.output.database.repository.RefreshTokenR2dbcRepository;
import pe.ask.auth.output.database.repository.SessionR2dbcRepository;
import pe.ask.auth.output.database.repository.UserR2dbcRepository;
import reactor.kafka.sender.KafkaSender;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("App Configuration and Properties tests")
class AppConfigurationsTest {

    @Test
    @DisplayName("Test CorePropertiesConfiguration")
    void testCorePropertiesConfiguration() {
        CorePropertiesConfiguration config = new CorePropertiesConfiguration();
        assertThat(config).isNotNull();
    }

    @Test
    @DisplayName("Test DatabaseInitializerConfiguration")
    void testDatabaseInitializerConfiguration() {
        DatabaseInitializerConfiguration config = new DatabaseInitializerConfiguration();
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
        ConnectionFactoryInitializer initializer = config.connectionFactoryInitializer(connectionFactory);
        assertThat(initializer).isNotNull();
    }

    @Test
    @DisplayName("Test DatabaseOutputConfiguration")
    void testDatabaseOutputConfiguration() {
        DatabaseOutputConfiguration config = new DatabaseOutputConfiguration();
        UserR2dbcRepository userRepo = mock(UserR2dbcRepository.class);
        SessionR2dbcRepository sessionRepo = mock(SessionR2dbcRepository.class);
        RefreshTokenR2dbcRepository refreshRepo = mock(RefreshTokenR2dbcRepository.class);
        OneTimeTokenR2dbcRepository ottRepo = mock(OneTimeTokenR2dbcRepository.class);
        OutboxMessageR2dbcRepository outboxRepo = mock(OutboxMessageR2dbcRepository.class);

        assertThat(config.userRepositoryOutputPort(userRepo)).isNotNull();
        assertThat(config.sessionRepositoryOutputPort(sessionRepo)).isNotNull();
        assertThat(config.refreshTokenRepositoryOutputPort(refreshRepo)).isNotNull();
        assertThat(config.oneTimeTokenRepositoryOutputPort(ottRepo)).isNotNull();
        assertThat(config.outboxRepositoryOutputPort(outboxRepo)).isNotNull();
    }

    @Test
    @DisplayName("Test KafkaOutputConfiguration")
    void testKafkaOutputConfiguration() {
        KafkaOutputConfiguration config = new KafkaOutputConfiguration();
        AuthKafkaProperties props = new AuthKafkaProperties("localhost:9092", "notif-topic", "audit-topic");

        @SuppressWarnings("unchecked")
        KafkaSender<String, String> sender = mock(KafkaSender.class);

        assertThat(config.kafkaSender(props)).isNotNull();
        assertThat(config.kafkaSender(null)).isNotNull();

        ObjectMapper mapper = config.objectMapper();
        assertThat(mapper).isNotNull();

        assertThat(config.notificationOutputPort(sender, props)).isNotNull();
        assertThat(config.notificationOutputPort(sender, null)).isNotNull();
        assertThat(config.securityAuditOutputPort(sender, props)).isNotNull();
        assertThat(config.securityAuditOutputPort(sender, null)).isNotNull();
    }

    @Test
    @DisplayName("Test SecurityOutputConfiguration")
    void testSecurityOutputConfiguration() throws Exception {
        SecurityOutputConfiguration config = new SecurityOutputConfiguration();
        AuthSecurityProperties.Argon2Properties argonProps = new AuthSecurityProperties.Argon2Properties(16, 32, 1, 19456, 2, 4, "dummyPass123!");
        AuthSecurityProperties.JwtProperties jwtProps = new AuthSecurityProperties.JwtProperties("pe.ask.auth", Duration.ofMinutes(15), Duration.ofDays(7));
        AuthSecurityProperties.TotpProperties totpProps = new AuthSecurityProperties.TotpProperties("AskPlatform", "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");
        AuthSecurityProperties.RateLimitProperties rateProps = new AuthSecurityProperties.RateLimitProperties(10, Duration.ofMinutes(1));
        AuthSecurityProperties secProps = new AuthSecurityProperties(argonProps, jwtProps, totpProps, rateProps);

        assertThat(config.clockOutputPort()).isNotNull();
        assertThat(config.idGeneratorOutputPort()).isNotNull();
        assertThat(config.passwordHasherOutputPort(secProps)).isNotNull();
        assertThat(config.passwordHasherOutputPort(new AuthSecurityProperties(null, null, null, null))).isNotNull();

        ECKey ecKey = config.ecKey();
        assertThat(ecKey).isNotNull();
        assertThat(config.tokenGeneratorOutputPort(ecKey)).isNotNull();
        assertThat(config.jwksOutputPort(ecKey)).isNotNull();
        assertThat(config.totpOutputPort(secProps)).isNotNull();
        assertThat(config.totpOutputPort(new AuthSecurityProperties(null, null, null, null))).isNotNull();
        assertThat(config.idempotencyOutputPort()).isNotNull();
        assertThat(config.rateLimiterOutputPort()).isNotNull();
    }

    @Test
    @DisplayName("Test WebFluxInputConfiguration")
    void testWebFluxInputConfiguration() {
        WebFluxInputConfiguration config = new WebFluxInputConfiguration();
        Validator validator = config.validator();
        assertThat(validator).isNotNull();
        CustomRequestValidator customValidator = config.customRequestValidator(validator);
        assertThat(customValidator).isNotNull();
        AuthApiMapper mapper = config.authApiMapper();
        assertThat(mapper).isNotNull();
        TraceabilityWebFilter filter = config.traceabilityWebFilter();
        assertThat(filter).isNotNull();

        RegisterUserInputPort reg = mock(RegisterUserInputPort.class);
        EmailVerificationRequestInputPort evReq = mock(EmailVerificationRequestInputPort.class);
        EmailVerificationConfirmInputPort evConf = mock(EmailVerificationConfirmInputPort.class);
        LoginInputPort login = mock(LoginInputPort.class);
        VerifyMfaInputPort verifyMfa = mock(VerifyMfaInputPort.class);
        RefreshTokenInputPort refresh = mock(RefreshTokenInputPort.class);
        LogoutInputPort logout = mock(LogoutInputPort.class);
        LogoutAllInputPort logoutAll = mock(LogoutAllInputPort.class);
        ForgotPasswordInputPort forgot = mock(ForgotPasswordInputPort.class);
        ResetPasswordInputPort reset = mock(ResetPasswordInputPort.class);
        ChangePasswordInputPort change = mock(ChangePasswordInputPort.class);
        GetMeInputPort me = mock(GetMeInputPort.class);
        GetSessionsInputPort sessions = mock(GetSessionsInputPort.class);
        RevokeSessionInputPort revoke = mock(RevokeSessionInputPort.class);
        EnrollMfaInputPort enroll = mock(EnrollMfaInputPort.class);
        ConfirmMfaInputPort confirm = mock(ConfirmMfaInputPort.class);
        DisableMfaInputPort disable = mock(DisableMfaInputPort.class);
        GetJwksInputPort jwks = mock(GetJwksInputPort.class);

        AuthRegistrationHandler regHandler = config.authRegistrationHandler(reg, evReq, evConf, customValidator, mapper);
        assertThat(regHandler).isNotNull();
        AuthAuthenticationHandler authHandler = config.authAuthenticationHandler(login, verifyMfa, refresh, logout, logoutAll, customValidator, mapper);
        assertThat(authHandler).isNotNull();
        AuthPasswordHandler passHandler = config.authPasswordHandler(forgot, reset, change, customValidator, mapper);
        assertThat(passHandler).isNotNull();
        AuthUserHandler userHandler = config.authUserHandler(me, sessions, revoke, mapper);
        assertThat(userHandler).isNotNull();
        AuthMfaHandler mfaHandler = config.authMfaHandler(enroll, confirm, disable, customValidator, mapper);
        assertThat(mfaHandler).isNotNull();
        AuthJwksHandler jwksHandler = config.authJwksHandler(jwks, mapper);
        assertThat(jwksHandler).isNotNull();

        AuthRouter router = config.authRouter(regHandler, authHandler, passHandler, userHandler, mfaHandler, jwksHandler);
        assertThat(router).isNotNull();
        RouterFunction<ServerResponse> routes = config.authRoutes(router);
        assertThat(routes).isNotNull();
    }

    @Test
    @DisplayName("Test UseCaseConfiguration")
    void testUseCaseConfiguration() {
        UseCaseConfiguration config = new UseCaseConfiguration();

        UserRepositoryOutputPort userRepo = mock(UserRepositoryOutputPort.class);
        SessionRepositoryOutputPort sessionRepo = mock(SessionRepositoryOutputPort.class);
        RefreshTokenRepositoryOutputPort refreshRepo = mock(RefreshTokenRepositoryOutputPort.class);
        OneTimeTokenRepositoryOutputPort ottRepo = mock(OneTimeTokenRepositoryOutputPort.class);
        OutboxRepositoryOutputPort outboxRepo = mock(OutboxRepositoryOutputPort.class);
        PasswordHasherOutputPort hasher = mock(PasswordHasherOutputPort.class);
        TokenGeneratorOutputPort tokenGen = mock(TokenGeneratorOutputPort.class);
        NotificationOutputPort notif = mock(NotificationOutputPort.class);
        SecurityAuditOutputPort audit = mock(SecurityAuditOutputPort.class);
        TotpOutputPort totp = mock(TotpOutputPort.class);
        JwksOutputPort jwks = mock(JwksOutputPort.class);
        IdempotencyOutputPort idempotency = mock(IdempotencyOutputPort.class);
        ClockOutputPort clock = mock(ClockOutputPort.class);
        IdGeneratorOutputPort idGen = mock(IdGeneratorOutputPort.class);

        assertThat(config.registerUserInputPort(userRepo, ottRepo, outboxRepo, hasher, tokenGen, notif, clock, idGen)).isNotNull();
        assertThat(config.emailVerificationRequestInputPort(userRepo, ottRepo, tokenGen, notif, clock, idGen)).isNotNull();
        assertThat(config.emailVerificationConfirmInputPort(userRepo, ottRepo, tokenGen, clock)).isNotNull();
        assertThat(config.loginInputPort(userRepo, sessionRepo, refreshRepo, hasher, tokenGen, clock, idGen)).isNotNull();
        assertThat(config.verifyMfaInputPort(userRepo, sessionRepo, refreshRepo, tokenGen, totp, clock, idGen)).isNotNull();
        assertThat(config.refreshTokenInputPort(userRepo, sessionRepo, refreshRepo, tokenGen, idempotency, audit, clock, idGen)).isNotNull();
        assertThat(config.logoutInputPort(sessionRepo, refreshRepo, tokenGen, clock)).isNotNull();
        assertThat(config.logoutAllInputPort(userRepo, sessionRepo, clock)).isNotNull();
        assertThat(config.forgotPasswordInputPort(userRepo, ottRepo, tokenGen, notif, clock, idGen)).isNotNull();
        assertThat(config.resetPasswordInputPort(userRepo, sessionRepo, ottRepo, hasher, tokenGen, clock)).isNotNull();
        assertThat(config.changePasswordInputPort(userRepo, hasher, clock)).isNotNull();
        assertThat(config.getMeInputPort(userRepo)).isNotNull();
        assertThat(config.getSessionsInputPort(sessionRepo)).isNotNull();
        assertThat(config.revokeSessionInputPort(sessionRepo, refreshRepo, clock)).isNotNull();
        assertThat(config.enrollMfaInputPort(userRepo, totp, tokenGen, clock)).isNotNull();
        assertThat(config.confirmMfaInputPort(userRepo, totp, clock)).isNotNull();
        assertThat(config.disableMfaInputPort(userRepo, hasher, totp, clock)).isNotNull();
        assertThat(config.getJwksInputPort(jwks)).isNotNull();
    }

    @Test
    @DisplayName("Test Kafka Configuration Properties")
    void testKafkaConfigurationProperties() {
        AuthKafkaProperties kafkaProps = new AuthKafkaProperties("localhost:9092", "notif", "audit");
        assertThat(kafkaProps.bootstrapServers()).isEqualTo("localhost:9092");
        assertThat(kafkaProps.notificationsTopic()).isEqualTo("notif");
        assertThat(kafkaProps.auditTopic()).isEqualTo("audit");
    }

    @Test
    @DisplayName("Test OpenAPI Configuration Properties")
    void testOpenApiConfigurationProperties() {
        AuthOpenApiProperties openApiProps = new AuthOpenApiProperties(
                "Title", "Desc", "1.0.0", "Name", "a@b.com",
                "MIT", "http://mit", "bearer", "JWT", "BearerAuth", "JWT Token"
        );
        assertThat(openApiProps.title()).isEqualTo("Title");
        assertThat(openApiProps.description()).isEqualTo("Desc");
        assertThat(openApiProps.version()).isEqualTo("1.0.0");
        assertThat(openApiProps.contactName()).isEqualTo("Name");
        assertThat(openApiProps.contactEmail()).isEqualTo("a@b.com");
        assertThat(openApiProps.licenseName()).isEqualTo("MIT");
        assertThat(openApiProps.licenseUrl()).isEqualTo("http://mit");
        assertThat(openApiProps.schemeBearer()).isEqualTo("bearer");
        assertThat(openApiProps.bearerFormat()).isEqualTo("JWT");
        assertThat(openApiProps.schemeNameBearer()).isEqualTo("BearerAuth");
        assertThat(openApiProps.schemeDescription()).isEqualTo("JWT Token");

        AuthOpenApiProperties defaultOpenApiProps = new AuthOpenApiProperties(
                null, "", "  ", null, null, null, null, null, null, null, null
        );
        assertThat(defaultOpenApiProps.title()).isNotBlank();
    }

    @Test
    @DisplayName("Test Security Configuration Properties")
    void testSecurityConfigurationProperties() {
        AuthSecurityProperties.Argon2Properties argon = new AuthSecurityProperties.Argon2Properties(16, 32, 1, 19456, 2, 4, "dummy");
        assertThat(argon.saltLength()).isEqualTo(16);
        assertThat(argon.hashLength()).isEqualTo(32);
        assertThat(argon.parallelism()).isEqualTo(1);
        assertThat(argon.memoryKib()).isEqualTo(19456);
        assertThat(argon.iterations()).isEqualTo(2);
        assertThat(argon.schedulerThreads()).isEqualTo(4);
        assertThat(argon.dummyPassword()).isEqualTo("dummy");

        AuthSecurityProperties.Argon2Properties defaultArgon = new AuthSecurityProperties.Argon2Properties(0, 0, 0, 0, 0, 0, null);
        assertThat(defaultArgon.saltLength()).isEqualTo(16);
        assertThat(defaultArgon.dummyPassword()).isNotBlank();

        AuthSecurityProperties.JwtProperties jwt = new AuthSecurityProperties.JwtProperties("pe.ask.auth", Duration.ofMinutes(15), Duration.ofDays(7));
        assertThat(jwt.issuer()).isEqualTo("pe.ask.auth");
        assertThat(jwt.accessTokenTtl()).isEqualTo(Duration.ofMinutes(15));
        assertThat(jwt.refreshTokenTtl()).isEqualTo(Duration.ofDays(7));

        AuthSecurityProperties.JwtProperties defaultJwt = new AuthSecurityProperties.JwtProperties(null, null, null);
        assertThat(defaultJwt.issuer()).isEqualTo("pe.ask.auth");

        AuthSecurityProperties.TotpProperties totp = new AuthSecurityProperties.TotpProperties("key", "hex");
        assertThat(totp.issuer()).isEqualTo("key");
        assertThat(totp.encryptionKeyHex()).isEqualTo("hex");

        AuthSecurityProperties.TotpProperties defaultTotp = new AuthSecurityProperties.TotpProperties(null, null);
        assertThat(defaultTotp.issuer()).isEqualTo("AskPlatform");

        AuthSecurityProperties.RateLimitProperties rate = new AuthSecurityProperties.RateLimitProperties(10, Duration.ofMinutes(1));
        assertThat(rate.maxRequests()).isEqualTo(10);
        assertThat(rate.window()).isEqualTo(Duration.ofMinutes(1));

        AuthSecurityProperties.RateLimitProperties defaultRate = new AuthSecurityProperties.RateLimitProperties(0, null);
        assertThat(defaultRate.maxRequests()).isEqualTo(100);
        assertThat(defaultRate.window()).isEqualTo(Duration.ofMinutes(1));
    }
}
