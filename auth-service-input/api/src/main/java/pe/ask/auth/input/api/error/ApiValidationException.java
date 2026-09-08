package pe.ask.auth.input.api.error;

import java.util.Collections;
import java.util.Map;

public class ApiValidationException extends RuntimeException {

    private final String errorCode;
    private final String title;
    private final int httpStatus;
    private final Map<String, String> errors;

    public ApiValidationException(Map<String, String> errors) {
        super("Validation failed");
        this.errorCode = "AUTH_VALIDATION_ERROR";
        this.title = "Validation Failed";
        this.httpStatus = 400;
        this.errors = errors != null ? Collections.unmodifiableMap(errors) : Map.of();
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getTitle() {
        return title;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
