package pe.ask.auth.input.api.filter;

import org.jspecify.annotations.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Order(Ordered.HIGHEST_PRECEDENCE)
public final class TraceabilityWebFilter implements WebFilter {

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        ApiValidation.requireNonNull(exchange, ApiMessageEnum.PARAM_ARGUMENT.value());
        ApiValidation.requireNonNull(chain, ApiMessageEnum.PARAM_ARGUMENT.value());

        String correlationId = exchange.getRequest().getHeaders().getFirst(ApiHeaderEnum.CORRELATION_ID.value());
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = generateId();
        }

        String requestId = exchange.getRequest().getHeaders().getFirst(ApiHeaderEnum.REQUEST_ID.value());
        if (requestId == null || requestId.isBlank()) {
            requestId = generateId();
        }

        final String finalCorrelationId = correlationId;
        final String finalRequestId = requestId;

        exchange.getResponse().getHeaders().set(ApiHeaderEnum.CORRELATION_ID.value(), finalCorrelationId);
        exchange.getResponse().getHeaders().set(ApiHeaderEnum.REQUEST_ID.value(), finalRequestId);

        String idempotencyKey = exchange.getRequest().getHeaders().getFirst(ApiHeaderEnum.IDEMPOTENCY_KEY.value());
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            exchange.getResponse().getHeaders().set(ApiHeaderEnum.IDEMPOTENCY_KEY.value(), idempotencyKey);
        }

        return chain.filter(exchange)
                .contextWrite(context -> context
                        .put(ApiMessageEnum.CONTEXT_CORRELATION_ID.value(), finalCorrelationId)
                        .put(ApiMessageEnum.CONTEXT_REQUEST_ID.value(), finalRequestId));
    }

    private String generateId() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return new UUID(random.nextLong(), random.nextLong()).toString();
    }
}
