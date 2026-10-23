package pl.zaimeteo.backend.measurement;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "measurements")
public class Measurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long seriesId;
    private Long sensorId;
    private Double value;

    @Column(name = "measured_at")
    private Instant timestamp;

    public Long getId() { return id; }
    public Long getSeriesId() { return seriesId; }
    public Long getSensorId() { return sensorId; }
    public Double getValue() { return value; }
    public Instant getTimestamp() { return timestamp; }

    public void setSeriesId(Long seriesId) { this.seriesId = seriesId; }
    public void setSensorId(Long sensorId) { this.sensorId = sensorId; }
    public void setValue(Double value) { this.value = value; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
