package pe.ask.auth.input.api.filter;

import org.springframework.web.reactive.function.server.ServerRequest;
import pe.ask.auth.input.api.error.ApiUnauthorizedException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

public final class SecurityRequestExtractor {

    private static final String JSON_SUB_KEY = "\"sub\":\"";
    private static final String QUOTE = "\"";
    private static final String DOT_REGEX = "\\.";

    private SecurityRequestExtractor() {
    }

    public static UUID extractUserId(ServerRequest request) {
        ApiValidation.requireNonNull(request, ApiMessageEnum.PARAM_ARGUMENT.value());

        String authHeader = request.headers().firstHeader(ApiHeaderEnum.AUTHORIZATION.value());
        if (authHeader != null && authHeader.regionMatches(true, 0, ApiHeaderEnum.BEARER_PREFIX.value(), 0, ApiHeaderEnum.BEARER_PREFIX.value().length())) {
            String token = authHeader.substring(ApiHeaderEnum.BEARER_PREFIX.value().length()).trim();
            String[] parts = token.split(DOT_REGEX);
            if (parts.length >= 2) {
                try {
                    byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
                    String json = new String(decoded, StandardCharsets.UTF_8);
                    int subIndex = json.indexOf(JSON_SUB_KEY);
                    if (subIndex != -1) {
                        int start = subIndex + JSON_SUB_KEY.length();
                        int end = json.indexOf(QUOTE, start);
                        if (end != -1) {
                            return UUID.fromString(json.substring(start, end));
                        }
                    }
                } catch (IllegalArgumentException | IndexOutOfBoundsException ignored) {
                    // Fallthrough to X-User-Id header or reject
                }
            }
        }

        String userIdHeader = request.headers().firstHeader(ApiHeaderEnum.USER_ID.value());
        if (userIdHeader != null && !userIdHeader.isBlank()) {
            try {
                return UUID.fromString(userIdHeader.trim());
            } catch (IllegalArgumentException e) {
                throw new ApiUnauthorizedException(ApiMessageEnum.INVALID_USER_ID_HEADER.value());
            }
        }

        throw new ApiUnauthorizedException(ApiMessageEnum.AUTHENTICATION_REQUIRED.value());
    }
}
