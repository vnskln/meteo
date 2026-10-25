package pl.zaimeteo.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import com.jayway.jsonpath.JsonPath;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = {"ADMIN_USERNAME=admin", "ADMIN_PASSWORD=test-password"})
@AutoConfigureMockMvc
public abstract class ApiTestSupport {

    @Autowired
    protected MockMvcTester mvc;

    protected String adminToken() throws Exception {
        MvcTestResult login = mvc.post().uri("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"test-password\"}")
                .exchange();
        return JsonPath.read(body(login), "$.accessToken");
    }

    protected Long createSeries(double minValue, double maxValue) throws Exception {
        MvcTestResult result = mvc.post().uri("/api/series").contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + adminToken())
                .content(seriesJson(minValue, maxValue))
                .exchange();
        return id(result);
    }

    protected MvcTestResult createSensor(Long seriesId) throws Exception {
        return mvc.post().uri("/api/sensors").contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + adminToken())
                .content("{\"name\":\"Test sensor\",\"seriesId\":" + seriesId + "}")
                .exchange();
    }

    protected String createSensorKey(Long seriesId) throws Exception {
        return JsonPath.read(body(createSensor(seriesId)), "$.apiKey");
    }

    protected MvcTestResult sendMeasurement(String apiKey, double value, String timestamp) {
        String json = timestamp == null
                ? "{\"value\":" + value + "}"
                : "{\"value\":" + value + ",\"timestamp\":\"" + timestamp + "\"}";
        return mvc.post().uri("/api/measurements").contentType(MediaType.APPLICATION_JSON)
                .header("X-API-Key", apiKey)
                .content(json)
                .exchange();
    }

    protected String seriesJson(double minValue, double maxValue) {
        return "{\"name\":\"Test series\",\"minValue\":" + minValue + ",\"maxValue\":" + maxValue
                + ",\"color\":\"#1f77b4\"}";
    }

    protected Long id(MvcTestResult result) throws Exception {
        Number id = JsonPath.read(body(result), "$.id");
        return id.longValue();
    }

    protected String body(MvcTestResult result) throws Exception {
        return result.getResponse().getContentAsString();
    }
}
