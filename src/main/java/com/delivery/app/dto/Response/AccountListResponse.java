package com.delivery.app.dto.Response;

import com.delivery.app.dto.AccountDto;
import lombok.Data;

import java.util.List;

@Data
public class AccountListResponse {
    private List<AccountDto> accounts;
    private long totalElements;  // Tổng số phần tử
    private int totalPages;      // Tổng số trang
    private int currentPage;     // Trang hiện tại
    private int pageSize;        // Kích thước trang

    public AccountListResponse(List<AccountDto> accounts, long totalElements, int totalPages, int currentPage, int pageSize) {
        this.accounts = accounts;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
    }
}