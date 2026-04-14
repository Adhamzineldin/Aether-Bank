package com.maayn.iamservice.repository;

import com.maayn.iamservice.domain.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    // Find role by name (ADMIN, CUSTOMER, EMPLOYEE)
    Optional<Role> findByName(String name);

    // Check if role already exists (optional but useful)
    boolean existsByName(String name);
}