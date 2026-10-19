package pl.zaimeteo.backend.sensor;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "sensors")
public class Sensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long seriesId;
    private String name;
    private byte[] apiKeyHash;
    private Instant createdAt;
    private Instant revokedAt;

    public Long getId() { return id; }
    public Long getSeriesId () {return seriesId;}
    public String getName() { return name; }
    public byte[] getApiKeyHash() { return apiKeyHash; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getRevokedAt() { return revokedAt; }
 
    public void setSeriesId(Long seriesId) { this.seriesId = seriesId; }
    public void setName(String name) { this.name = name; }
    public void setApiKeyHash(byte[] apiKeyHash) { this.apiKeyHash = apiKeyHash; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setRevokedAt(Instant revokedAt) { this.revokedAt = revokedAt; }
}
