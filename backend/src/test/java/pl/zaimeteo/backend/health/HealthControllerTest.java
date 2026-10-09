package pl.zaimeteo.backend.health;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(HealthController.class)
public class HealthControllerTest {

    @Autowired
    MockMvcTester mvc;

    @Test
    void returnsStatusOkAsJson() {
        assertThat(mvc.get().uri("/api/health").accept(MediaType.APPLICATION_JSON))
                .hasStatus(HttpStatus.OK).hasContentType(MediaType.APPLICATION_JSON).bodyJson()
                .extractingPath("$.status").isEqualTo("ok");
    }

    @Test
    void rejectsXmlWith406() {
        assertThat(mvc.get().uri("/api/health").accept(MediaType.APPLICATION_XML))
                .hasStatus(HttpStatus.NOT_ACCEPTABLE);
    }
}
