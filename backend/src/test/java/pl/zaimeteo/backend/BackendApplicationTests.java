package pl.zaimeteo.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = {"ADMIN_USERNAME=admin", "ADMIN_PASSWORD=test-password"})
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
