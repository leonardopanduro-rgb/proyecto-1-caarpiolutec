package com.dbp.democarpultec.service;

import com.dbp.democarpultec.model.enums.Status;
import com.dbp.democarpultec.service.impl.RequestNotificationEmailServiceImpl;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RequestNotificationEmailServiceTest {

    @Test
    void shouldSendEmailWhenRequestIsAccepted() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MimeMessage message = new MimeMessage((Session) null);
        when(mailSender.createMimeMessage()).thenReturn(message);
        RequestNotificationEmailServiceImpl service = new RequestNotificationEmailServiceImpl();
        ReflectionTestUtils.setField(service, "mailSender", mailSender);
        ReflectionTestUtils.setField(service, "fromAddress", "no-reply@carpoolutec.local");

        service.sendStatusEmail("juan@utec.edu.pe", "Juan", "Viaje a UTEC", Status.ACCEPTED);

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void shouldSkipEmailWhenRecipientIsMissing() {
        RequestNotificationEmailServiceImpl service = new RequestNotificationEmailServiceImpl();
        ReflectionTestUtils.setField(service, "fromAddress", "no-reply@carpoolutec.local");

        assertDoesNotThrow(() -> service.sendStatusEmail(null, "Juan", "Viaje a UTEC", Status.REJECTED));
    }
}
