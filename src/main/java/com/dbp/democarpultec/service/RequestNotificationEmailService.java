package com.dbp.democarpultec.service;

import com.dbp.democarpultec.model.enums.Status;

public interface RequestNotificationEmailService {
    void sendStatusEmail(String toEmail, String recipientName, String publicationTitle, Status status);
}
