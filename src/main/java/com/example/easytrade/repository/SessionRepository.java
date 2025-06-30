package com.example.easytrade.repository;

import com.example.easytrade.model.Session;
import com.example.easytrade.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
Optional<Session> findBySessionToken(String sessionToken);
List<Session> findByUserAndIsActiveTrue(User user); // Example: find active sessions for a user
}