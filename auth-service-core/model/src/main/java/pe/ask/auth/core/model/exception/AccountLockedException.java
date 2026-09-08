package pe.ask.auth.core.model.exception;

import java.util.Map;

public class AccountLockedException extends BaseException {

    public AccountLockedException() {
        super(ErrorCatalog.ACCOUNT_LOCKED);
    }

    public AccountLockedException(String customMessage) {
        super(ErrorCatalog.ACCOUNT_LOCKED, customMessage);
    }

    public AccountLockedException(Map<String, String> errors) {
        super(ErrorCatalog.ACCOUNT_LOCKED, errors);
    }

    public AccountLockedException(String customMessage, Map<String, String> errors) {
        super(ErrorCatalog.ACCOUNT_LOCKED, customMessage, errors);
    }
}
