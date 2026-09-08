package pe.ask.auth.output.security.config;

import reactor.blockhound.BlockHound;
import reactor.blockhound.integration.BlockHoundIntegration;

public final class SecurityTestBlockHoundIntegration implements BlockHoundIntegration {

    @Override
    public void applyTo(BlockHound.Builder builder) {
        builder.allowBlockingCallsInside("java.security.SecureRandom", "nextBytes");
        builder.allowBlockingCallsInside("sun.security.provider.NativePRNG$RandomIO", "readFully");
        builder.allowBlockingCallsInside("sun.security.provider.NativePRNG$RandomIO", "implNextBytes");
        builder.allowBlockingCallsInside("sun.security.provider.NativePRNG$RandomIO", "ensureBufferValid");
        builder.allowBlockingCallsInside("org.springframework.security.crypto.argon2.Argon2PasswordEncoder", "encodeNonNullPassword");
        builder.allowBlockingCallsInside("org.bouncycastle.crypto.generators.Argon2BytesGenerator", "generateBytes");
    }
}
