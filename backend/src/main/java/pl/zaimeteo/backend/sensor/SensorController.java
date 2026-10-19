package pl.zaimeteo.backend.sensor;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/sensors")
public class SensorController {
    
    private final SensorService service;

    public SensorController(SensorService service) {
        this.service = service;
    }

    @GetMapping
    public List<SensorResponse> list() {
        List<Sensor> sensors = service.findAll();
        List<SensorResponse> sensorResponses = new ArrayList<SensorResponse>();
        for (Sensor sensor : sensors) {
            SensorResponse sensorResponse = new SensorResponse(
                sensor.getId(),
                sensor.getName(),
                sensor.getSeriesId(),
                sensor.getCreatedAt()
            );
            sensorResponses.add(sensorResponse);
        }
        return sensorResponses;
    }

    @PostMapping
    public ResponseEntity<SensorCreatedResponse> create(@Valid @RequestBody SensorInput input) {
        
        SensorWithKey sensorWithKey = service.create(input);
        SensorCreatedResponse sensorCreatedResponse = new SensorCreatedResponse(
            sensorWithKey.sensor().getId(), sensorWithKey.sensor().getName(),
            sensorWithKey.sensor().getSeriesId(),
            sensorWithKey.sensor().getCreatedAt(),
            sensorWithKey.apiKey());
        return ResponseEntity.created(URI.create("/api/sensors/" + sensorCreatedResponse.id()))
        .body(sensorCreatedResponse);
    }

    @GetMapping("/{id}")
    public SensorResponse getSensorById(@PathVariable Long id) {
        Sensor sensor = service.findById(id);
        SensorResponse sensorResponse = new SensorResponse(
                sensor.getId(),
                sensor.getName(),
                sensor.getSeriesId(),
                sensor.getCreatedAt());
        return sensorResponse;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeSensor(@PathVariable Long id) {
        service.revoke(id);
    }
}