package pl.zaimeteo.backend.sensor;

public record SensorWithKey(
        
    Sensor sensor,
    String apiKey) {}