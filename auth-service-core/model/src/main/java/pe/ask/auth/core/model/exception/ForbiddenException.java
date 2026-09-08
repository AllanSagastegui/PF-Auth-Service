package pe.ask.auth.core.model.exception;

import java.util.Map;

public class ForbiddenException extends BaseException {

    public ForbiddenException() {
        super(ErrorCatalog.FORBIDDEN);
    }

    public ForbiddenException(String customMessage) {
        super(ErrorCatalog.FORBIDDEN, customMessage);
    }

    public ForbiddenException(Map<String, String> errors) {
        super(ErrorCatalog.FORBIDDEN, errors);
    }

    public ForbiddenException(String customMessage, Map<String, String> errors) {
        super(ErrorCatalog.FORBIDDEN, customMessage, errors);
    }
}
