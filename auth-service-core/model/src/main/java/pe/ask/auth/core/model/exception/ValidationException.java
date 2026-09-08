package pe.ask.auth.core.model.exception;

import java.util.Map;

public class ValidationException extends BaseException {

    public ValidationException() {
        super(ErrorCatalog.VALIDATION_EXCEPTION);
    }

    public ValidationException(String customMessage) {
        super(ErrorCatalog.VALIDATION_EXCEPTION, customMessage);
    }

    public ValidationException(Map<String, String> errors) {
        super(ErrorCatalog.VALIDATION_EXCEPTION, errors);
    }

    public ValidationException(String customMessage, Map<String, String> errors) {
        super(ErrorCatalog.VALIDATION_EXCEPTION, customMessage, errors);
    }

    public ValidationException(String customMessage, Throwable cause) {
        super(ErrorCatalog.VALIDATION_EXCEPTION, customMessage, cause);
    }
}
