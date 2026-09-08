package pe.ask.auth.core.model.exception;

import java.util.Map;

public class UserAlreadyExistsException extends BaseException {

    public UserAlreadyExistsException() {
        super(ErrorCatalog.USER_ALREADY_EXISTS);
    }

    public UserAlreadyExistsException(String customMessage) {
        super(ErrorCatalog.USER_ALREADY_EXISTS, customMessage);
    }

    public UserAlreadyExistsException(Map<String, String> errors) {
        super(ErrorCatalog.USER_ALREADY_EXISTS, errors);
    }

    public UserAlreadyExistsException(String customMessage, Map<String, String> errors) {
        super(ErrorCatalog.USER_ALREADY_EXISTS, customMessage, errors);
    }
}
