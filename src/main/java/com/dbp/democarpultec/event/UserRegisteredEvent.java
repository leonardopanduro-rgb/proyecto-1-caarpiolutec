package com.dbp.democarpultec.event;

import org.springframework.context.ApplicationEvent;

public class UserRegisteredEvent extends ApplicationEvent {
    private final Long userId;
    private final String email;
    private final String name;

    public UserRegisteredEvent(Object source, Long userId, String email, String name) {
        super(source);
        this.userId = userId;
        this.email = email;
        this.name = name;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }
}
