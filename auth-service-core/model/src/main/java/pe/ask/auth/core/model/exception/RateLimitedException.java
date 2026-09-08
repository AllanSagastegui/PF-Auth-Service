package pe.ask.auth.core.model.exception;

public final class RateLimitedException extends BaseException {
    public RateLimitedException() {
        super(ErrorCatalog.RATE_LIMITED);
    }
}
