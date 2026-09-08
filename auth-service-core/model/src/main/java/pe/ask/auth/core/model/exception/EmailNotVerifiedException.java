package pe.ask.auth.core.model.exception;

public final class EmailNotVerifiedException extends BaseException {
    public EmailNotVerifiedException() {
        super(ErrorCatalog.EMAIL_NOT_VERIFIED);
    }
}
