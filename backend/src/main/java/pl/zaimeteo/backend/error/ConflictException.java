package pl.zaimeteo.backend.error;

// 409
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
