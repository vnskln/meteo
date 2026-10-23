package pl.zaimeteo.backend.measurement;

import java.net.URI;
import java.time.Instant;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

@RestController
@RequestMapping("/api/measurements")
public class MeasurementController {

    private final MeasurementService service;

    public MeasurementController(MeasurementService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public Measurement get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<Measurement> create(
            @RequestHeader(name = "X-API-Key", required = false) String apiKey,
            @Valid @RequestBody MeasurementInput input) {

        Measurement measurement = service.create(apiKey, input);
        return ResponseEntity.created(URI.create("/api/measurements/" + measurement.getId())).body(measurement);
    }

    @GetMapping
    public List<Measurement> list(
            @RequestParam(required = false) List<Long> series,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(defaultValue = "timestamp") @Pattern(regexp = "-?timestamp") String sort,
            @RequestParam(defaultValue = "1000") @Min(1) @Max(10000) int limit) {
        return service.find(series, from, to, sort, limit);
    }

}