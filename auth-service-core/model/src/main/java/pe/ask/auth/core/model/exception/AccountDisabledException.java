package pe.ask.auth.core.model.exception;

import java.util.Map;

public class AccountDisabledException extends BaseException {

    public AccountDisabledException() {
        super(ErrorCatalog.ACCOUNT_DISABLED);
    }

    public AccountDisabledException(String customMessage) {
        super(ErrorCatalog.ACCOUNT_DISABLED, customMessage);
    }

    public AccountDisabledException(Map<String, String> errors) {
        super(ErrorCatalog.ACCOUNT_DISABLED, errors);
    }

    public AccountDisabledException(String customMessage, Map<String, String> errors) {
        super(ErrorCatalog.ACCOUNT_DISABLED, customMessage, errors);
    }
}
