package pl.zaimeteo.backend.series;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import pl.zaimeteo.backend.error.NotFoundException;
import pl.zaimeteo.backend.error.UnprocessableException;


@Service
public class SeriesService {

    private final SeriesRepository repository;

    public SeriesService(SeriesRepository repository) {
        this.repository = repository;
    }

    public List<Series> findAll() {
        return repository.findAll();
    }

    public Series findById(Long id) {
        Optional<Series> series = repository.findById(id);
        if (series.isEmpty()) {
            throw new NotFoundException("Series " + id + " not found");
        }
        return series.get();
    }

    public Series create(SeriesInput input) {
        checkMinMaxValues(input);
        Series series = new Series();
        fill(series, input);
        return repository.save(series);
    }

    public Series update(Long id, SeriesInput input) {
        checkMinMaxValues(input);
        Series series = findById(id);
        fill(series, input);
        return repository.save(series);
    }

    public void delete(Long id) {
        Series series = findById(id);
        repository.delete(series);
    }

    private void checkMinMaxValues(SeriesInput input) {
        if (input.minValue() >= input.maxValue()) {
            throw new UnprocessableException("Min value can't be bigger than or equal max value");
        }
    }

    private void fill(Series series, SeriesInput input) {
        series.setName(input.name());
        series.setMaxValue(input.maxValue());
        series.setMinValue(input.minValue());
        series.setColor(input.color());
        series.setIcon(input.icon());
        series.setUnit(input.unit());
    }
}