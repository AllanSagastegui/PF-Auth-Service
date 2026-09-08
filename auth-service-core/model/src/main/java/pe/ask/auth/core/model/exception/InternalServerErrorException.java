package pe.ask.auth.core.model.exception;

import java.util.Map;

public class InternalServerErrorException extends BaseException {

    public InternalServerErrorException() {
        super(ErrorCatalog.INTERNAL_SERVER_ERROR);
    }

    public InternalServerErrorException(String customMessage) {
        super(ErrorCatalog.INTERNAL_SERVER_ERROR, customMessage);
    }

    public InternalServerErrorException(Map<String, String> errors) {
        super(ErrorCatalog.INTERNAL_SERVER_ERROR, errors);
    }

    public InternalServerErrorException(String customMessage, Map<String, String> errors) {
        super(ErrorCatalog.INTERNAL_SERVER_ERROR, customMessage, errors);
    }

    public InternalServerErrorException(Throwable cause) {
        super(ErrorCatalog.INTERNAL_SERVER_ERROR, cause);
    }

    public InternalServerErrorException(String customMessage, Throwable cause) {
        super(ErrorCatalog.INTERNAL_SERVER_ERROR, customMessage, cause);
    }
}
