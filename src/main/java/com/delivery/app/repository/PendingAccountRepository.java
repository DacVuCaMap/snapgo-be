package com.delivery.app.repository;

import com.delivery.app.Entity.PendingAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PendingAccountRepository extends JpaRepository<PendingAccount,Long> {
    Optional<PendingAccount>  findByEmail(String email);
    Optional<PendingAccount> findByActivationToken(String activationToken);
    List<PendingAccount> findByTokenExpiryBefore(LocalDateTime expiry);
}
