package com.dbp.democarpultec.listener;

import com.dbp.democarpultec.event.UserRegisteredEvent;
import com.dbp.democarpultec.service.RegistrationEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserRegisteredListener {

    private final RegistrationEmailService registrationEmailService;

    @Async("eventTaskExecutor")
    @EventListener
    public void onUserRegistered(UserRegisteredEvent event) {
        registrationEmailService.sendWelcomeEmail(event.getEmail(), event.getName());
    }
}
