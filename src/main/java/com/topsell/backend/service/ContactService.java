package com.topsell.backend.service;

import com.topsell.backend.entity.Contact;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ContactService {

    @Qualifier("contactsMailSender")
    private final JavaMailSender contactsMailSender;

    @Value("${mail.contacts.recipient:ventas@topsell.com.pe}")
    private String recipientEmail;

    public void sendContactNotification(Contact contact) throws MessagingException {
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<div style='font-family: Arial, sans-serif; max-width: 700px; margin: auto; padding: 20px; border: 1px solid #eee;'>");
        
        htmlContent.append("<h1 style='font-size: 22px; color: #333; border-bottom: 2px solid #007bff; padding-bottom: 10px;'>Nuevo Mensaje de Contacto</h1>");
        
        htmlContent.append("<div style='background-color: #f9f9f9; padding: 15px; border-radius: 5px; margin: 20px 0;'>");
        htmlContent.append("<p style='margin: 5px 0;'><strong>Fecha:</strong> ").append(contact.getFechaCreacion().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("</p>");
        htmlContent.append("<p style='margin: 5px 0;'><strong>Nombres:</strong> ").append(contact.getNombres() != null ? contact.getNombres() : "N/A").append("</p>");
        htmlContent.append("<p style='margin: 5px 0;'><strong>Apellidos:</strong> ").append(contact.getApellidos() != null ? contact.getApellidos() : "N/A").append("</p>");
        htmlContent.append("<p style='margin: 5px 0;'><strong>DNI/RUC:</strong> ").append(contact.getDniOrRuc() != null ? contact.getDniOrRuc() : "N/A").append("</p>");
        
        if (contact.getRazonSocial() != null && !contact.getRazonSocial().isEmpty()) {
            htmlContent.append("<p style='margin: 5px 0;'><strong>Razón Social:</strong> ").append(contact.getRazonSocial()).append("</p>");
        }
        
        htmlContent.append("<p style='margin: 5px 0;'><strong>Correo:</strong> <a href='mailto:").append(contact.getCorreo()).append("'>").append(contact.getCorreo()).append("</a></p>");
        htmlContent.append("</div>");
        
        htmlContent.append("<div style='margin: 20px 0;'>");
        htmlContent.append("<h3 style='color: #555; margin-bottom: 10px;'>Mensaje:</h3>");
        htmlContent.append("<div style='background-color: #fff; padding: 15px; border-left: 4px solid #007bff; white-space: pre-wrap;'>");
        htmlContent.append(contact.getMensaje());
        htmlContent.append("</div>");
        htmlContent.append("</div>");
        
        htmlContent.append("<div style='margin-top: 30px; padding-top: 15px; border-top: 1px solid #ddd; color: #777; font-size: 12px;'>");
        htmlContent.append("<p>Este mensaje fue enviado desde el formulario de contacto de Topsell.</p>");
        htmlContent.append("</div>");
        
        htmlContent.append("</div>");

        sendEmail(recipientEmail, "Nuevo Mensaje de Contacto - Topsell", htmlContent.toString());
    }

    private void sendEmail(String to, String subject, String body) throws MessagingException {
        MimeMessage message = contactsMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);

        contactsMailSender.send(message);
    }
}
