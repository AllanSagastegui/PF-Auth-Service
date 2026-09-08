package pe.ask.auth.output.security.adapter;

import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.ask.auth.core.model.JwksKey;
import pe.ask.auth.core.model.exception.RequiredArgumentException;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("JwksAdapter tests")
class JwksAdapterTest {

    @Test
    @DisplayName("Should extract public keys correctly from ECKey")
    void shouldExtractPublicKeys() throws Exception {
        ECKey ecKey = new ECKeyGenerator(Curve.P_256).keyID("test-key-id").generate();
        JwksAdapter adapter = new JwksAdapter(ecKey);

        StepVerifier.create(adapter.getPublicKeys())
                .assertNext((List<JwksKey> keys) -> {
                    assertThat(keys).hasSize(1);
                    JwksKey key = keys.get(0);
                    assertThat(key.kty()).isEqualTo("EC");
                    assertThat(key.crv()).isEqualTo("P-256");
                    assertThat(key.kid()).isEqualTo("test-key-id");
                    assertThat(key.use()).isEqualTo("sig");
                    assertThat(key.alg()).isEqualTo("ES256");
                    assertThat(key.x()).isNotBlank();
                    assertThat(key.y()).isNotBlank();
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should throw RequiredArgumentException when key is null")
    void shouldThrowWhenKeyNull() {
        assertThrows(RequiredArgumentException.class, () -> new JwksAdapter(null));
    }
}
