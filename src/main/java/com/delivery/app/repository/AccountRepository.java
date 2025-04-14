package com.delivery.app.repository;

import com.delivery.app.Entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account,Long> {
    Optional<Account> findByEmail(String email);
    boolean existsByEmail(String email);

    // Lọc theo tiền tố firstName
    Page<Account> findByFirstNameStartingWithAndIsDeletedFalse(String prefix, Pageable pageable);

    // Lọc theo email
    Page<Account> findByEmailContainingAndIsDeletedFalse(String email, Pageable pageable);

    // Lọc theo cả tiền tố firstName và email
    Page<Account> findByFirstNameStartingWithAndEmailContainingAndIsDeletedFalse(String prefix, String email, Pageable pageable);

    // Lấy tất cả account chưa xóa
    Page<Account> findByIsDeletedFalse(Pageable pageable);
}
