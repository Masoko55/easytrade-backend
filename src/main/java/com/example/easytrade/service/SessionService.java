package com.example.easytrade.service;

import com.example.easytrade.model.Session;
import com.example.easytrade.model.User;
import com.example.easytrade.repository.SessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List; // Was used in commented out code, keep or remove if not needed

@Service
public class SessionService {

    private final SessionRepository sessionRepository;

    @Autowired
    public SessionService(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }

    @Transactional
    public Session recordLoginSession(User user, HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");

        // Optional: Invalidate previous active sessions for this user
        // List<Session> activeSessions = sessionRepository.findByUserAndIsActiveTrue(user);
        // activeSessions.forEach(session -> {
        //     session.setActive(false);
        //     session.setExpiryTime(LocalDateTime.now()); // Also set expiry if invalidating
        // });
        // if (!activeSessions.isEmpty()) { // Only save if there were sessions to update
        //    sessionRepository.saveAll(activeSessions);
        // }


        Session newSession = new Session(user, ipAddress, userAgent);
        // Example: Set expiry time if you want sessions to expire
        // newSession.setExpiryTime(LocalDateTime.now().plusHours(24)); 
        
        System.out.println("Recording new session for user: " + user.getUsername() + " with token: " + newSession.getSessionToken());
        return sessionRepository.save(newSession);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String remoteAddr = "";
        if (request != null) {
            remoteAddr = request.getHeader("X-FORWARDED-FOR");
            if (remoteAddr == null || "".equals(remoteAddr)) {
                remoteAddr = request.getRemoteAddr();
            }
        }
        return remoteAddr;
    }

    @Transactional
    public void invalidateSession(String sessionToken) {
        sessionRepository.findBySessionToken(sessionToken).ifPresent(session -> {
            session.setActive(false);
            session.setExpiryTime(LocalDateTime.now()); // Mark as expired now
            sessionRepository.save(session);
            System.out.println("Invalidated session token: " + sessionToken);
        });
    }
} // <<<<<<<< THIS IS LIKELY AROUND LINE 64 or before. Check for extra braces or missing ones above this line.