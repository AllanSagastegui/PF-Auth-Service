package pe.ask.auth.core.model.exception;

import java.util.Map;

public class InvalidTokenException extends BaseException {

    public InvalidTokenException() {
        super(ErrorCatalog.INVALID_TOKEN);
    }

    public InvalidTokenException(String customMessage) {
        super(ErrorCatalog.INVALID_TOKEN, customMessage);
    }

    public InvalidTokenException(Map<String, String> errors) {
        super(ErrorCatalog.INVALID_TOKEN, errors);
    }

    public InvalidTokenException(String customMessage, Map<String, String> errors) {
        super(ErrorCatalog.INVALID_TOKEN, customMessage, errors);
    }
}
