package pe.ask.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.WebExceptionHandler;
import pe.ask.auth.core.model.exception.BaseException;
import pe.ask.auth.core.model.exception.UserAlreadyExistsException;
import pe.ask.auth.input.api.error.ApiUnauthorizedException;
import pe.ask.auth.input.api.error.ApiValidationException;
import pe.ask.auth.input.api.filter.ApiHeaderEnum;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class WebFluxErrorConfigurationTest {

    private WebExceptionHandler webExceptionHandler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        WebFluxErrorConfiguration configuration = new WebFluxErrorConfiguration();
        webExceptionHandler = configuration.webExceptionHandler(objectMapper);
    }

    @Test
    @DisplayName("Should handle BaseException with status, code, title, and detail")
    void shouldHandleBaseException() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/auth/register")
                .header(ApiHeaderEnum.CORRELATION_ID.value(), "corr-12345")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        BaseException baseException = new UserAlreadyExistsException("user@example.com");

        StepVerifier.create(webExceptionHandler.handle(exchange, baseException))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exchange.getResponse().getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(exchange.getResponse().getHeaders().getFirst(ApiHeaderEnum.CORRELATION_ID.value())).isEqualTo("corr-12345");

        String body = extractResponseBody(exchange);
        assertThat(body).contains("\"success\":false");
        assertThat(body).contains("\"code\":\"AUTH_USER_ALREADY_EXISTS\"");
        assertThat(body).contains("\"status\":409");
        assertThat(body).contains("\"user@example.com\"");
        assertThat(body).doesNotContain("\"data\":");
    }

    @Test
    @DisplayName("Should handle ApiValidationException with status 400")
    void shouldHandleApiValidationException() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/auth/login")
                .header(ApiHeaderEnum.REQUEST_ID.value(), "req-67890")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        ApiValidationException exception = new ApiValidationException(
                Map.of("password", "must not be blank")
        );

        StepVerifier.create(webExceptionHandler.handle(exchange, exception))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exchange.getResponse().getHeaders().getFirst(ApiHeaderEnum.CORRELATION_ID.value())).isEqualTo("req-67890");

        String body = extractResponseBody(exchange);
        assertThat(body).contains("\"success\":false");
        assertThat(body).contains("\"code\":\"AUTH_VALIDATION_ERROR\"");
        assertThat(body).contains("\"status\":400");
        assertThat(body).contains("\"password\":\"must not be blank\"");
    }

    @Test
    @DisplayName("Should handle ApiUnauthorizedException with status 401")
    void shouldHandleApiUnauthorizedException() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/auth/me").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        ApiUnauthorizedException exception = new ApiUnauthorizedException("Token has expired");

        StepVerifier.create(webExceptionHandler.handle(exchange, exception))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getResponse().getHeaders().getFirst(ApiHeaderEnum.CORRELATION_ID.value())).isNotBlank();

        String body = extractResponseBody(exchange);
        assertThat(body).contains("\"success\":false");
        assertThat(body).contains("\"code\":\"AUTH_UNAUTHORIZED\"");
        assertThat(body).contains("\"status\":401");
        assertThat(body).contains("Token has expired");
    }

    @Test
    @DisplayName("Should handle ResponseStatusException e.g. 404 Not Found")
    void shouldHandleResponseStatusException() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/unknown").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        ResponseStatusException exception = new ResponseStatusException(HttpStatus.NOT_FOUND, "Route not found");

        StepVerifier.create(webExceptionHandler.handle(exchange, exception))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        String body = extractResponseBody(exchange);
        assertThat(body).contains("\"success\":false");
        assertThat(body).contains("\"status\":404");
        assertThat(body).contains("\"code\":\"HTTP_404\"");
    }

    @Test
    @DisplayName("Should handle generic unexpected exception as 500 Internal Server Error")
    void shouldHandleGenericException() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/auth/register").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        RuntimeException exception = new RuntimeException("relation users does not exist");

        StepVerifier.create(webExceptionHandler.handle(exchange, exception))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        String body = extractResponseBody(exchange);
        assertThat(body).contains("\"success\":false");
        assertThat(body).contains("\"status\":500");
        assertThat(body).contains("\"code\":\"AUTH_INTERNAL_ERROR\"");
        assertThat(body).doesNotContain("\"data\":");
    }

    private String extractResponseBody(MockServerWebExchange exchange) {
        return exchange.getResponse().getBodyAsString().block();
    }
}
