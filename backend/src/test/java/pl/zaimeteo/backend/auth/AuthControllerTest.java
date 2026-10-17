package pl.zaimeteo.backend.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import com.jayway.jsonpath.JsonPath;

import pl.zaimeteo.backend.TestcontainersConfiguration;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = {"ADMIN_USERNAME=admin", "ADMIN_PASSWORD=test-password"})
@AutoConfigureMockMvc
class AuthControllerTest {

    private static final String SERIES_JSON =
            "{\"name\":\"Test\",\"minValue\":0,\"maxValue\":1,\"color\":\"#000000\"}";

    @Autowired
    MockMvcTester mvc;

    @Test
    void loginWithWrongPasswordReturns401() {
        assertThat(mvc.post().uri("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"wrong-password\"}"))
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void createSeriesWithoutTokenReturns401() {
        assertThat(mvc.post().uri("/api/series").contentType(MediaType.APPLICATION_JSON)
                .content(SERIES_JSON))
                .hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void createSeriesWithTokenReturns201() throws Exception {
        MvcTestResult login = mvc.post().uri("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"test-password\"}")
                .exchange();
        assertThat(login).hasStatus(HttpStatus.OK);
        String token = JsonPath.read(login.getResponse().getContentAsString(), "$.accessToken");

        assertThat(mvc.post().uri("/api/series").contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(SERIES_JSON))
                .hasStatus(HttpStatus.CREATED)
                .containsHeader("Location");
    }
}
