package com.delivery.app.service;

import com.delivery.app.Entity.*;
import com.delivery.app.dto.LoginRequest;
import com.delivery.app.dto.LoginResponse;
import com.delivery.app.dto.Request.RegisterRequest;
import com.delivery.app.dto.Response.DefaultResponse;
import com.delivery.app.repository.*;
import com.delivery.app.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@AllArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final AccountRepository accountRepository;
    private final ModelMapper modelMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserSessionRepository userSessionRepository;
    private final EmailService emailService;
    private final PendingAccountRepository pendingAccountRepository;
    private final StoreRepository storeRepository;

    public DefaultResponse login(LoginRequest loginRequest){
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getEmail());
            final String jwt = jwtUtil.generateToken(userDetails);

            // save token inside db
            Account account = accountRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("Account not found"));
            UserSession session = new UserSession();
            session.setAccount(account);
            session.setToken(jwt);
            session.setCreatedAt(LocalDateTime.now());
            // set expiration time
            LocalDateTime expirationTime = LocalDateTime.ofInstant(jwtUtil.extractExpiration(jwt).toInstant(),java.time.ZoneId.systemDefault());
            session.setExpiresAt(expirationTime);
            userSessionRepository.save(session);
            LoginResponse loginResponse = new LoginResponse();

            //set value
            loginResponse.setActive(true);
            loginResponse.setRoleName(account.getRole().getName());
            loginResponse.setExpirationTime(expirationTime);
            loginResponse.setToken(jwt);
            loginResponse.setName(account.getLastName()+" "+account.getFirstName());
            loginResponse.setAvatar(account.getAvatar());
            loginResponse.setEmail(account.getEmail());
            return new DefaultResponse(200,"Login success",loginResponse,true);
        } catch (BadCredentialsException | UsernameNotFoundException e) {
            return new DefaultResponse(400,"Tài khoản hoặc mật khẩu không tồn tại");
        }
    }
    public DefaultResponse registerAccount (RegisterRequest registerRequest){
        if (accountRepository.existsByEmail(registerRequest.getEmail())) {
            return new DefaultResponse(409,"Account already exists.",false);
        }
//        Account newAcc = modelMapper.map(registerRequest, Account.class);
        PendingAccount newPendingAcc = modelMapper.map(registerRequest,PendingAccount.class);
        PendingAccount existPending = pendingAccountRepository.findByEmail(newPendingAcc.getEmail()).orElse(null);
        if (existPending!=null){
            newPendingAcc.setId(existPending.getId());
        }
        // encoder password
        Role defaultRole = roleRepository.findByName(registerRequest.getRoleName())
                .orElse(null);
        if (defaultRole == null) {
            return new DefaultResponse(404, "Không tìm thấy: " + registerRequest.getRoleName(), false);
        }

        newPendingAcc.setRole(defaultRole);
        newPendingAcc.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        /// active code

//        int code = 100000 + new Random().nextInt(900000);
        int code = 000000;
        String activationCode = String.valueOf(code);
        newPendingAcc.setActivationToken(activationCode);
        newPendingAcc.setTokenExpiry(LocalDateTime.now().plusHours(24));

        pendingAccountRepository.save(newPendingAcc);

//        // Gửi email
//        try {
//            emailService.sendActivationEmail(newPendingAcc.getEmail(), activationCode);
//        } catch (Exception e) {
//            System.err.println("Không thể gửi email kích hoạt cho {}: {}" + newPendingAcc.getEmail() + e.getMessage());
//        }
        return new DefaultResponse(200,"Register success: "+newPendingAcc.getEmail(),true);
    }
    @Transactional
    public DefaultResponse activateAccount(String email,String activationToken) {
        PendingAccount pendingAccount = pendingAccountRepository.findByEmail(email)
                .orElse(null);
        if (pendingAccount == null || !pendingAccount.getActivationToken().equals(activationToken)) {
            return new DefaultResponse(404, "Mã kích hoạt không hợp lệ.", false);
        }

        if (pendingAccount.getTokenExpiry().isBefore(LocalDateTime.now())) {
            return new DefaultResponse(400, "Mã kích hoạt đã hết hạn.", false);
        }

        // Ánh xạ sang Account
        Account account = modelMapper.map(pendingAccount, Account.class);
        Account newAcc = accountRepository.save(account);
        /// tao store
        if (pendingAccount.getRole().getName().equals("PARTNER")){
            Store newStore = modelMapper.map(pendingAccount,Store.class);
            newStore.setAccount(newAcc);
            storeRepository.save(newStore);

        }
        // Xóa bản ghi pending
        pendingAccountRepository.delete(pendingAccount);

        return new DefaultResponse(200, "Kích hoạt tài khoản thành công: " + account.getEmail(), true);
    }
    public DefaultResponse logoutAccount (String jwt){
        UserSession session = userSessionRepository.findByToken(jwt).orElse(null);
        if (session == null){
            return new DefaultResponse(404,"Không tìm thấy phiên",false );
        }
        session.setActive(false);
        userSessionRepository.save(session);
        return new DefaultResponse(200,"Đăng xuất thành công",true);
    }
}
