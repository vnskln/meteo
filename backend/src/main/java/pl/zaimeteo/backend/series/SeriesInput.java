package pl.zaimeteo.backend.series;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SeriesInput(
        
    @NotBlank @Size(max = 100) String name,
    @NotNull Double minValue,
    @NotNull Double maxValue,
    @NotBlank @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String color,
    @Size(max = 100) String icon,
    @Size(max = 20) String unit) {}