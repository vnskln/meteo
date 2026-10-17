package pl.zaimeteo.backend.auth;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController (AuthService authService) {
        this.authService = authService;
    }

    record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }

    record LoginResponse(String accessToken, String tokenType, int expiresIn) {
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest loginRequest) {
        
        String token = authService.login(loginRequest.username(), loginRequest.password());
        return new LoginResponse(token, "Bearer", AuthService.SESSION_SECONDS);
    }
    
}
