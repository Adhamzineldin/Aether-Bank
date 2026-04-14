package com.maayn.iamservice.service.impl;


import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl {

    public String updateUser() {
        return "user updated";
    }

    public String assignRole() {
        return "role assigned";
    }
}
