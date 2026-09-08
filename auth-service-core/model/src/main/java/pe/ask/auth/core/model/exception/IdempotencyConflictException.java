package pe.ask.auth.core.model.exception;

public final class IdempotencyConflictException extends BaseException {
    public IdempotencyConflictException() {
        super(ErrorCatalog.IDEMPOTENCY_CONFLICT);
    }
}
