package pe.ask.auth.core.model.exception;

import java.util.Map;

public class UnauthorizedException extends BaseException {

    public UnauthorizedException() {
        super(ErrorCatalog.UNAUTHORIZED);
    }

    public UnauthorizedException(String customMessage) {
        super(ErrorCatalog.UNAUTHORIZED, customMessage);
    }

    public UnauthorizedException(Map<String, String> errors) {
        super(ErrorCatalog.UNAUTHORIZED, errors);
    }

    public UnauthorizedException(String customMessage, Map<String, String> errors) {
        super(ErrorCatalog.UNAUTHORIZED, customMessage, errors);
    }
}
