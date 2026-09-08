package pe.ask.auth.output.security.adapter;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import pe.ask.auth.core.model.exception.DomainValidation;
import pe.ask.auth.core.port.out.PasswordHasherOutputPort;
import pe.ask.auth.output.security.model.Argon2SecurityConfig;
import pe.ask.auth.output.security.model.SecurityEnum;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public final class Argon2idPasswordHasherAdapter implements PasswordHasherOutputPort {

    private final Argon2PasswordEncoder encoder;
    private final Scheduler argonScheduler;
    private final String dummyHash;

    public Argon2idPasswordHasherAdapter(Argon2SecurityConfig config, Scheduler argonScheduler) {
        Argon2SecurityConfig effectiveConfig = config != null ? config : Argon2SecurityConfig.defaultConfig();
        this.encoder = new Argon2PasswordEncoder(
                effectiveConfig.saltLength(),
                effectiveConfig.hashLength(),
                effectiveConfig.parallelism(),
                effectiveConfig.memoryKib(),
                effectiveConfig.iterations()
        );
        this.argonScheduler = argonScheduler != null
                ? argonScheduler
                : Schedulers.newParallel(SecurityEnum.SCHEDULER_NAME.value(), effectiveConfig.schedulerThreads());
        this.dummyHash = encoder.encode(effectiveConfig.dummyPassword());
    }

    public Argon2idPasswordHasherAdapter(Argon2SecurityConfig config) {
        this(config, null);
    }

    public Argon2idPasswordHasherAdapter() {
        this(Argon2SecurityConfig.defaultConfig(), null);
    }

    @Override
    public Mono<String> hashPassword(String rawPassword) {
        DomainValidation.requireNonNull(rawPassword, SecurityEnum.PARAM_RAW_PASSWORD.value());
        return Mono.fromCallable(() -> encoder.encode(rawPassword))
                .subscribeOn(argonScheduler);
    }

    @Override
    public Mono<Boolean> verifyPassword(String rawPassword, String passwordHash) {
        DomainValidation.requireNonNull(rawPassword, SecurityEnum.PARAM_RAW_PASSWORD.value());
        DomainValidation.requireNonNull(passwordHash, SecurityEnum.PARAM_PASSWORD_HASH.value());
        return Mono.fromCallable(() -> encoder.matches(rawPassword, passwordHash))
                .subscribeOn(argonScheduler);
    }

    @Override
    public Mono<Void> verifyDummyPassword(String rawPassword) {
        DomainValidation.requireNonNull(rawPassword, SecurityEnum.PARAM_RAW_PASSWORD.value());
        return Mono.fromCallable(() -> {
            encoder.matches(rawPassword, dummyHash);
            return true;
        }).subscribeOn(argonScheduler).then();
    }
}
