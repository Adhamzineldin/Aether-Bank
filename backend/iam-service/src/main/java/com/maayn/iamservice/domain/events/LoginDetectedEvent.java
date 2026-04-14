package com.maayn.iamservice.domain.events;


import com.maayn.iamservice.domain.entity.User;

public class LoginDetectedEvent {

    private final User user;

    public LoginDetectedEvent(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}