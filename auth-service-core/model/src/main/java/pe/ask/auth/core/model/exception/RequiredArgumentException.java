package pe.ask.auth.core.model.exception;

import pe.ask.auth.core.model.constant.DomainFieldEnum;
import pe.ask.auth.core.model.constant.DomainMessageEnum;

import java.util.Map;

public final class RequiredArgumentException extends BaseException {

    public RequiredArgumentException(String argumentName) {
        super(
                ErrorCatalog.REQUIRED_ARGUMENT.getErrorCode(),
                ErrorCatalog.REQUIRED_ARGUMENT.getExceptionName(),
                DomainFieldEnum.ARGUMENT_PREFIX.value() + argumentName + DomainFieldEnum.ARGUMENT_SUFFIX.value() + DomainMessageEnum.ARGUMENT_MUST_NOT_BE_NULL.value(),
                ErrorCatalog.REQUIRED_ARGUMENT.getStatus(),
                Map.of(argumentName != null ? argumentName : DomainFieldEnum.ARGUMENT.value(), DomainMessageEnum.ARGUMENT_MUST_NOT_BE_NULL.value())
        );
    }
}
