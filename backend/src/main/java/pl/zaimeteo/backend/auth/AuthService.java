package pl.zaimeteo.backend.auth;

import java.time.Instant;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AdminUserRepository adminUserRepository;
    private final SessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
 
    public AuthService(AdminUserRepository adminUserRepository, 
        SessionRepository sessionRepository, PasswordEncoder passwordEncoder) {

        this.adminUserRepository = adminUserRepository;
        this.sessionRepository = sessionRepository;
        this.passwordEncoder = passwordEncoder;
    }
      
    public  void createAdminIfMissing(String username, String password) {
        Optional<AdminUser> adminUser = adminUserRepository.findByUsername(username);
        if (adminUser.isPresent()) return;
        AdminUser newAdminUser = new AdminUser();
        newAdminUser.setPasswordHash(passwordEncoder.encode(password));
        newAdminUser.setUsername(username);
        newAdminUser.setCreatedAt(Instant.now());
        adminUserRepository.save(newAdminUser);
    }

}