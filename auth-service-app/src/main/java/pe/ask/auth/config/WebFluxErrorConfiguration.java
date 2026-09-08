package pe.ask.auth.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.WebExceptionHandler;
import pe.ask.auth.core.model.exception.BaseException;
import pe.ask.auth.input.api.error.ApiUnauthorizedException;
import pe.ask.auth.input.api.error.ApiValidationException;
import pe.ask.auth.input.api.filter.ApiHeaderEnum;
import pe.ask.auth.input.api.filter.ApiMessageEnum;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Configuration(proxyBeanMethods = false)
public final class WebFluxErrorConfiguration {

    private static final Logger log = LoggerFactory.getLogger(WebFluxErrorConfiguration.class);

    @Bean
    @Order(-2)
    public WebExceptionHandler webExceptionHandler(ObjectMapper objectMapper) {
        return (exchange, ex) -> {
            int status = resolveStatus(ex);
            String errorCode = resolveErrorCode(ex);
            String title = resolveTitle(ex);
            String detail = resolveDetail(ex);
            Map<String, String> errors = resolveErrors(ex);

            String correlationId = exchange.getRequest().getHeaders().getFirst(ApiHeaderEnum.CORRELATION_ID.value());
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = exchange.getRequest().getHeaders().getFirst(ApiHeaderEnum.REQUEST_ID.value());
            }
            if (correlationId == null || correlationId.isBlank()) {
                ThreadLocalRandom random = ThreadLocalRandom.current();
                correlationId = new UUID(random.nextLong(), random.nextLong()).toString();
            }

            if (status >= HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                log.error("[{}] Internal server error processing request [{} {}]: {}",
                        correlationId, exchange.getRequest().getMethod(), exchange.getRequest().getPath(), ex.getMessage(), ex);
            } else {
                log.warn("[{}] Handled client error [{} {}]: status={}, code={}, message={}",
                        correlationId, exchange.getRequest().getMethod(), exchange.getRequest().getPath(), status, errorCode, detail);
            }

            exchange.getResponse().setStatusCode(HttpStatusCode.valueOf(status));
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            exchange.getResponse().getHeaders().set(ApiHeaderEnum.CORRELATION_ID.value(), correlationId);

            Map<String, Object> errorMap = new LinkedHashMap<>();
            errorMap.put("code", errorCode);
            errorMap.put("message", detail);
            errorMap.put("title", title);
            errorMap.put("status", status);
            if (!errors.isEmpty()) {
                errorMap.put("errors", errors);
            }

            Map<String, Object> responseMap = new LinkedHashMap<>();
            responseMap.put("success", false);
            responseMap.put("error", errorMap);
            responseMap.put("timestamp", Instant.now().toString());

            try {
                byte[] bytes = objectMapper.writeValueAsString(responseMap).getBytes(StandardCharsets.UTF_8);
                DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
                return exchange.getResponse().writeWith(Mono.just(buffer));
            } catch (JsonProcessingException e) {
                log.error("[{}] Error serializing error response to JSON: {}", correlationId, e.getMessage(), e);
                return exchange.getResponse().setComplete();
            }
        };
    }

    private int resolveStatus(Throwable ex) {
        if (ex instanceof BaseException baseEx) {
            return baseEx.getStatus();
        }
        if (ex instanceof ApiValidationException valEx) {
            return valEx.getHttpStatus();
        }
        if (ex instanceof ApiUnauthorizedException unauthEx) {
            return unauthEx.getHttpStatus();
        }
        if (ex instanceof ResponseStatusException rse) {
            return rse.getStatusCode().value();
        }
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

    private String resolveErrorCode(Throwable ex) {
        if (ex instanceof BaseException baseEx) {
            return baseEx.getErrorCode();
        }
        if (ex instanceof ApiValidationException valEx) {
            return valEx.getErrorCode();
        }
        if (ex instanceof ApiUnauthorizedException unauthEx) {
            return unauthEx.getErrorCode();
        }
        if (ex instanceof ResponseStatusException rse) {
            return "HTTP_" + rse.getStatusCode().value();
        }
        return ApiMessageEnum.ERROR_CODE_INTERNAL_ERROR.value();
    }

    private String resolveTitle(Throwable ex) {
        if (ex instanceof BaseException baseEx) {
            return baseEx.getTitle();
        }
        if (ex instanceof ApiValidationException valEx) {
            return valEx.getTitle();
        }
        if (ex instanceof ApiUnauthorizedException unauthEx) {
            return unauthEx.getTitle();
        }
        if (ex instanceof ResponseStatusException rse) {
            return HttpStatus.resolve(rse.getStatusCode().value()) != null
                    ? HttpStatus.resolve(rse.getStatusCode().value()).getReasonPhrase()
                    : ApiMessageEnum.TITLE_INTERNAL_SERVER_ERROR.value();
        }
        return ApiMessageEnum.TITLE_INTERNAL_SERVER_ERROR.value();
    }

    private String resolveDetail(Throwable ex) {
        if (ex instanceof BaseException baseEx) {
            return baseEx.getMessage();
        }
        if (ex instanceof ApiValidationException valEx) {
            return valEx.getMessage();
        }
        if (ex instanceof ApiUnauthorizedException unauthEx) {
            return unauthEx.getMessage();
        }
        if (ex instanceof ResponseStatusException rse && rse.getReason() != null) {
            return rse.getReason();
        }
        return ApiMessageEnum.INTERNAL_SERVER_ERROR.value();
    }

    private Map<String, String> resolveErrors(Throwable ex) {
        if (ex instanceof BaseException baseEx && baseEx.getErrors() != null) {
            return baseEx.getErrors();
        }
        if (ex instanceof ApiValidationException valEx && valEx.getErrors() != null) {
            return valEx.getErrors();
        }
        return Map.of();
    }
}
