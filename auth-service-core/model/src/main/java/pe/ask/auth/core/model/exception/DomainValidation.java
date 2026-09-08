package pe.ask.auth.core.model.exception;

public final class DomainValidation {

    private DomainValidation() {
        // Utility class
    }

    public static <T> T requireNonNull(T object, String argumentName) {
        if (object == null) {
            throw new RequiredArgumentException(argumentName);
        }
        return object;
    }
}
