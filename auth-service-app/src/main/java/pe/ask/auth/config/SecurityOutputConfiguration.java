package pe.ask.auth.config;

import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.ask.auth.config.properties.AuthSecurityProperties;
import pe.ask.auth.core.port.out.ClockOutputPort;
import pe.ask.auth.core.port.out.IdGeneratorOutputPort;
import pe.ask.auth.core.port.out.IdempotencyOutputPort;
import pe.ask.auth.core.port.out.JwksOutputPort;
import pe.ask.auth.core.port.out.PasswordHasherOutputPort;
import pe.ask.auth.core.port.out.RateLimiterOutputPort;
import pe.ask.auth.core.port.out.TokenGeneratorOutputPort;
import pe.ask.auth.core.port.out.TotpOutputPort;
import pe.ask.auth.output.security.adapter.Argon2idPasswordHasherAdapter;
import pe.ask.auth.output.security.adapter.IdempotencySecurityAdapter;
import pe.ask.auth.output.security.adapter.JwksAdapter;
import pe.ask.auth.output.security.adapter.RateLimiterSecurityAdapter;
import pe.ask.auth.output.security.adapter.SystemClockAdapter;
import pe.ask.auth.output.security.adapter.TokenGeneratorAdapter;
import pe.ask.auth.output.security.adapter.TotpAdapter;
import pe.ask.auth.output.security.adapter.UuidGeneratorAdapter;
import pe.ask.auth.output.security.model.Argon2SecurityConfig;
import pe.ask.auth.output.security.model.SecurityEnum;

@Configuration(proxyBeanMethods = false)
public final class SecurityOutputConfiguration {

    @Bean
    public ClockOutputPort clockOutputPort() {
        return new SystemClockAdapter();
    }

    @Bean
    public IdGeneratorOutputPort idGeneratorOutputPort() {
        return new UuidGeneratorAdapter();
    }

    @Bean
    public PasswordHasherOutputPort passwordHasherOutputPort(AuthSecurityProperties properties) {
        AuthSecurityProperties.Argon2Properties argon = properties.argon2() != null
                ? properties.argon2()
                : new AuthSecurityProperties.Argon2Properties(16, 32, 1, 19456, 2, 4, null);
        Argon2SecurityConfig config = new Argon2SecurityConfig(
                argon.saltLength(),
                argon.hashLength(),
                argon.parallelism(),
                argon.memoryKib(),
                argon.iterations(),
                argon.schedulerThreads(),
                argon.dummyPassword()
        );
        return new Argon2idPasswordHasherAdapter(config);
    }

    @Bean
    public ECKey ecKey() throws Exception {
        return new ECKeyGenerator(Curve.P_256).keyID(SecurityEnum.KEY_ID.value()).generate();
    }

    @Bean
    public TokenGeneratorOutputPort tokenGeneratorOutputPort(ECKey ecKey) {
        return new TokenGeneratorAdapter(ecKey);
    }

    @Bean
    public JwksOutputPort jwksOutputPort(ECKey ecKey) {
        return new JwksAdapter(ecKey);
    }

    @Bean
    public TotpOutputPort totpOutputPort(AuthSecurityProperties properties) {
        String keyHex = properties.totp() != null ? properties.totp().encryptionKeyHex() : "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
        byte[] secretKey = new byte[32];
        for (int i = 0; i < 32; i++) {
            secretKey[i] = (byte) Integer.parseInt(keyHex.substring(i * 2, i * 2 + 2), 16);
        }
        return new TotpAdapter(secretKey);
    }

    @Bean
    public IdempotencyOutputPort idempotencyOutputPort() {
        return new IdempotencySecurityAdapter();
    }

    @Bean
    public RateLimiterOutputPort rateLimiterOutputPort() {
        return new RateLimiterSecurityAdapter();
    }
}
