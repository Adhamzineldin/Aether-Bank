package com.maayn.accountservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.maayn.accountservice.dto.CustomerRequest;
import com.maayn.accountservice.dto.CustomerResponse;
import com.maayn.accountservice.service.AccountProfileService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/customers")
public class AccountController {

    @Autowired
    private AccountProfileService accountProfileService;

    @PostMapping("/register")
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerRequest request) {
        return new ResponseEntity<>(accountProfileService.registerCustomer(request), HttpStatus.CREATED);
    }

    @GetMapping("/list")
    public ResponseEntity<List<CustomerResponse>> listAll() {
        return ResponseEntity.ok(accountProfileService.getAllCustomers());
    }
}