package pl.zaimeteo.backend.sensor;

import java.time.Instant;

public record SensorCreatedResponse(
    
    Long id, 
    String name, 
    Long seriesId, 
    Instant createdAt, 
    String apiKey) {}
