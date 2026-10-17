package pl.zaimeteo.backend.auth;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
    
@Entity
@Table(name = "sessions")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private byte[] tokenHash;
    private Instant createdAt;
    private Instant expiresAt;

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public byte[] getTokenHash() { return tokenHash; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
 
    public void setUserId(Long userId) { this.userId = userId; }
    public void setTokenHash(byte[] tokenHash) { this.tokenHash = tokenHash; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
}