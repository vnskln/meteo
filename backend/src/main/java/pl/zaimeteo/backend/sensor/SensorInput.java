package pl.zaimeteo.backend.sensor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SensorInput(
        
    @NotBlank @Size(max = 100) String name,
    @NotNull Long seriesId) {}