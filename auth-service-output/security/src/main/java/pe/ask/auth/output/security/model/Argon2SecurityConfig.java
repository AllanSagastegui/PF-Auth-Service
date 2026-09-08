package pe.ask.auth.output.security.model;

public record Argon2SecurityConfig(
        int saltLength,
        int hashLength,
        int parallelism,
        int memoryKib,
        int iterations,
        int schedulerThreads,
        String dummyPassword
) {
    public static Argon2SecurityConfig defaultConfig() {
        return new Argon2SecurityConfig(16, 32, 1, 19456, 2, 4, "dummy-password-for-anti-enumeration-timing");
    }
}
