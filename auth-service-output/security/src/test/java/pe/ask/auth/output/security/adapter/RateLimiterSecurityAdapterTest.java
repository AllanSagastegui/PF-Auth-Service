package pe.ask.auth.output.security.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.ask.auth.core.model.exception.RequiredArgumentException;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("RateLimiterSecurityAdapter tests")
class RateLimiterSecurityAdapterTest {

    private RateLimiterSecurityAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new RateLimiterSecurityAdapter();
    }

    @Test
    @DisplayName("Should allow requests within rate limit and reject when exceeded")
    void shouldRateLimitAccurately() {
        String key = "client-ip-123";
        int limit = 3;
        Duration window = Duration.ofMinutes(1);

        for (int i = 0; i < limit; i++) {
            StepVerifier.create(adapter.tryAcquire(key, limit, window))
                    .expectNext(true)
                    .verifyComplete();
        }

        StepVerifier.create(adapter.tryAcquire(key, limit, window))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should validate required arguments")
    void shouldValidateArguments() {
        Duration window = Duration.ofMinutes(1);
        assertThrows(RequiredArgumentException.class, () -> adapter.tryAcquire(null, 10, window));
        assertThrows(RequiredArgumentException.class, () -> adapter.tryAcquire("key", 10, null));
    }
}
