package com.example.easytrade.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "sessions")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // <<< ADD THIS MAPPING
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "session_token", nullable = false, unique = true)
    private String sessionToken;

    @Column(name = "login_time", nullable = false)
    private LocalDateTime loginTime;

    @Column(name = "expiry_time")
    private LocalDateTime expiryTime;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "is_active")
    private boolean isActive = true;

    public Session() {
        this.loginTime = LocalDateTime.now();
        this.sessionToken = UUID.randomUUID().toString();
        this.isActive = true;
    }

    public Session(User user, String ipAddress, String userAgent) {
        this();
        this.user = user;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    // Getters and Setters... (ensure they are all present)
     public Long getId() { return id; }
     public void setId(Long id) { this.id = id; }
     public User getUser() { return user; }
     public void setUser(User user) { this.user = user; }
     public String getSessionToken() { return sessionToken; }
     public void setSessionToken(String sessionToken) { this.sessionToken = sessionToken; }
     public LocalDateTime getLoginTime() { return loginTime; }
     public void setLoginTime(LocalDateTime loginTime) { this.loginTime = loginTime; }
     public LocalDateTime getExpiryTime() { return expiryTime; }
     public void setExpiryTime(LocalDateTime expiryTime) { this.expiryTime = expiryTime; }
     public String getIpAddress() { return ipAddress; }
     public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
     public String getUserAgent() { return userAgent; }
     public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
     public boolean isActive() { return isActive; }
     public void setActive(boolean active) { isActive = active; }
}