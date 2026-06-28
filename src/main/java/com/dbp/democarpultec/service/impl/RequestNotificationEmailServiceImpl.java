package com.dbp.democarpultec.service.impl;

import com.dbp.democarpultec.model.enums.Status;
import com.dbp.democarpultec.service.RequestNotificationEmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

@Service
@Slf4j
public class RequestNotificationEmailServiceImpl implements RequestNotificationEmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@carpoolutec.local}")
    private String fromAddress;

    public void sendStatusEmail(String toEmail, String recipientName, String publicationTitle, Status status) {
        if (mailSender == null || toEmail == null || toEmail.isBlank()) {
            log.info("Skipping request status email because recipient or JavaMailSender is not configured");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("Carpool UTEC - solicitud " + status.name().toLowerCase());
            helper.setText(buildHtmlBody(recipientName, publicationTitle, status), true);
            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("Could not send request status email to {}: {}", toEmail, ex.getMessage());
        }
    }

    private String buildHtmlBody(String recipientName, String publicationTitle, Status status) {
        String name = recipientName == null || recipientName.isBlank() ? "estudiante" : recipientName;
        String title = publicationTitle == null || publicationTitle.isBlank() ? "tu viaje" : publicationTitle;
        return "<h2>Hola " + HtmlUtils.htmlEscape(name) + "</h2>"
                + "<p>La solicitud relacionada con <strong>" + HtmlUtils.htmlEscape(title) + "</strong>"
                + " cambio a estado <strong>" + status.name() + "</strong>.</p>"
                + "<p>Ingresa a Carpool UTEC para revisar los detalles.</p>"
                + "<p>Equipo Carpool UTEC</p>";
    }
}
