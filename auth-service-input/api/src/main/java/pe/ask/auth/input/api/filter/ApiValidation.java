package pe.ask.auth.input.api.filter;

import pe.ask.auth.input.api.error.ApiValidationException;

import java.util.Map;

public final class ApiValidation {

    private ApiValidation() {
        // Prevent instantiation
    }

    public static <T> T requireNonNull(T obj, String fieldName) {
        if (obj == null) {
            String effectiveName = fieldName != null ? fieldName : ApiMessageEnum.PARAM_ARGUMENT.value();
            throw new ApiValidationException(Map.of(effectiveName, effectiveName + " " + ApiMessageEnum.VALIDATION_FAILED.value()));
        }
        return obj;
    }
}
