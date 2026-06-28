package com.dbp.democarpultec.service;

public interface RegistrationEmailService {
    void sendWelcomeEmail(String toEmail, String recipientName);
}
