package pl.zaimeteo.backend.measurement;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MeasurementRepository extends JpaRepository<Measurement, Long>{   
    
    boolean existsBySeriesIdAndValueLessThan(Long seriesId, Double value);

    boolean existsBySeriesIdAndValueGreaterThan(Long seriesId, Double value);

}