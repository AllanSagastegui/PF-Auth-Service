package pe.ask.auth.core.model.exception;

public final class TokenInitializationException extends BaseException {

    public TokenInitializationException(String message, Throwable cause) {
        super(
                ErrorCatalog.INTERNAL_SERVER_ERROR.getErrorCode(),
                "TokenInitializationException",
                message,
                500,
                null,
                cause
        );
    }
}
