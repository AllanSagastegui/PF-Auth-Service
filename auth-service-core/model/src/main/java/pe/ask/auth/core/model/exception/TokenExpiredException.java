package pe.ask.auth.core.model.exception;

import java.util.Map;

public class TokenExpiredException extends BaseException {

    public TokenExpiredException() {
        super(ErrorCatalog.TOKEN_EXPIRED);
    }

    public TokenExpiredException(String customMessage) {
        super(ErrorCatalog.TOKEN_EXPIRED, customMessage);
    }

    public TokenExpiredException(Map<String, String> errors) {
        super(ErrorCatalog.TOKEN_EXPIRED, errors);
    }

    public TokenExpiredException(String customMessage, Map<String, String> errors) {
        super(ErrorCatalog.TOKEN_EXPIRED, customMessage, errors);
    }
}
