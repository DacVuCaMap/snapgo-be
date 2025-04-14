package com.delivery.app.repository;

import com.delivery.app.Entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserSessionRepository extends JpaRepository<UserSession,Long> {
    Optional<UserSession> findByToken(String token);
}
