package pe.ask.auth.input.api.filter;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import pe.ask.auth.input.api.error.ApiValidationException;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

public final class CustomRequestValidator {

    private final Validator validator;

    public CustomRequestValidator(Validator validator) {
        this.validator = ApiValidation.requireNonNull(validator, "validator");
    }

    public <T> Mono<T> validate(T obj) {
        ApiValidation.requireNonNull(obj, "obj");
        return Mono.fromCallable(() -> validator.validate(obj))
                .flatMap(violations -> violations.isEmpty()
                        ? Mono.just(obj)
                        : Mono.error(new ApiValidationException(
                        violations.stream()
                                .collect(Collectors.toMap(
                                        v -> v.getPropertyPath().toString(),
                                        ConstraintViolation::getMessage,
                                        (msg1, msg2) -> msg1
                                ))
                )));
    }
}
