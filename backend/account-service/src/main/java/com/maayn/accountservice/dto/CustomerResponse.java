package com.maayn.accountservice.dto;

public class CustomerResponse {
    private Long id;
    private String fullName;
    private String email;
    private String status;

    public CustomerResponse(Long id, String fullName, String email, String status) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.status = status;
    }

    // Getters
    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getStatus() { return status; }
}