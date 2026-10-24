package pl.zaimeteo.backend.auth;

public record LoginResponse(
    String accessToken,
    String tokenType,
    int expiresIn) {}
