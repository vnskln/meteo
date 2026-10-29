package pl.zaimeteo.backend.sensor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import com.jayway.jsonpath.JsonPath;

import pl.zaimeteo.backend.ApiTestSupport;

class SensorControllerTest extends ApiTestSupport {

    @Test
    void apiKeyIsShownOnlyOnCreate() throws Exception {
        MvcTestResult created = createSensor(createSeries(0, 10));
        assertThat(created).hasStatus(HttpStatus.CREATED).containsHeader("Location");
        String apiKey = JsonPath.read(body(created), "$.apiKey");
        assertThat(apiKey).hasSizeGreaterThanOrEqualTo(32);

        String token = adminToken();
        String location = created.getResponse().getHeader("Location");
        String one = body(mvc.get().uri(location).header("Authorization", "Bearer " + token).exchange());
        String list = body(mvc.get().uri("/api/sensors").header("Authorization", "Bearer " + token).exchange());

        assertThat(one).doesNotContain(apiKey).doesNotContain("apiKey").doesNotContain("Hash");
        assertThat(list).doesNotContain(apiKey).doesNotContain("apiKey").doesNotContain("Hash");
    }

    @Test
    void createForMissingSeriesReturns422() throws Exception {
        assertThat(createSensor(999999999L)).hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @Test
    void listWithoutTokenReturns401() {
        assertThat(mvc.get().uri("/api/sensors")).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test 
    void revokedSensorKeyReturns401AndMeasurementsStay() throws Exception {
        MvcTestResult created = createSensor(createSeries(0, 10));
        assertThat(created).hasStatus(HttpStatus.CREATED);
        Long id = id(created);
        String apiKey = JsonPath.read(body(created), "$.apiKey");
        String adminToken = adminToken();
        String location = sendMeasurement(apiKey, 1, "2026-01-10T10:00:00Z")
            .getResponse().getHeader("Location");
        assertThat(mvc.delete().uri("/api/sensors/" + id)
            .header("Authorization", "Bearer " + adminToken)).hasStatus(HttpStatus.NO_CONTENT);
        assertThat(sendMeasurement(apiKey, 1, "2026-01-10T10:00:00Z"))
            .hasStatus(HttpStatus.UNAUTHORIZED);
        assertThat(mvc.get().uri(location))
            .hasStatusOk().bodyJson().extractingPath("$.value").isEqualTo(1.0);
    }
}
