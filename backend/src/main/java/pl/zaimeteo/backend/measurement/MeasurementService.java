package pl.zaimeteo.backend.measurement;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import pl.zaimeteo.backend.error.NotFoundException;
import pl.zaimeteo.backend.error.UnauthorizedException;
import pl.zaimeteo.backend.error.UnprocessableException;
import pl.zaimeteo.backend.sensor.Sensor;
import pl.zaimeteo.backend.sensor.SensorService;
import pl.zaimeteo.backend.series.Series;
import pl.zaimeteo.backend.series.SeriesService;

@Service
public class MeasurementService {

    private static final Logger log = LoggerFactory.getLogger(MeasurementService.class);

    private final MeasurementRepository repository;
    private final SensorService sensorService;
    private final SeriesService seriesService;
    private final EntityManager entityManager;

    public MeasurementService(MeasurementRepository repository, SensorService sensorService,
            SeriesService seriesService, EntityManager entityManager) {
        this.repository = repository;
        this.sensorService = sensorService;
        this.seriesService = seriesService;
        this.entityManager = entityManager;
    }

    public Measurement findById(Long id) {
        Optional<Measurement> measurement = repository.findById(id);
        if (measurement.isEmpty()) {
            throw new NotFoundException("Measurement " + id + " not found");
        }
        return measurement.get();
    }

    public Measurement create(String apiKey, MeasurementInput input) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new UnauthorizedException("Invalid API key");
        }
        Sensor sensor = sensorService.findByApiKey(apiKey);
        Series series = seriesService.findById(sensor.getSeriesId());

        Instant now = Instant.now();
        Instant timestamp = input.timestamp();
        if (timestamp == null) {
            timestamp = now;
        } else if (timestamp.isAfter(now.plusSeconds(5 * 60))) {
            log.warn("Rejected measurement from sensor {}: timestamp {} is in the future",
                    sensor.getId(), timestamp);
            throw new UnprocessableException("Timestamp is more than 5 minutes in the future");
        }

        if (input.value() < series.getMinValue() || input.value() > series.getMaxValue()) {
            log.warn("Rejected measurement from sensor {}: value {} outside [{}, {}]",
                    sensor.getId(), input.value(), series.getMinValue(), series.getMaxValue());
            throw new UnprocessableException("Value " + input.value() + " is outside ["
                    + series.getMinValue() + ", " + series.getMaxValue() + "]");
        }

        Measurement measurement = new Measurement();
        measurement.setSeriesId(series.getId());
        measurement.setSensorId(sensor.getId());
        measurement.setValue(input.value());
        measurement.setTimestamp(timestamp);
        return repository.save(measurement);
    }

    public List<Measurement> find(List<Long> series, Instant from, 
        Instant to, String sort, int limit) {
        String jpql = "SELECT m FROM Measurement m WHERE 1 = 1";
        if (series != null) {
            jpql += " AND m.seriesId IN :series";
        }
        if (from != null) {
            jpql += " AND m.timestamp >= :from";
        }
        if (to != null) {
            jpql += " AND m.timestamp <= :to";
        }
        if (sort.equals("-timestamp")) {
            jpql += " ORDER BY m.timestamp DESC, m.id DESC";
        } else {
            jpql += " ORDER BY m.timestamp, m.id";
        }

        TypedQuery<Measurement> query = entityManager.createQuery(jpql, Measurement.class);
        if (series != null) {
            query.setParameter("series", series);
        }
        if (from != null) {
            query.setParameter("from", from);
        }
        if (to != null) {
            query.setParameter("to", to);
        }
        query.setMaxResults(limit);
        return query.getResultList();
    }

}
