package pe.ask.auth.output.security.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.ask.auth.core.model.exception.RequiredArgumentException;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("IdempotencySecurityAdapter tests")
class IdempotencySecurityAdapterTest {

    private IdempotencySecurityAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new IdempotencySecurityAdapter();
    }

    @Test
    @DisplayName("Should acquire lock successfully on new key and reject on duplicate")
    void shouldAcquireLockAndRejectDuplicate() {
        String key = "idemp-key-1";
        String hash = "hash-123";
        Duration ttl = Duration.ofMinutes(1);

        StepVerifier.create(adapter.acquireLock(key, hash, ttl))
                .expectNext(true)
                .verifyComplete();

        StepVerifier.create(adapter.acquireLock(key, hash, ttl))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should save and retrieve cached response")
    void shouldSaveAndRetrieveCachedResponse() {
        String key = "cached-key-1";
        String response = "{\"status\":\"ok\"}";
        Duration ttl = Duration.ofMinutes(5);

        StepVerifier.create(adapter.getCachedResponse(key))
                .expectNextCount(0)
                .verifyComplete();

        StepVerifier.create(adapter.saveCachedResponse(key, response, ttl))
                .verifyComplete();

        StepVerifier.create(adapter.getCachedResponse(key))
                .expectNext(response)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should validate required arguments")
    void shouldValidateArguments() {
        Duration ttl = Duration.ofMinutes(1);
        assertThrows(RequiredArgumentException.class, () -> adapter.acquireLock(null, "hash", ttl));
        assertThrows(RequiredArgumentException.class, () -> adapter.acquireLock("key", null, ttl));
        assertThrows(RequiredArgumentException.class, () -> adapter.acquireLock("key", "hash", null));
        assertThrows(RequiredArgumentException.class, () -> adapter.getCachedResponse(null));
        assertThrows(RequiredArgumentException.class, () -> adapter.saveCachedResponse(null, "resp", ttl));
        assertThrows(RequiredArgumentException.class, () -> adapter.saveCachedResponse("key", null, ttl));
        assertThrows(RequiredArgumentException.class, () -> adapter.saveCachedResponse("key", "resp", null));
    }
}
