package pl.zaimeteo.backend.error;

// 401
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
