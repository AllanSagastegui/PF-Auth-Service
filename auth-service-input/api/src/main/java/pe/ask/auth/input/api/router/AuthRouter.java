package pe.ask.auth.input.api.router;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import pe.ask.auth.input.api.filter.ApiMessageEnum;
import pe.ask.auth.input.api.filter.ApiPathEnum;
import pe.ask.auth.input.api.filter.ApiValidation;
import pe.ask.auth.input.api.handler.AuthAuthenticationHandler;
import pe.ask.auth.input.api.handler.AuthJwksHandler;
import pe.ask.auth.input.api.handler.AuthMfaHandler;
import pe.ask.auth.input.api.handler.AuthPasswordHandler;
import pe.ask.auth.input.api.handler.AuthRegistrationHandler;
import pe.ask.auth.input.api.handler.AuthUserHandler;

public final class AuthRouter {

    private final AuthRegistrationHandler registrationHandler;
    private final AuthAuthenticationHandler authenticationHandler;
    private final AuthPasswordHandler passwordHandler;
    private final AuthUserHandler userHandler;
    private final AuthMfaHandler mfaHandler;
    private final AuthJwksHandler jwksHandler;

    public AuthRouter(
            AuthRegistrationHandler registrationHandler,
            AuthAuthenticationHandler authenticationHandler,
            AuthPasswordHandler passwordHandler,
            AuthUserHandler userHandler,
            AuthMfaHandler mfaHandler,
            AuthJwksHandler jwksHandler
    ) {
        this.registrationHandler = ApiValidation.requireNonNull(registrationHandler, ApiMessageEnum.PARAM_ARGUMENT.value());
        this.authenticationHandler = ApiValidation.requireNonNull(authenticationHandler, ApiMessageEnum.PARAM_ARGUMENT.value());
        this.passwordHandler = ApiValidation.requireNonNull(passwordHandler, ApiMessageEnum.PARAM_ARGUMENT.value());
        this.userHandler = ApiValidation.requireNonNull(userHandler, ApiMessageEnum.PARAM_ARGUMENT.value());
        this.mfaHandler = ApiValidation.requireNonNull(mfaHandler, ApiMessageEnum.PARAM_ARGUMENT.value());
        this.jwksHandler = ApiValidation.requireNonNull(jwksHandler, ApiMessageEnum.PARAM_ARGUMENT.value());
    }

    public RouterFunction<ServerResponse> routes() {
        return RouterFunctions.route()
                .path(ApiPathEnum.AUTH_PREFIX.value(), builder -> builder
                        .nest(RequestPredicates.accept(MediaType.APPLICATION_JSON), b -> b
                                .POST(ApiPathEnum.SUB_REGISTER.value(), registrationHandler::register)
                                .POST(ApiPathEnum.SUB_EMAIL_VERIFY_REQUEST.value(), registrationHandler::requestEmailVerification)
                                .POST(ApiPathEnum.SUB_EMAIL_VERIFY_CONFIRM.value(), registrationHandler::confirmEmailVerification)
                                .POST(ApiPathEnum.SUB_LOGIN.value(), authenticationHandler::login)
                                .POST(ApiPathEnum.SUB_VERIFY_MFA.value(), authenticationHandler::verifyMfa)
                                .POST(ApiPathEnum.SUB_REFRESH.value(), authenticationHandler::refresh)
                                .POST(ApiPathEnum.SUB_LOGOUT.value(), authenticationHandler::logout)
                                .POST(ApiPathEnum.SUB_LOGOUT_ALL.value(), authenticationHandler::logoutAll)
                                .POST(ApiPathEnum.SUB_FORGOT_PASSWORD.value(), passwordHandler::forgotPassword)
                                .POST(ApiPathEnum.SUB_RESET_PASSWORD.value(), passwordHandler::resetPassword)
                                .POST(ApiPathEnum.SUB_CHANGE_PASSWORD.value(), passwordHandler::changePassword)
                                .GET(ApiPathEnum.SUB_ME.value(), userHandler::getMe)
                                .GET(ApiPathEnum.SUB_SESSIONS.value(), userHandler::getSessions)
                                .DELETE(ApiPathEnum.SUB_SESSION_BY_ID.value(), userHandler::revokeSession)
                                .POST(ApiPathEnum.SUB_MFA_ENROLL.value(), mfaHandler::enroll)
                                .POST(ApiPathEnum.SUB_MFA_CONFIRM.value(), mfaHandler::confirm)
                                .DELETE(ApiPathEnum.SUB_MFA_DISABLE.value(), mfaHandler::disable)
                        )
                )
                .GET(ApiPathEnum.JWKS.value(), jwksHandler::getJwks)
                .build();
    }
}
