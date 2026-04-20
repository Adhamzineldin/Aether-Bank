package com.maayn.accountservice.service;

import java.util.List;

import com.maayn.accountservice.dto.CustomerRequest;
import com.maayn.accountservice.dto.CustomerResponse;

public interface AccountProfileService {
    CustomerResponse registerCustomer(CustomerRequest request);
    List<CustomerResponse> getAllCustomers();
}