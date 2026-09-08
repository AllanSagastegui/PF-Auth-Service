package pe.ask.auth.core.model.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BaseExceptionTest {

    @Test
    @DisplayName("Should initialize BaseException using ErrorCatalog defaults")
    void shouldInitializeFromErrorCatalog() {
        UserNotFoundException exception = new UserNotFoundException();

        assertEquals("AUTH_USER_NOT_FOUND", exception.getErrorCode());
        assertEquals("UserNotFoundException", exception.getTitle());
        assertEquals("The requested user could not be found.", exception.getMessage());
        assertEquals(404, exception.getStatus());
        assertNull(exception.getErrors());
        assertNotNull(exception.getTimestamp());
    }

    @Test
    @DisplayName("Should initialize BaseException with custom message")
    void shouldInitializeWithCustomMessage() {
        UserNotFoundException exception = new UserNotFoundException("User with email user@test.com was not found");

        assertEquals("AUTH_USER_NOT_FOUND", exception.getErrorCode());
        assertEquals("UserNotFoundException", exception.getTitle());
        assertEquals("User with email user@test.com was not found", exception.getMessage());
        assertEquals(404, exception.getStatus());
        assertNull(exception.getErrors());
        assertNotNull(exception.getTimestamp());
    }

    @Test
    @DisplayName("Should initialize BaseException with custom errors map")
    void shouldInitializeWithCustomErrors() {
        Map<String, String> errors = Map.of("email", "Email already registered");
        UserAlreadyExistsException exception = new UserAlreadyExistsException(errors);

        assertEquals("AUTH_USER_ALREADY_EXISTS", exception.getErrorCode());
        assertEquals("UserAlreadyExistsException", exception.getTitle());
        assertEquals("A user with the same email already exists.", exception.getMessage());
        assertEquals(409, exception.getStatus());
        assertEquals(errors, exception.getErrors());
    }

    @Test
    @DisplayName("Should initialize BaseException with custom message and errors map")
    void shouldInitializeWithCustomMessageAndErrors() {
        Map<String, String> errors = Map.of("username", "must not be blank");
        ValidationException exception = new ValidationException("Invalid payload", errors);

        assertEquals("AUTH_VALIDATION_ERROR", exception.getErrorCode());
        assertEquals("Validation Failed", exception.getTitle());
        assertEquals("Invalid payload", exception.getMessage());
        assertEquals(400, exception.getStatus());
        assertEquals(errors, exception.getErrors());
    }

    @Test
    @DisplayName("Should initialize BaseException with explicit Clock")
    void shouldInitializeWithExplicitClock() {
        Instant fixedInstant = Instant.parse("2026-01-01T10:00:00Z");
        Clock fixedClock = Clock.fixed(fixedInstant, ZoneId.of("UTC"));

        BaseException exception = new BaseException(
                "CUSTOM_CODE",
                "CustomTitle",
                "Custom message",
                400,
                null,
                fixedClock
        );

        assertEquals("CUSTOM_CODE", exception.getErrorCode());
        assertEquals("CustomTitle", exception.getTitle());
        assertEquals("Custom message", exception.getMessage());
        assertEquals(400, exception.getStatus());
        assertEquals(LocalDateTime.ofInstant(fixedInstant, ZoneId.of("UTC")), exception.getTimestamp());
    }

    @Test
    @DisplayName("Should initialize InternalServerErrorException with default server errors map")
    void shouldInitializeInternalServerError() {
        InternalServerErrorException exception = new InternalServerErrorException();

        assertEquals("AUTH_INTERNAL_ERROR", exception.getErrorCode());
        assertEquals("Internal Server Error", exception.getTitle());
        assertEquals(500, exception.getStatus());
        assertNotNull(exception.getErrors());
        assertEquals("Unexpected error occurred", exception.getErrors().get("server"));
    }

    @Test
    @DisplayName("Should initialize auth-specific domain exceptions correctly")
    void shouldInitializeAuthDomainExceptions() {
        EmailNotVerifiedException emailException = new EmailNotVerifiedException();
        assertEquals("AUTH_EMAIL_NOT_VERIFIED", emailException.getErrorCode());
        assertEquals(403, emailException.getStatus());

        RateLimitedException rateException = new RateLimitedException();
        assertEquals("AUTH_RATE_LIMITED", rateException.getErrorCode());
        assertEquals(429, rateException.getStatus());

        IdempotencyConflictException idempException = new IdempotencyConflictException();
        assertEquals("AUTH_IDEMPOTENCY_CONFLICT", idempException.getErrorCode());
        assertEquals(409, idempException.getStatus());

        PasswordPolicyException policyException = new PasswordPolicyException(Map.of("password", "Too short"));
        assertEquals("AUTH_PASSWORD_POLICY", policyException.getErrorCode());
        assertEquals(422, policyException.getStatus());
    }
}
