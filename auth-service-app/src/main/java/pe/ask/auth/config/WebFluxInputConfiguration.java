package pe.ask.auth.config;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
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

@Configuration(proxyBeanMethods = false)
public final class WebFluxInputConfiguration {

    @Bean
    public Validator validator() {
        return Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Bean
    public CustomRequestValidator customRequestValidator(Validator validator) {
        return new CustomRequestValidator(validator);
    }

    @Bean
    public AuthApiMapper authApiMapper() {
        return AuthApiMapper.INSTANCE;
    }

    @Bean
    public TraceabilityWebFilter traceabilityWebFilter() {
        return new TraceabilityWebFilter();
    }

    @Bean
    public AuthRegistrationHandler authRegistrationHandler(
            RegisterUserInputPort registerPort,
            EmailVerificationRequestInputPort emailReqPort,
            EmailVerificationConfirmInputPort emailConfPort,
            CustomRequestValidator validator,
            AuthApiMapper mapper
    ) {
        return new AuthRegistrationHandler(registerPort, emailReqPort, emailConfPort, validator, mapper);
    }

    @Bean
    public AuthAuthenticationHandler authAuthenticationHandler(
            LoginInputPort loginPort,
            VerifyMfaInputPort verifyMfaPort,
            RefreshTokenInputPort refreshPort,
            LogoutInputPort logoutPort,
            LogoutAllInputPort logoutAllPort,
            CustomRequestValidator validator,
            AuthApiMapper mapper
    ) {
        return new AuthAuthenticationHandler(loginPort, verifyMfaPort, refreshPort, logoutPort, logoutAllPort, validator, mapper);
    }

    @Bean
    public AuthPasswordHandler authPasswordHandler(
            ForgotPasswordInputPort forgotPort,
            ResetPasswordInputPort resetPort,
            ChangePasswordInputPort changePort,
            CustomRequestValidator validator,
            AuthApiMapper mapper
    ) {
        return new AuthPasswordHandler(forgotPort, resetPort, changePort, validator, mapper);
    }

    @Bean
    public AuthUserHandler authUserHandler(
            GetMeInputPort mePort,
            GetSessionsInputPort sessionsPort,
            RevokeSessionInputPort revokePort,
            AuthApiMapper mapper
    ) {
        return new AuthUserHandler(mePort, sessionsPort, revokePort, mapper);
    }

    @Bean
    public AuthMfaHandler authMfaHandler(
            EnrollMfaInputPort enrollPort,
            ConfirmMfaInputPort confirmPort,
            DisableMfaInputPort disablePort,
            CustomRequestValidator validator,
            AuthApiMapper mapper
    ) {
        return new AuthMfaHandler(enrollPort, confirmPort, disablePort, validator, mapper);
    }

    @Bean
    public AuthJwksHandler authJwksHandler(GetJwksInputPort jwksPort, AuthApiMapper mapper) {
        return new AuthJwksHandler(jwksPort, mapper);
    }

    @Bean
    public AuthRouter authRouter(
            AuthRegistrationHandler registrationHandler,
            AuthAuthenticationHandler authenticationHandler,
            AuthPasswordHandler passwordHandler,
            AuthUserHandler userHandler,
            AuthMfaHandler mfaHandler,
            AuthJwksHandler jwksHandler
    ) {
        return new AuthRouter(
                registrationHandler,
                authenticationHandler,
                passwordHandler,
                userHandler,
                mfaHandler,
                jwksHandler
        );
    }

    @Bean
    public RouterFunction<ServerResponse> authRoutes(AuthRouter authRouter) {
        return authRouter.routes();
    }
}
