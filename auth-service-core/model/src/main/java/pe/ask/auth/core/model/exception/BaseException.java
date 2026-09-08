package pe.ask.auth.core.model.exception;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;

public class BaseException extends RuntimeException {
    private final String errorCode;
    private final String title;
    private final String message;
    private final int status;
    private final LocalDateTime timestamp;
    private final Map<String, String> errors;

    protected BaseException(String errorCode, String title, String message, int status, Map<String, String> errors) {
        this(errorCode, title, message, status, errors, Clock.systemDefaultZone(), null);
    }

    protected BaseException(String errorCode, String title, String message, int status, Map<String, String> errors, Clock clock) {
        this(errorCode, title, message, status, errors, clock, null);
    }

    protected BaseException(String errorCode, String title, String message, int status, Map<String, String> errors, Throwable cause) {
        this(errorCode, title, message, status, errors, Clock.systemDefaultZone(), cause);
    }

    protected BaseException(String errorCode, String title, String message, int status, Map<String, String> errors, Clock clock, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.title = title;
        this.message = message;
        this.status = status;
        this.timestamp = LocalDateTime.now(clock != null ? clock : Clock.systemDefaultZone());
        this.errors = errors;
    }

    protected BaseException(ErrorCatalog catalog) {
        this(catalog.getErrorCode(), catalog.getExceptionName(), catalog.getMessage(), catalog.getStatus(), catalog.getErrors(), Clock.systemDefaultZone(), null);
    }

    protected BaseException(ErrorCatalog catalog, Map<String, String> errors) {
        this(catalog.getErrorCode(), catalog.getExceptionName(), catalog.getMessage(), catalog.getStatus(), errors, Clock.systemDefaultZone(), null);
    }

    protected BaseException(ErrorCatalog catalog, String customMessage) {
        this(catalog.getErrorCode(), catalog.getExceptionName(), customMessage, catalog.getStatus(), catalog.getErrors(), Clock.systemDefaultZone(), null);
    }

    protected BaseException(ErrorCatalog catalog, String customMessage, Map<String, String> errors) {
        this(catalog.getErrorCode(), catalog.getExceptionName(), customMessage, catalog.getStatus(), errors, Clock.systemDefaultZone(), null);
    }

    protected BaseException(ErrorCatalog catalog, Throwable cause) {
        this(catalog.getErrorCode(), catalog.getExceptionName(), catalog.getMessage(), catalog.getStatus(), catalog.getErrors(), Clock.systemDefaultZone(), cause);
    }

    protected BaseException(ErrorCatalog catalog, String customMessage, Throwable cause) {
        this(catalog.getErrorCode(), catalog.getExceptionName(), customMessage, catalog.getStatus(), catalog.getErrors(), Clock.systemDefaultZone(), cause);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
