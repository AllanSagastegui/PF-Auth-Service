package pe.ask.auth.output.security.adapter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.ask.auth.core.model.exception.RequiredArgumentException;
import reactor.test.StepVerifier;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("SystemClockAdapter tests")
class SystemClockAdapterTest {

    @Test
    @DisplayName("Should return fixed instant when configured with fixed clock")
    void shouldReturnFixedInstant() {
        Instant fixedInstant = Instant.parse("2026-03-30T10:00:00Z");
        Clock fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC);
        SystemClockAdapter adapter = new SystemClockAdapter(fixedClock);

        StepVerifier.create(adapter.now())
                .expectNext(fixedInstant)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should instantiate with default constructor")
    void shouldInstantiateDefault() {
        SystemClockAdapter adapter = new SystemClockAdapter();
        StepVerifier.create(adapter.now())
                .assertNext(inst -> assertThat(inst).isNotNull())
                .verifyComplete();
    }

    @Test
    @DisplayName("Should throw RequiredArgumentException when clock is null")
    void shouldThrowWhenClockNull() {
        assertThrows(RequiredArgumentException.class, () -> new SystemClockAdapter(null));
    }
}
