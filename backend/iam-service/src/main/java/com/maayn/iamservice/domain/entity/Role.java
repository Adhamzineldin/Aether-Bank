package com.maayn.iamservice.domain.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    // Domain rule (clean, minimal logic)
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(this.name);
    }

    public boolean isCustomer() {
        return "CUSTOMER".equalsIgnoreCase(this.name);
    }

    public boolean isEmployee() {
        return "EMPLOYEE".equalsIgnoreCase(this.name);
    }
}