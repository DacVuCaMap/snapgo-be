package com.delivery.app.service.implement;

import com.delivery.app.Entity.Account;
import com.delivery.app.Entity.Role;
import com.delivery.app.dto.AccountDto;
import com.delivery.app.dto.Request.AccountRequest;
import com.delivery.app.dto.Response.AccountListResponse;
import com.delivery.app.dto.Response.DefaultResponse;
import com.delivery.app.repository.AccountRepository;
import com.delivery.app.repository.RoleRepository;
import com.delivery.app.service.AccountService;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AccountImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final ModelMapper modelMapper;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ResponseEntity<?> getAccounts(String prefix, String email, int page, int size) {
        // Tạo Pageable để phân trang
        Pageable pageable = PageRequest.of(page, size);

        // Lọc danh sách account
        Page<Account> accountPage;
        if (prefix != null && !prefix.trim().isEmpty() && email != null && !email.trim().isEmpty()) {
            accountPage = accountRepository.findByFirstNameStartingWithAndEmailContainingAndIsDeletedFalse(
                    prefix, email, pageable);
        } else if (prefix != null && !prefix.trim().isEmpty()) {
            accountPage = accountRepository.findByFirstNameStartingWithAndIsDeletedFalse(prefix, pageable);
        } else if (email != null && !email.trim().isEmpty()) {
            accountPage = accountRepository.findByEmailContainingAndIsDeletedFalse(email, pageable);
        } else {
            accountPage = accountRepository.findByIsDeletedFalse(pageable);
        }

        // Ánh xạ sang AccountDto
        List<AccountDto> accountDtos = accountPage.getContent().stream()
                .map(this::mapToAccountResponse)
                .collect(Collectors.toList());

        // Tạo response với thông tin phân trang
        DefaultResponse defaultResponse = new DefaultResponse(
                200,
                "Lấy danh sách tài khoản thành công",
                new AccountListResponse(
                        accountDtos,
                        accountPage.getTotalElements(),
                        accountPage.getTotalPages(),
                        accountPage.getNumber(),
                        accountPage.getSize()
                ),
                true
        );
        return ResponseEntity.ok().body(defaultResponse);
    }

    @Override
    public DefaultResponse createAccount(AccountRequest request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            return new DefaultResponse(409,"Email đã tồn tại",false);
        }

        Account account = modelMapper.map(request,Account.class);
        account.setPassword(passwordEncoder.encode(request.getPassword()));

        String requestedRole = request.getRoleName();
        Role defaultRole;
        defaultRole = roleRepository.findByName(requestedRole)
                .orElseGet(() -> roleRepository.findByName("USER")
                        .orElseThrow(() -> new IllegalStateException("Default role USER not found")));

        account.setRole(defaultRole);

        accountRepository.save(account);

        return new DefaultResponse(201,"Thêm tài khoản thành công",mapToAccountResponse(account),true);
    }

    @Override
    public ResponseEntity<?> updateAccount(Long id, AccountRequest request) {
        Account account = accountRepository.findById(id)
                .filter(a -> !a.isDeleted()) // Chỉ lấy account chưa bị xóa
                .orElseThrow(() -> new IllegalArgumentException("Account not found with ID: " + id));

        // Kiểm tra email trùng nếu thay đổi
        if (!account.getEmail().equals(request.getEmail()) && accountRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Ánh xạ các trường từ request sang account
        modelMapper.map(request, account);

        // Cập nhật mật khẩu nếu có
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            account.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // Cập nhật role nếu có
        String requestedRole = request.getRoleName();
        if (requestedRole != null && !requestedRole.trim().isEmpty()) {
            Role role = roleRepository.findByName(requestedRole)
                    .orElseThrow(() -> new IllegalArgumentException("Role not found: " + requestedRole));
            account.setRole(role);
        }

        accountRepository.save(account);

        return ResponseEntity.ok().body(new DefaultResponse(200, "Cập nhật tài khoản thành công", mapToAccountResponse(account), true));
    }

    @Override
    public ResponseEntity<?> deleteAccount(Long id) {
        Account account = accountRepository.findById(id)
                .filter(a -> !a.isDeleted()) // Chỉ lấy account chưa bị xóa
                .orElseThrow(() -> new IllegalArgumentException("Account not found with ID: " + id));

        // Soft delete
        account.setDeleted(true);
        account.setDeletedAt(LocalDateTime.now());
        accountRepository.save(account);

        return ResponseEntity.ok().body(new DefaultResponse(200, "Xóa tài khoản thành công", null, true));
    }

    private AccountDto mapToAccountResponse(Account account) {
        AccountDto accountDto = modelMapper.map(account,AccountDto.class);
        accountDto.setPassword(null);
        accountDto.setRoleName(account.getRole() != null ? account.getRole().getName() : null);
        return accountDto;
    }
}
