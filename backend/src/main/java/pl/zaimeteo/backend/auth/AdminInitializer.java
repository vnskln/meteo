package pl.zaimeteo.backend.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final AuthService authService;
    private final String username;
    private final String password;

    public AdminInitializer(AuthService authService,
            @Value("${ADMIN_USERNAME}") String username,
            @Value("${ADMIN_PASSWORD}") String password) {
        this.authService = authService;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        authService.createAdminIfMissing(username, password);
    }
}
