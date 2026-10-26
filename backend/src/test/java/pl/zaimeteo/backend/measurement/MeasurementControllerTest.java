package pl.zaimeteo.backend.measurement;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import com.jayway.jsonpath.JsonPath;

import pl.zaimeteo.backend.ApiTestSupport;

class MeasurementControllerTest extends ApiTestSupport {

    @Test
    void valueOutsideRangeReturns422AndIsNotSaved() throws Exception {
        Long seriesId = createSeries(0, 10);

        assertThat(sendMeasurement(createSensorKey(seriesId), 11, "2026-01-10T10:00:00Z"))
                .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT)
                .hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(mvc.get().uri("/api/measurements?series=" + seriesId))
                .hasStatusOk()
                .bodyJson().isEqualTo("[]");
    }

    @Test
    void timestampMoreThanFiveMinutesAheadReturns422() throws Exception {
        String inOneHour = Instant.now().plus(Duration.ofHours(1)).toString();

        assertThat(sendMeasurement(createSensorKey(createSeries(0, 10)), 5, inOneHour))
                .hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @Test
    void missingTimestampUsesServerTime() throws Exception {
        Instant before = Instant.now();
        MvcTestResult result = sendMeasurement(createSensorKey(createSeries(0, 10)), 5, null);

        assertThat(result).hasStatus(HttpStatus.CREATED).containsHeader("Location");
        Instant timestamp = Instant.parse(JsonPath.read(body(result), "$.timestamp"));
        assertThat(timestamp).isBetween(before, Instant.now());
    }

    @Test
    void adminTokenWithoutApiKeyReturns401() throws Exception {
        createSeries(0, 10);

        assertThat(mvc.post().uri("/api/measurements").contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + adminToken())
                .content("{\"value\":5}"))
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void sortDescendingReturnsNewestFirst() throws Exception {
        Long seriesId = createSeries(0, 10);
        String apiKey = createSensorKey(seriesId);
        sendMeasurement(apiKey, 1, "2026-01-10T10:00:00Z");
        sendMeasurement(apiKey, 2, "2026-01-10T12:00:00Z");
        sendMeasurement(apiKey, 3, "2026-01-10T11:00:00Z");

        assertThat(mvc.get().uri("/api/measurements?series=" + seriesId + "&sort=-timestamp"))
                .hasStatusOk()
                .bodyJson().extractingPath("$[*].value").asArray().containsExactly(2.0, 3.0, 1.0);
    }

    @Test
    void invalidFromReturns400() {
        assertThat(mvc.get().uri("/api/measurements?from=not-a-date")).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void limitAbove10000Returns400() {
        assertThat(mvc.get().uri("/api/measurements?limit=10001")).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void valuesOnRangeBoundariesAreAccepted() throws Exception  {
        Long seriesId = createSeries(-5, 5);
        String apiKey = createSensorKey(seriesId);
        sendMeasurement(apiKey, -5, "2026-01-10T10:00:00Z");
        sendMeasurement(apiKey, 5, "2026-01-10T11:00:00Z");
        assertThat(mvc.get().uri("/api/measurements?series=" + seriesId)).hasStatusOk()
                .bodyJson().extractingPath("$[*].value").asArray().containsExactly(-5.0, 5.0); 
    }

    @Test
    void measurementCannotBeChanged() throws Exception {
        MvcTestResult created = sendMeasurement(createSensorKey(createSeries(0, 10)), 5, "2026-01-10T10:00:00Z");
        String location = created.getResponse().getHeader("Location");

        assertThat(mvc.put().uri(location).contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + adminToken())
                .content("{\"value\":6}"))
                .hasStatus(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(mvc.delete().uri(location).header("Authorization", "Bearer " + adminToken()))
                .hasStatus(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(mvc.get().uri(location))
                .hasStatusOk()
                .bodyJson().extractingPath("$.value").isEqualTo(5.0);
    }

    @Test
    void fromAndToAreInclusive() throws Exception {
        Long seriesId = createSeries(0, 10);
        String apiKey = createSensorKey(seriesId);
        sendMeasurement(apiKey, 1, "2026-01-10T10:00:00Z");
        sendMeasurement(apiKey, 2, "2026-01-10T11:00:00Z");
        sendMeasurement(apiKey, 3, "2026-01-10T12:00:00Z");
        sendMeasurement(apiKey, 4, "2026-01-10T13:00:00Z");
        assertThat(mvc.get().uri("/api/measurements?series=" + seriesId 
        + "&from=2026-01-10T11:00:00Z&to=2026-01-10T12:00:00Z"))
                .hasStatusOk()
                .bodyJson().extractingPath("$[*].value").asArray().containsExactly(2.0, 3.0); 
    }

}
