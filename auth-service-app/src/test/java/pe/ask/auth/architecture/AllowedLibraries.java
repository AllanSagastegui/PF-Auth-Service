package pe.ask.auth.architecture;

public final class AllowedLibraries {
    private AllowedLibraries() {
        throw new IllegalStateException("Architecture constants class");
    }

    /*
     * Allowed libraries inside parts of core.
     */
    public static final String JAVA =
            "java..";

    public static final String REACTOR =
            "reactor..";

    public static final String REACTIVE_STREAMS =
            "org.reactivestreams..";
}
