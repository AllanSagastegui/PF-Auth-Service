package pe.ask.auth.core.model.exception;

import java.util.Map;

public class UserNotFoundException extends BaseException {

    public UserNotFoundException() {
        super(ErrorCatalog.USER_NOT_FOUND);
    }

    public UserNotFoundException(String customMessage) {
        super(ErrorCatalog.USER_NOT_FOUND, customMessage);
    }

    public UserNotFoundException(Map<String, String> errors) {
        super(ErrorCatalog.USER_NOT_FOUND, errors);
    }

    public UserNotFoundException(String customMessage, Map<String, String> errors) {
        super(ErrorCatalog.USER_NOT_FOUND, customMessage, errors);
    }
}
