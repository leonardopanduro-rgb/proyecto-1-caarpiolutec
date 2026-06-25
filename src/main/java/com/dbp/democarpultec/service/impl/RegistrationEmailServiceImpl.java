package com.dbp.democarpultec.service.impl;

import com.dbp.democarpultec.service.RegistrationEmailService;
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
public class RegistrationEmailServiceImpl implements RegistrationEmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@carpoolutec.local}")
    private String fromAddress;

    public void sendWelcomeEmail(String toEmail, String recipientName) {
        if (mailSender == null) {
            log.info("Skipping registration email because JavaMailSender is not configured");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("Bienvenido a Carpool UTEC");
            helper.setText(buildHtmlBody(recipientName), true);
            mailSender.send(message);
        } catch (Exception ex) {
            log.warn("Could not send registration email to {}: {}", toEmail, ex.getMessage());
        }
    }

    private String buildHtmlBody(String recipientName) {
        String name = recipientName == null || recipientName.isBlank() ? "estudiante" : recipientName;
        return "<h2>Hola " + HtmlUtils.htmlEscape(name) + "</h2>"
                + "<p>Tu cuenta en <strong>Carpool UTEC</strong> fue creada correctamente.</p>"
                + "<p>Ya puedes iniciar sesion y publicar o solicitar viajes.</p>"
                + "<p>Equipo Carpool UTEC</p>";
    }
}
