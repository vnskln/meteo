package pl.zaimeteo.backend.sensor;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorRepository extends JpaRepository<Sensor, Long> {
    List<Sensor> findByRevokedAtIsNull();

    Optional<Sensor> findByIdAndRevokedAtIsNull(Long id);

    Optional<Sensor> findByApiKeyHashAndRevokedAtIsNull(byte[] apiKeyHash);
}
