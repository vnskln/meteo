package pl.zaimeteo.backend.series;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import pl.zaimeteo.backend.ApiTestSupport;

class SeriesControllerTest extends ApiTestSupport {

    @Test
    void createWithMinEqualToMaxReturns422() throws Exception {
        assertThat(mvc.post().uri("/api/series").contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + adminToken())
                .content(seriesJson(5, 5)))
                .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @Test
    void getMissingSeriesReturns404AsProblemDetail() {
        assertThat(mvc.get().uri("/api/series/999999999"))
                .hasStatus(HttpStatus.NOT_FOUND)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyJson().extractingPath("$.status").isEqualTo(404);
    }

    @Test
    void updateKeepingExistingMeasurementsInRangeReturns200() throws Exception {
        Long seriesId = createSeries(0, 100);
        sendMeasurement(createSensorKey(seriesId), 21.5, "2026-01-10T10:00:00Z");

        assertThat(mvc.put().uri("/api/series/" + seriesId).contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + adminToken())
                .content(seriesJson(21.5, 30)))
                .hasStatusOk()
                .bodyJson().extractingPath("$.minValue").isEqualTo(21.5);
    }

    @Test
    void deleteRemovesSeriesWithItsMeasurements() throws Exception {
        Long seriesId = createSeries(0, 100);
        MvcTestResult measurement = sendMeasurement(createSensorKey(seriesId), 10, "2026-01-10T10:00:00Z");
        String measurementLocation = measurement.getResponse().getHeader("Location");

        assertThat(mvc.delete().uri("/api/series/" + seriesId).header("Authorization", "Bearer " + adminToken()))
                .hasStatus(HttpStatus.NO_CONTENT);
        assertThat(mvc.get().uri("/api/series/" + seriesId)).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(mvc.get().uri(measurementLocation)).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateExcludingExistingMeasurementsReturns409() throws Exception {
        Long seriesId = createSeries(0, 100);
        sendMeasurement(createSensorKey(seriesId), 21.5, "2026-01-10T10:00:00Z");
        assertThat(mvc.put().uri("/api/series/" + seriesId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + adminToken())
                .content(seriesJson(50, 60)))
                .hasStatus(HttpStatus.CONFLICT);
        assertThat(mvc.get().uri("/api/series/" + seriesId))
                .hasStatusOk()
                .bodyJson().extractingPath("$.minValue").isEqualTo(0.0);
    }
}
