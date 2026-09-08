package pe.ask.auth.core.model.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Domain Exceptions Comprehensive Tests")
class DomainExceptionsTest {

    @Test
    @DisplayName("Test AccountDisabledException")
    void testAccountDisabledException() {
        AccountDisabledException ex1 = new AccountDisabledException();
        assertThat(ex1.getStatus()).isEqualTo(403);
        assertThat(ex1.getErrorCode()).isEqualTo("AUTH_ACCOUNT_UNAVAILABLE");

        AccountDisabledException ex2 = new AccountDisabledException("Custom disabled");
        assertThat(ex2.getMessage()).isEqualTo("Custom disabled");

        AccountDisabledException ex3 = new AccountDisabledException(Map.of("field", "err"));
        assertThat(ex3.getErrors()).containsEntry("field", "err");

        AccountDisabledException ex4 = new AccountDisabledException("Custom", Map.of("field", "err"));
        assertThat(ex4.getMessage()).isEqualTo("Custom");
    }

    @Test
    @DisplayName("Test AccountLockedException")
    void testAccountLockedException() {
        AccountLockedException ex1 = new AccountLockedException();
        assertThat(ex1.getStatus()).isEqualTo(423);
        assertThat(ex1.getErrorCode()).isEqualTo("AUTH_ACCOUNT_LOCKED");

        AccountLockedException ex2 = new AccountLockedException("Custom locked");
        assertThat(ex2.getMessage()).isEqualTo("Custom locked");

        AccountLockedException ex3 = new AccountLockedException(Map.of("field", "err"));
        assertThat(ex3.getErrors()).containsEntry("field", "err");

        AccountLockedException ex4 = new AccountLockedException("Custom", Map.of("field", "err"));
        assertThat(ex4.getMessage()).isEqualTo("Custom");
    }

    @Test
    @DisplayName("Test ForbiddenException")
    void testForbiddenException() {
        ForbiddenException ex1 = new ForbiddenException();
        assertThat(ex1.getStatus()).isEqualTo(403);

        ForbiddenException ex2 = new ForbiddenException("Custom forbidden");
        assertThat(ex2.getMessage()).isEqualTo("Custom forbidden");

        ForbiddenException ex3 = new ForbiddenException(Map.of("perm", "denied"));
        assertThat(ex3.getErrors()).containsEntry("perm", "denied");

        ForbiddenException ex4 = new ForbiddenException("Custom", Map.of("perm", "denied"));
        assertThat(ex4.getMessage()).isEqualTo("Custom");
    }

    @Test
    @DisplayName("Test InvalidCredentialsException")
    void testInvalidCredentialsException() {
        InvalidCredentialsException ex1 = new InvalidCredentialsException();
        assertThat(ex1.getStatus()).isEqualTo(401);

        InvalidCredentialsException ex2 = new InvalidCredentialsException("Custom bad creds");
        assertThat(ex2.getMessage()).isEqualTo("Custom bad creds");

        InvalidCredentialsException ex3 = new InvalidCredentialsException(Map.of("cred", "invalid"));
        assertThat(ex3.getErrors()).containsEntry("cred", "invalid");

        InvalidCredentialsException ex4 = new InvalidCredentialsException("Custom", Map.of("cred", "invalid"));
        assertThat(ex4.getMessage()).isEqualTo("Custom");
    }

    @Test
    @DisplayName("Test InvalidTokenException")
    void testInvalidTokenException() {
        InvalidTokenException ex1 = new InvalidTokenException();
        assertThat(ex1.getStatus()).isEqualTo(401);

        InvalidTokenException ex2 = new InvalidTokenException("Token bad");
        assertThat(ex2.getMessage()).isEqualTo("Token bad");

        InvalidTokenException ex3 = new InvalidTokenException(Map.of("tok", "bad"));
        assertThat(ex3.getErrors()).containsEntry("tok", "bad");

        InvalidTokenException ex4 = new InvalidTokenException("Custom", Map.of("tok", "bad"));
        assertThat(ex4.getMessage()).isEqualTo("Custom");
    }

    @Test
    @DisplayName("Test RequiredArgumentException")
    void testRequiredArgumentException() {
        RequiredArgumentException ex1 = new RequiredArgumentException("paramName");
        assertThat(ex1.getStatus()).isEqualTo(400);
        assertThat(ex1.getMessage()).contains("paramName");

        RequiredArgumentException ex2 = new RequiredArgumentException(null);
        assertThat(ex2.getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("Test TokenExpiredException")
    void testTokenExpiredException() {
        TokenExpiredException ex1 = new TokenExpiredException();
        assertThat(ex1.getStatus()).isEqualTo(401);

        TokenExpiredException ex2 = new TokenExpiredException("Expired token");
        assertThat(ex2.getMessage()).isEqualTo("Expired token");

        TokenExpiredException ex3 = new TokenExpiredException(Map.of("tok", "expired"));
        assertThat(ex3.getErrors()).containsEntry("tok", "expired");

        TokenExpiredException ex4 = new TokenExpiredException("Custom", Map.of("tok", "expired"));
        assertThat(ex4.getMessage()).isEqualTo("Custom");
    }

    @Test
    @DisplayName("Test TokenInitializationException")
    void testTokenInitializationException() {
        TokenInitializationException ex = new TokenInitializationException("Failed to init", new RuntimeException("root cause"));
        assertThat(ex.getStatus()).isEqualTo(500);
        assertThat(ex.getMessage()).isEqualTo("Failed to init");
        assertThat(ex.getCause()).isNotNull();
    }

    @Test
    @DisplayName("Test TotpCryptoException")
    void testTotpCryptoException() {
        TotpCryptoException ex = new TotpCryptoException("Crypto fail", new RuntimeException("root cause"));
        assertThat(ex.getStatus()).isEqualTo(500);
        assertThat(ex.getMessage()).isEqualTo("Crypto fail");
        assertThat(ex.getCause()).isNotNull();
    }

    @Test
    @DisplayName("Test UnauthorizedException")
    void testUnauthorizedException() {
        UnauthorizedException ex1 = new UnauthorizedException();
        assertThat(ex1.getStatus()).isEqualTo(401);

        UnauthorizedException ex2 = new UnauthorizedException("Unauthorized action");
        assertThat(ex2.getMessage()).isEqualTo("Unauthorized action");

        UnauthorizedException ex3 = new UnauthorizedException(Map.of("auth", "none"));
        assertThat(ex3.getErrors()).containsEntry("auth", "none");

        UnauthorizedException ex4 = new UnauthorizedException("Custom", Map.of("auth", "none"));
        assertThat(ex4.getMessage()).isEqualTo("Custom");
    }

    @Test
    @DisplayName("Test DatabaseOperationException")
    void testDatabaseOperationException() {
        DatabaseOperationException ex1 = new DatabaseOperationException("DB error");
        assertThat(ex1.getStatus()).isEqualTo(500);
        assertThat(ex1.getMessage()).isEqualTo("DB error");

        DatabaseOperationException ex2 = new DatabaseOperationException("DB error with cause", new RuntimeException("db down"));
        assertThat(ex2.getCause()).isNotNull();
    }
}
