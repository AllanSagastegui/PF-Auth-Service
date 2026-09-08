package pe.ask.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import pe.ask.auth.core.usecase.ChangePasswordUseCase;
import pe.ask.auth.core.usecase.ConfirmMfaUseCase;
import pe.ask.auth.core.usecase.DisableMfaUseCase;
import pe.ask.auth.core.usecase.EmailVerificationConfirmUseCase;
import pe.ask.auth.core.usecase.EmailVerificationRequestUseCase;
import pe.ask.auth.core.usecase.EnrollMfaUseCase;
import pe.ask.auth.core.usecase.ForgotPasswordUseCase;
import pe.ask.auth.core.usecase.GetJwksUseCase;
import pe.ask.auth.core.usecase.GetMeUseCase;
import pe.ask.auth.core.usecase.GetSessionsUseCase;
import pe.ask.auth.core.usecase.LoginUseCase;
import pe.ask.auth.core.usecase.LogoutAllUseCase;
import pe.ask.auth.core.usecase.LogoutUseCase;
import pe.ask.auth.core.usecase.RefreshTokenUseCase;
import pe.ask.auth.core.usecase.RegisterUserUseCase;
import pe.ask.auth.core.usecase.ResetPasswordUseCase;
import pe.ask.auth.core.usecase.RevokeSessionUseCase;
import pe.ask.auth.core.usecase.VerifyMfaUseCase;

@Configuration(proxyBeanMethods = false)
public final class UseCaseConfiguration {

    @Bean
    public RegisterUserInputPort registerUserInputPort(
            UserRepositoryOutputPort userRepository,
            OneTimeTokenRepositoryOutputPort oneTimeTokenRepository,
            OutboxRepositoryOutputPort outboxRepository,
            PasswordHasherOutputPort passwordHasher,
            TokenGeneratorOutputPort tokenGenerator,
            NotificationOutputPort notificationPort,
            ClockOutputPort clock,
            IdGeneratorOutputPort idGenerator
    ) {
        return new RegisterUserUseCase(
                userRepository,
                oneTimeTokenRepository,
                outboxRepository,
                passwordHasher,
                tokenGenerator,
                notificationPort,
                clock,
                idGenerator
        );
    }

    @Bean
    public EmailVerificationRequestInputPort emailVerificationRequestInputPort(
            UserRepositoryOutputPort userRepository,
            OneTimeTokenRepositoryOutputPort oneTimeTokenRepository,
            TokenGeneratorOutputPort tokenGenerator,
            NotificationOutputPort notificationPort,
            ClockOutputPort clock,
            IdGeneratorOutputPort idGenerator
    ) {
        return new EmailVerificationRequestUseCase(
                userRepository,
                oneTimeTokenRepository,
                tokenGenerator,
                notificationPort,
                clock,
                idGenerator
        );
    }

    @Bean
    public EmailVerificationConfirmInputPort emailVerificationConfirmInputPort(
            UserRepositoryOutputPort userRepository,
            OneTimeTokenRepositoryOutputPort oneTimeTokenRepository,
            TokenGeneratorOutputPort tokenGenerator,
            ClockOutputPort clock
    ) {
        return new EmailVerificationConfirmUseCase(
                userRepository,
                oneTimeTokenRepository,
                tokenGenerator,
                clock
        );
    }

    @Bean
    public LoginInputPort loginInputPort(
            UserRepositoryOutputPort userRepository,
            SessionRepositoryOutputPort sessionRepository,
            RefreshTokenRepositoryOutputPort refreshTokenRepository,
            PasswordHasherOutputPort passwordHasher,
            TokenGeneratorOutputPort tokenGenerator,
            ClockOutputPort clock,
            IdGeneratorOutputPort idGenerator
    ) {
        return new LoginUseCase(
                userRepository,
                sessionRepository,
                refreshTokenRepository,
                passwordHasher,
                tokenGenerator,
                clock,
                idGenerator
        );
    }

    @Bean
    public VerifyMfaInputPort verifyMfaInputPort(
            UserRepositoryOutputPort userRepository,
            SessionRepositoryOutputPort sessionRepository,
            RefreshTokenRepositoryOutputPort refreshTokenRepository,
            TokenGeneratorOutputPort tokenGenerator,
            TotpOutputPort totpPort,
            ClockOutputPort clock,
            IdGeneratorOutputPort idGenerator
    ) {
        return new VerifyMfaUseCase(
                userRepository,
                sessionRepository,
                refreshTokenRepository,
                tokenGenerator,
                totpPort,
                clock,
                idGenerator
        );
    }

    @Bean
    public RefreshTokenInputPort refreshTokenInputPort(
            UserRepositoryOutputPort userRepository,
            SessionRepositoryOutputPort sessionRepository,
            RefreshTokenRepositoryOutputPort refreshTokenRepository,
            TokenGeneratorOutputPort tokenGenerator,
            IdempotencyOutputPort idempotencyPort,
            SecurityAuditOutputPort auditPort,
            ClockOutputPort clock,
            IdGeneratorOutputPort idGenerator
    ) {
        return new RefreshTokenUseCase(
                userRepository,
                sessionRepository,
                refreshTokenRepository,
                tokenGenerator,
                idempotencyPort,
                auditPort,
                clock,
                idGenerator
        );
    }

    @Bean
    public LogoutInputPort logoutInputPort(
            SessionRepositoryOutputPort sessionRepository,
            RefreshTokenRepositoryOutputPort refreshTokenRepository,
            TokenGeneratorOutputPort tokenGenerator,
            ClockOutputPort clock
    ) {
        return new LogoutUseCase(
                sessionRepository,
                refreshTokenRepository,
                tokenGenerator,
                clock
        );
    }

    @Bean
    public LogoutAllInputPort logoutAllInputPort(
            UserRepositoryOutputPort userRepository,
            SessionRepositoryOutputPort sessionRepository,
            ClockOutputPort clock
    ) {
        return new LogoutAllUseCase(
                userRepository,
                sessionRepository,
                clock
        );
    }

    @Bean
    public ForgotPasswordInputPort forgotPasswordInputPort(
            UserRepositoryOutputPort userRepository,
            OneTimeTokenRepositoryOutputPort oneTimeTokenRepository,
            TokenGeneratorOutputPort tokenGenerator,
            NotificationOutputPort notificationPort,
            ClockOutputPort clock,
            IdGeneratorOutputPort idGenerator
    ) {
        return new ForgotPasswordUseCase(
                userRepository,
                oneTimeTokenRepository,
                tokenGenerator,
                notificationPort,
                clock,
                idGenerator
        );
    }

    @Bean
    public ResetPasswordInputPort resetPasswordInputPort(
            UserRepositoryOutputPort userRepository,
            SessionRepositoryOutputPort sessionRepository,
            OneTimeTokenRepositoryOutputPort oneTimeTokenRepository,
            PasswordHasherOutputPort passwordHasher,
            TokenGeneratorOutputPort tokenGenerator,
            ClockOutputPort clock
    ) {
        return new ResetPasswordUseCase(
                userRepository,
                sessionRepository,
                oneTimeTokenRepository,
                passwordHasher,
                tokenGenerator,
                clock
        );
    }

    @Bean
    public ChangePasswordInputPort changePasswordInputPort(
            UserRepositoryOutputPort userRepository,
            PasswordHasherOutputPort passwordHasher,
            ClockOutputPort clock
    ) {
        return new ChangePasswordUseCase(
                userRepository,
                passwordHasher,
                clock
        );
    }

    @Bean
    public GetMeInputPort getMeInputPort(UserRepositoryOutputPort userRepository) {
        return new GetMeUseCase(userRepository);
    }

    @Bean
    public GetSessionsInputPort getSessionsInputPort(SessionRepositoryOutputPort sessionRepository) {
        return new GetSessionsUseCase(sessionRepository);
    }

    @Bean
    public RevokeSessionInputPort revokeSessionInputPort(
            SessionRepositoryOutputPort sessionRepository,
            RefreshTokenRepositoryOutputPort refreshTokenRepository,
            ClockOutputPort clock
    ) {
        return new RevokeSessionUseCase(
                sessionRepository,
                refreshTokenRepository,
                clock
        );
    }

    @Bean
    public EnrollMfaInputPort enrollMfaInputPort(
            UserRepositoryOutputPort userRepository,
            TotpOutputPort totpPort,
            TokenGeneratorOutputPort tokenGenerator,
            ClockOutputPort clock
    ) {
        return new EnrollMfaUseCase(
                userRepository,
                totpPort,
                tokenGenerator,
                clock
        );
    }

    @Bean
    public ConfirmMfaInputPort confirmMfaInputPort(
            UserRepositoryOutputPort userRepository,
            TotpOutputPort totpPort,
            ClockOutputPort clock
    ) {
        return new ConfirmMfaUseCase(
                userRepository,
                totpPort,
                clock
        );
    }

    @Bean
    public DisableMfaInputPort disableMfaInputPort(
            UserRepositoryOutputPort userRepository,
            PasswordHasherOutputPort passwordHasher,
            TotpOutputPort totpPort,
            ClockOutputPort clock
    ) {
        return new DisableMfaUseCase(
                userRepository,
                passwordHasher,
                totpPort,
                clock
        );
    }

    @Bean
    public GetJwksInputPort getJwksInputPort(JwksOutputPort jwksPort) {
        return new GetJwksUseCase(jwksPort);
    }
}
