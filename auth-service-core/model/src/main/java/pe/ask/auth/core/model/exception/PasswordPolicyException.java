package pe.ask.auth.core.model.exception;

import java.util.Map;

public final class PasswordPolicyException extends BaseException {
    public PasswordPolicyException(Map<String, String> errors) {
        super(ErrorCatalog.PASSWORD_POLICY, errors);
    }
}
