package pl.zaimeteo.backend.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

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

    record PasswordChangeRequest(@NotBlank String currentPassword, 
            @NotBlank @Size(min = 8) String newPassword) {
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest loginRequest) {
        
        String token = authService.login(loginRequest.username(), loginRequest.password());
        return new LoginResponse(token, "Bearer", AuthService.SESSION_SECONDS);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@AuthenticationPrincipal Session session) {

        authService.logout(session);
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@AuthenticationPrincipal Session session, 
        @RequestBody @Valid PasswordChangeRequest passwordChangeRequest) {

        authService.changePassword(session, 
            passwordChangeRequest.currentPassword(),
            passwordChangeRequest.newPassword());
    }
    
}
