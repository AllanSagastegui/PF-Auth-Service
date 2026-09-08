package pe.ask.auth.core.model.exception;

import java.util.Map;

public class InvalidCredentialsException extends BaseException {

    public InvalidCredentialsException() {
        super(ErrorCatalog.INVALID_CREDENTIALS);
    }

    public InvalidCredentialsException(String customMessage) {
        super(ErrorCatalog.INVALID_CREDENTIALS, customMessage);
    }

    public InvalidCredentialsException(Map<String, String> errors) {
        super(ErrorCatalog.INVALID_CREDENTIALS, errors);
    }

    public InvalidCredentialsException(String customMessage, Map<String, String> errors) {
        super(ErrorCatalog.INVALID_CREDENTIALS, customMessage, errors);
    }
}
