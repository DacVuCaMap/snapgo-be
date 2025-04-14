package com.delivery.app.controller;

import com.delivery.app.dto.Request.AccountRequest;
import com.delivery.app.dto.Response.DefaultResponse;
import com.delivery.app.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@AllArgsConstructor
@Tag(name = "Account Management", description = "APIs for managing accounts")
public class AccountController {
    private final AccountService accountService;

    @GetMapping("/list")
    @Operation(summary = "Get list of accounts", description = "Retrieves a paginated list of accounts with optional filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Accounts retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role")
    })
    public ResponseEntity<?> getAccounts(
            @RequestParam(required = false) String prefix,
            @RequestParam(required = false) String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return accountService.getAccounts(prefix, email, page, size);
    }
    @PostMapping("/create")
    @Operation(summary = "Create a new account", description = "Creates a new account with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Account created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or email already exists"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role")
    })
    public ResponseEntity<?> createAccount (@RequestBody  AccountRequest accountRequest){
        System.out.println(accountRequest);
        DefaultResponse defaultResponse = accountService.createAccount(accountRequest);
        return ResponseEntity.ok().body(defaultResponse);
    }
    @PutMapping("/{id}")
    @Operation(summary = "Update an account", description = "Updates an existing account by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or email already exists"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role")
    })
    public ResponseEntity<?> updateAccount(
            @PathVariable Long id,
            @RequestBody AccountRequest accountRequest) {
        return ResponseEntity.ok().body(accountService.updateAccount(id, accountRequest));
    }
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an account", description = "Soft deletes an account by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden - requires ADMIN role")
    })
    public ResponseEntity<?> deleteAccount(@PathVariable Long id) {
        return ResponseEntity.ok().body(accountService.deleteAccount(id));
    }

}
