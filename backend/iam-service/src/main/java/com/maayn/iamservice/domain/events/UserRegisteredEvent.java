package com.maayn.iamservice.domain.events;


import com.maayn.iamservice.domain.entity.User;

public class UserRegisteredEvent {

    private final User user;

    public UserRegisteredEvent(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}