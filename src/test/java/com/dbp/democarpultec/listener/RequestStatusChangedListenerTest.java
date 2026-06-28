package com.dbp.democarpultec.listener;

import com.dbp.democarpultec.event.RequestStatusChangedEvent;
import com.dbp.democarpultec.model.enums.Status;
import com.dbp.democarpultec.service.RequestNotificationEmailService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RequestStatusChangedListenerTest {

    @Test
    void shouldDelegateEmailSendingWhenRequestStatusChanges() {
        RequestNotificationEmailService emailService = mock(RequestNotificationEmailService.class);
        RequestStatusChangedListener listener = new RequestStatusChangedListener(emailService);

        listener.onRequestStatusChanged(new RequestStatusChangedEvent(
                listener,
                1L,
                "juan@utec.edu.pe",
                "Juan",
                "Viaje a UTEC",
                Status.ACCEPTED
        ));

        verify(emailService).sendStatusEmail(
                "juan@utec.edu.pe",
                "Juan",
                "Viaje a UTEC",
                Status.ACCEPTED
        );
    }
}
