package pl.zaimeteo.backend.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import pl.zaimeteo.backend.error.UnauthorizedException;

@Service
public class AuthService {

    private final AdminUserRepository adminUserRepository;
    private final SessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;

    public static final int SESSION_SECONDS = 8 * 60 * 60;
 
    public AuthService(AdminUserRepository adminUserRepository, 
        SessionRepository sessionRepository, PasswordEncoder passwordEncoder) {

        this.adminUserRepository = adminUserRepository;
        this.sessionRepository = sessionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String login(String username, String password) {

        Optional<AdminUser> adminUser = adminUserRepository.findByUsername(username);
        if (adminUser.isEmpty() || 
        !passwordEncoder.matches(password,adminUser.get().getPasswordHash())) {
            throw new UnauthorizedException("Invalid username or password");
        }
        else {
            String token = newToken();
            Session session = new Session();
            session.setCreatedAt(Instant.now());
            session.setExpiresAt(session.getCreatedAt().plusSeconds(SESSION_SECONDS));
            session.setUserId(adminUser.get().getId());
            session.setTokenHash(hash(token));
            sessionRepository.save(session);
            return token;
        }
    }
      
    public void createAdminIfMissing(String username, String password) {
        Optional<AdminUser> adminUser = adminUserRepository.findByUsername(username);
        if (adminUser.isPresent()) return;
        AdminUser newAdminUser = new AdminUser();
        newAdminUser.setPasswordHash(passwordEncoder.encode(password));
        newAdminUser.setUsername(username);
        newAdminUser.setCreatedAt(Instant.now());
        adminUserRepository.save(newAdminUser);
    }

    private String newToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private byte[] hash(String token) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public Optional<Session> findValidSession(String token) {
        Optional<Session> session = sessionRepository.findByTokenHash(hash(token));
        if (session.isPresent() && session.get().getExpiresAt().isAfter(Instant.now())) {
            return session;
        }
        else {
            return Optional.empty();
        }
    }

}