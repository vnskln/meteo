package pl.zaimeteo.backend.error;

// 422 - request breaks a business rule
public class UnprocessableException extends RuntimeException {

    public UnprocessableException(String message) {
        super(message);
    }
}
