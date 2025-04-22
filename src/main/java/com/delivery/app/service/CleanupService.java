package com.delivery.app.service;

import com.delivery.app.Entity.PendingAccount;
import com.delivery.app.repository.PendingAccountRepository;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class CleanupService {

    private final PendingAccountRepository pendingAccountRepository;

    @Scheduled(cron = "0 0 0 * * ?") // Chạy hàng ngày lúc 00:00
    @Transactional
    public void cleanupExpiredPendingAccounts() {
        LocalDateTime now = LocalDateTime.now();
        List<PendingAccount> expiredAccounts = pendingAccountRepository.findByTokenExpiryBefore(now);
        pendingAccountRepository.deleteAll(expiredAccounts);
    }
}
