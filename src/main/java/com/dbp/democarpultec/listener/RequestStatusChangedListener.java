package com.dbp.democarpultec.listener;

import com.dbp.democarpultec.event.RequestStatusChangedEvent;
import com.dbp.democarpultec.service.RequestNotificationEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RequestStatusChangedListener {

    private final RequestNotificationEmailService requestNotificationEmailService;

    @Async("eventTaskExecutor")
    @EventListener
    public void onRequestStatusChanged(RequestStatusChangedEvent event) {
        requestNotificationEmailService.sendStatusEmail(
                event.getEmail(),
                event.getName(),
                event.getPublicationTitle(),
                event.getStatus()
        );
    }
}
