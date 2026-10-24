package pl.zaimeteo.backend.error;

public record FieldViolation(
    String field,
    String message) {}
