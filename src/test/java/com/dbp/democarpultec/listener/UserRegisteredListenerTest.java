package com.dbp.democarpultec.listener;

import com.dbp.democarpultec.event.UserRegisteredEvent;
import com.dbp.democarpultec.service.RegistrationEmailService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class UserRegisteredListenerTest {

    @Test
    void shouldDelegateEmailSendingWhenUserRegisters() {
        RegistrationEmailService registrationEmailService = mock(RegistrationEmailService.class);
        UserRegisteredListener listener = new UserRegisteredListener(registrationEmailService);

        listener.onUserRegistered(new UserRegisteredEvent(listener, 1L, "juan@utec.edu.pe", "Juan"));

        verify(registrationEmailService).sendWelcomeEmail("juan@utec.edu.pe", "Juan");
    }
}
