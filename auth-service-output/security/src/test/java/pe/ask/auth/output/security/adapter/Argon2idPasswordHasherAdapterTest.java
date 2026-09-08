package pe.ask.auth.output.security.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.ask.auth.core.model.exception.RequiredArgumentException;
import pe.ask.auth.output.security.model.Argon2SecurityConfig;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Argon2idPasswordHasherAdapter tests")
class Argon2idPasswordHasherAdapterTest {

    private Argon2idPasswordHasherAdapter adapter;

    @BeforeEach
    void setUp() {
        Argon2SecurityConfig config = new Argon2SecurityConfig(16, 32, 1, 4096, 2, 2, "dummy-pass");
        adapter = new Argon2idPasswordHasherAdapter(config);
    }

    @Test
    @DisplayName("Should hash and verify password successfully")
    void shouldHashAndVerifyPassword() {
        String raw = "StrongSecret123!";

        StepVerifier.create(adapter.hashPassword(raw)
                        .flatMap(hash -> adapter.verifyPassword(raw, hash)))
                .expectNext(true)
                .verifyComplete();

        StepVerifier.create(adapter.hashPassword(raw)
                        .flatMap(hash -> adapter.verifyPassword("wrong-pass", hash)))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should verify dummy password without error")
    void shouldVerifyDummyPassword() {
        StepVerifier.create(adapter.verifyDummyPassword("any-input"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should throw RequiredArgumentException for null arguments")
    void shouldThrowOnNull() {
        assertThrows(RequiredArgumentException.class, () -> adapter.hashPassword(null));
        assertThrows(RequiredArgumentException.class, () -> adapter.verifyPassword(null, "hash"));
        assertThrows(RequiredArgumentException.class, () -> adapter.verifyPassword("pass", null));
        assertThrows(RequiredArgumentException.class, () -> adapter.verifyDummyPassword(null));
    }

    @Test
    @DisplayName("Should instantiate with default constructors")
    void shouldSupportConstructors() {
        Argon2idPasswordHasherAdapter def1 = new Argon2idPasswordHasherAdapter();
        assertThat(def1).isNotNull();
        Argon2idPasswordHasherAdapter def2 = new Argon2idPasswordHasherAdapter(null);
        assertThat(def2).isNotNull();
    }
}
