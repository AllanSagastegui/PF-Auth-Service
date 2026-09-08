package pe.ask.auth.core.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.ask.auth.core.model.OutboxMessage;
import pe.ask.auth.core.model.Role;
import pe.ask.auth.core.model.User;
import pe.ask.auth.core.model.UserStatus;
import pe.ask.auth.core.model.exception.PasswordPolicyException;
import pe.ask.auth.core.model.exception.UserAlreadyExistsException;
import pe.ask.auth.core.port.in.command.RegisterUserCommand;
import pe.ask.auth.core.port.out.ClockOutputPort;
import pe.ask.auth.core.port.out.IdGeneratorOutputPort;
import pe.ask.auth.core.port.out.NotificationOutputPort;
import pe.ask.auth.core.port.out.OneTimeTokenRepositoryOutputPort;
import pe.ask.auth.core.port.out.OutboxRepositoryOutputPort;
import pe.ask.auth.core.port.out.PasswordHasherOutputPort;
import pe.ask.auth.core.port.out.TokenGeneratorOutputPort;
import pe.ask.auth.core.port.out.UserRepositoryOutputPort;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

    @Mock
    private UserRepositoryOutputPort userRepository;
    @Mock
    private OneTimeTokenRepositoryOutputPort oneTimeTokenRepository;
    @Mock
    private OutboxRepositoryOutputPort outboxRepository;
    @Mock
    private PasswordHasherOutputPort passwordHasher;
    @Mock
    private TokenGeneratorOutputPort tokenGenerator;
    @Mock
    private NotificationOutputPort notificationPort;
    @Mock
    private ClockOutputPort clockPort;
    @Mock
    private IdGeneratorOutputPort idGenerator;

    private RegisterUserUseCase useCase;

    private final Instant now = Instant.parse("2026-03-30T10:00:00Z");
    private final UUID generatedUserId = UUID.randomUUID();
    private final UUID generatedTokenId = UUID.randomUUID();
    private final UUID generatedOutboxId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new RegisterUserUseCase(
                userRepository,
                oneTimeTokenRepository,
                outboxRepository,
                passwordHasher,
                tokenGenerator,
                notificationPort,
                clockPort,
                idGenerator
        );
    }

    @Test
    @DisplayName("Should reject password shorter than 15 characters")
    void shouldRejectShortPassword() {
        RegisterUserCommand cmd = new RegisterUserCommand("User@Example.com", "Short1!");

        StepVerifier.create(useCase.register(cmd))
                .expectError(PasswordPolicyException.class)
                .verify();
    }

    @Test
    @DisplayName("Should reject registration if canonical email already exists")
    void shouldRejectDuplicateEmail() {
        RegisterUserCommand cmd = new RegisterUserCommand("User@Example.com", "ValidSecurePassword123!");
        when(clockPort.now()).thenReturn(Mono.just(now));
        when(userRepository.existsByCanonicalEmail("user@example.com")).thenReturn(Mono.just(true));

        StepVerifier.create(useCase.register(cmd))
                .expectError(UserAlreadyExistsException.class)
                .verify();
    }

    @Test
    @DisplayName("Should register user with hashed password, canonical email, and publish verification token")
    void shouldRegisterUserSuccessfully() {
        RegisterUserCommand cmd = new RegisterUserCommand("User@Example.com", "ValidSecurePassword123!");

        when(clockPort.now()).thenReturn(Mono.just(now));
        when(userRepository.existsByCanonicalEmail("user@example.com")).thenReturn(Mono.just(false));
        when(passwordHasher.hashPassword("ValidSecurePassword123!")).thenReturn(Mono.just("argon2_hash"));
        when(tokenGenerator.generateSecureToken()).thenReturn(Mono.just("raw_verification_token"));
        when(tokenGenerator.hashToken("raw_verification_token")).thenReturn(Mono.just("hashed_verification_token"));
        when(idGenerator.nextId())
                .thenReturn(Mono.just(generatedUserId))
                .thenReturn(Mono.just(generatedTokenId))
                .thenReturn(Mono.just(generatedOutboxId));

        User savedUser = new User(
                generatedUserId,
                "User@Example.com",
                "user@example.com",
                "argon2_hash",
                UserStatus.PENDING_VERIFICATION,
                Set.of(Role.ROLE_USER),
                1L,
                false,
                null,
                now,
                now
        );
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(savedUser));
        when(oneTimeTokenRepository.save(any())).thenReturn(Mono.empty());
        when(notificationPort.sendVerificationEmail(eq("User@Example.com"), eq("raw_verification_token"))).thenReturn(Mono.empty());
        when(outboxRepository.save(any(OutboxMessage.class))).thenReturn(Mono.empty());

        StepVerifier.create(useCase.register(cmd))
                .expectNextMatches(result ->
                        result.userId().equals(generatedUserId) &&
                        result.email().equals("User@Example.com")
                )
                .verifyComplete();

        verify(userRepository).save(argThat(user ->
                user.canonicalEmail().equals("user@example.com") &&
                user.rawEmail().equals("User@Example.com") &&
                user.passwordHash().equals("argon2_hash") &&
                user.status() == UserStatus.PENDING_VERIFICATION
        ));

        verify(notificationPort).sendVerificationEmail("User@Example.com", "raw_verification_token");
        verify(outboxRepository).save(any(OutboxMessage.class));
    }
}
