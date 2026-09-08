package pe.ask.auth.input.api.error;

public class ApiUnauthorizedException extends RuntimeException {

    private final String errorCode;
    private final String title;
    private final int httpStatus;

    public ApiUnauthorizedException(String message) {
        super(message != null ? message : "Authentication required");
        this.errorCode = "AUTH_UNAUTHORIZED";
        this.title = "Unauthorized";
        this.httpStatus = 401;
    }

    public ApiUnauthorizedException() {
        this("Authentication required");
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getTitle() {
        return title;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
