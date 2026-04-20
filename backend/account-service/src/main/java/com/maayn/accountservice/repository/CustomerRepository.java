package com.maayn.accountservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.maayn.accountservice.entity.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    // Adding a finder method to help with banking validation (Check if email exists)
    Optional<Customer> findByEmail(String email);
}