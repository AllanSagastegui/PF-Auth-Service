package pe.ask.auth.core.model.exception;

public final class DatabaseOperationException extends BaseException {

    public DatabaseOperationException(String message, Throwable cause) {
        super(
                ErrorCatalog.INTERNAL_SERVER_ERROR.getErrorCode(),
                "DatabaseOperationException",
                message,
                500,
                null,
                cause
        );
    }

    public DatabaseOperationException(String message) {
        this(message, null);
    }
}
