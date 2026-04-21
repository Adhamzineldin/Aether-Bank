package com.maayn.accountservice.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.maayn.accountservice.dto.CustomerRequest;
import com.maayn.accountservice.dto.CustomerResponse;
import com.maayn.accountservice.entity.Customer;
import com.maayn.accountservice.repository.CustomerRepository;

@Service
public class AccountProfileServiceImpl implements AccountProfileService {

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    @Transactional
    public CustomerResponse registerCustomer(CustomerRequest request) {
        // SECURITY CHECK: Verify email uniqueness before persistence
        if (customerRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email address is already registered in the banking system.");
        }

        Customer customer = new Customer();
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setAddress(request.getAddress());

        Customer saved = customerRepository.save(customer);
        return mapToResponse(saved);
    }

    @Override
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private CustomerResponse mapToResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFirstName() + " " + customer.getLastName(),
                customer.getEmail(),
                customer.getStatus()
        );
    }
}