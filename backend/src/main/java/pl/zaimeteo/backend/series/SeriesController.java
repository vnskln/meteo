package pl.zaimeteo.backend.series;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/series")
public class SeriesController {

    private final SeriesService service;

    public SeriesController(SeriesService service) {
        this.service = service;
    }

    @GetMapping
    public List<Series> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Series get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<Series> create(@Valid @RequestBody SeriesInput input) {
        Series series = service.create(input);
        return ResponseEntity.created(URI.create("/api/series/" + series.getId())).body(series);
    }

    @PutMapping("/{id}")
    public Series update(@PathVariable Long id, @Valid @RequestBody SeriesInput input) {
        return service.update(id, input);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}