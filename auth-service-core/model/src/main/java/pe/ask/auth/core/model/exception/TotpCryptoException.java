package pe.ask.auth.core.model.exception;

public final class TotpCryptoException extends BaseException {

    public TotpCryptoException(String message, Throwable cause) {
        super(
                ErrorCatalog.INTERNAL_SERVER_ERROR.getErrorCode(),
                "TotpCryptoException",
                message,
                500,
                null,
                cause
        );
    }

    public TotpCryptoException(String message) {
        this(message, null);
    }
}
