package pe.ask.auth.output.security.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.ask.auth.core.model.exception.RequiredArgumentException;
import pe.ask.auth.core.model.exception.TotpCryptoException;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("TotpAdapter tests")
class TotpAdapterTest {

    private TotpAdapter adapter;

    @BeforeEach
    void setUp() {
        byte[] key = new byte[32];
        for (int i = 0; i < key.length; i++) {
            key[i] = (byte) i;
        }
        adapter = new TotpAdapter(key);
    }

    @Test
    @DisplayName("Should generate valid Base32 secret")
    void shouldGenerateSecret() {
        StepVerifier.create(adapter.generateSecret())
                .assertNext(secret -> assertThat(secret).matches("^[A-Z2-7]+$"))
                .verifyComplete();
    }

    @Test
    @DisplayName("Should encrypt and decrypt secret successfully")
    void shouldEncryptAndDecrypt() {
        String secret = "JBSWY3DPEHPK3PXP";

        StepVerifier.create(adapter.encryptSecret(secret)
                        .flatMap(encrypted -> adapter.decryptSecret(encrypted)))
                .expectNext(secret)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should fail decrypting corrupted or short payload")
    void shouldFailDecryptingCorrupted() {
        String tooShort = Base64.getEncoder().encodeToString("short".getBytes(StandardCharsets.UTF_8));

        StepVerifier.create(adapter.decryptSecret(tooShort))
                .expectError(TotpCryptoException.class)
                .verify();

        StepVerifier.create(adapter.decryptSecret("not-valid-base-64!!!"))
                .expectError(TotpCryptoException.class)
                .verify();
    }

    @Test
    @DisplayName("Should validate required arguments for encryption and decryption")
    void shouldValidateArguments() {
        assertThrows(RequiredArgumentException.class, () -> adapter.encryptSecret(null));
        assertThrows(RequiredArgumentException.class, () -> adapter.decryptSecret(null));
    }

    @Test
    @DisplayName("Should return false for invalid code format or null")
    void shouldReturnFalseForInvalidCode() {
        StepVerifier.create(adapter.verifyCode(null, "123456"))
                .expectNext(false)
                .verifyComplete();

        StepVerifier.create(adapter.verifyCode("JBSWY3DPEHPK3PXP", null))
                .expectNext(false)
                .verifyComplete();

        StepVerifier.create(adapter.verifyCode("JBSWY3DPEHPK3PXP", "12"))
                .expectNext(false)
                .verifyComplete();

        StepVerifier.create(adapter.verifyCode("JBSWY3DPEHPK3PXP", "000000"))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("Should generate TOTP URI with proper encoding")
    void shouldGenerateTotpUri() {
        StepVerifier.create(adapter.generateTotpUri("user@example.com", "JBSWY3DPEHPK3PXP"))
                .assertNext(uri -> {
                    assertThat(uri).startsWith("otpauth://totp/");
                    assertThat(uri).contains("secret=JBSWY3DPEHPK3PXP");
                    assertThat(uri).contains("user%40example.com");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Should instantiate with default constructor")
    void shouldSupportDefaultConstructor() {
        TotpAdapter defaultAdapter = new TotpAdapter();
        assertThat(defaultAdapter).isNotNull();
    }
}
