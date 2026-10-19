package pl.zaimeteo.backend.sensor;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import pl.zaimeteo.backend.auth.Tokens;
import pl.zaimeteo.backend.error.NotFoundException;
import pl.zaimeteo.backend.error.UnauthorizedException;
import pl.zaimeteo.backend.error.UnprocessableException;
import pl.zaimeteo.backend.series.SeriesRepository;

@Service
public class SensorService {

    private final SensorRepository sensorRepository;
    private final SeriesRepository seriesRepository;

    public SensorService(SensorRepository sensorRepository, SeriesRepository seriesRepository) {
        this.sensorRepository = sensorRepository;
        this.seriesRepository = seriesRepository;
    }

    public List<Sensor> findAll() {
        return sensorRepository.findByRevokedAtIsNull();
    }

    public Sensor findById(Long id) {
        Optional<Sensor> sensor = sensorRepository.findByIdAndRevokedAtIsNull(id);
        if (sensor.isEmpty()) {
            throw new NotFoundException("Sensor " + id + " not found");
        }
        return sensor.get();
    }

    public SensorWithKey create(SensorInput input) {
        if (seriesRepository.existsById(input.seriesId())) {
            Sensor sensor = new Sensor();
            String apiKey = Tokens.generate();
            byte[] apiKeyHash = Tokens.hash(apiKey);
            sensor.setApiKeyHash(apiKeyHash);
            sensor.setCreatedAt(Instant.now());
            sensor.setName(input.name());
            sensor.setSeriesId(input.seriesId());
            sensorRepository.save(sensor);
            return new SensorWithKey(sensor, apiKey);
        } else {
            throw new UnprocessableException("Series with id " + input.seriesId() + " not found");
        }
    }

    public void revoke (Long id) {
        Optional<Sensor> sensor = sensorRepository.findByIdAndRevokedAtIsNull(id);
        if (sensor.isPresent()) {
            sensor.get().setRevokedAt(Instant.now());
            sensorRepository.save(sensor.get());
        } else {
            throw new NotFoundException("Sensor with id " + id + " not found");
        }
    }

    public Sensor findByApiKey (String apiKey) {
        Optional<Sensor> sensor = sensorRepository
        .findByApiKeyHashAndRevokedAtIsNull(Tokens.hash(apiKey));
        if (sensor.isPresent()) {
            return sensor.get();
        } else {
            throw new UnauthorizedException("Invalid API key");
        }
    }    
}