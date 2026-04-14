package com.maayn.iamservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String userName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id")
    private Role role;

    // 🔒 Business-safe update methods (encapsulation)

    public void updateProfile(String newUserName, String newEmail) {
        this.userName = newUserName;
        this.email = newEmail;
    }

    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    public void assignRole(Role role) {
        this.role = role;
    }

    // 🔍 Domain logic (NOT business logic)

    public boolean hasRole(String roleName) {
        return this.role != null &&
                this.role.getName().equalsIgnoreCase(roleName);
    }
}