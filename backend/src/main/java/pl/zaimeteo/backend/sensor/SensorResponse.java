package pl.zaimeteo.backend.sensor;

import java.time.Instant;


public record SensorResponse(
    
    Long id, 
    String name, 
    Long seriesId, 
    Instant createdAt) {}
