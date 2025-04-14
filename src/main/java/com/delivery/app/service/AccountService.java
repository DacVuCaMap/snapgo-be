package com.delivery.app.service;

import com.delivery.app.dto.Request.AccountRequest;
import com.delivery.app.dto.Response.DefaultResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface AccountService {
    ResponseEntity<?> getAccounts(String prefix, String email, int page, int size);
    DefaultResponse createAccount(AccountRequest request);
    ResponseEntity<?> updateAccount (Long id,AccountRequest request);
    ResponseEntity<?> deleteAccount (Long id);
}
