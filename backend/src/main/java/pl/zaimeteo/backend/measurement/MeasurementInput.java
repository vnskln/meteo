package pl.zaimeteo.backend.measurement;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;

public record MeasurementInput(
    @NotNull Double value,
    Instant timestamp) {}