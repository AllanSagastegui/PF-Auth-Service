package pe.ask.auth.output.security.adapter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UuidGeneratorAdapter tests")
class UuidGeneratorAdapterTest {

    @Test
    @DisplayName("Should generate valid non-null UUID")
    void shouldGenerateValidUuid() {
        UuidGeneratorAdapter adapter = new UuidGeneratorAdapter();
        StepVerifier.create(adapter.nextId())
                .assertNext((UUID id) -> assertThat(id).isNotNull())
                .verifyComplete();
    }
}
