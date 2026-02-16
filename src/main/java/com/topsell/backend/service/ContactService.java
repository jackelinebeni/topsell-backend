package com.topsell.backend.service;

import com.resend.Resend; // Opcional: Si quieres notificaciones
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions; // Opcional
import com.topsell.backend.dto.ContactRequest;
import com.topsell.backend.entity.Contact;
import com.topsell.backend.repository.ContactRepository;
import lombok.RequiredArgsConstructor;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime; // Asumiendo que guardas la fecha manualmente si no tienes @PrePersist
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ContactService {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private ReCaptchaService reCaptchaService;

    @Autowired
    private Resend resend; // Descomenta si quieres enviar alertas al admin

    @Value("${resend.from.email.contact}")
    private String fromEmail;

    @Value("${resend.admin.email.contact}")
    private String adminEmail;

    private static final String LOGO_URL = "https://www.topsell.pe/logo.png";

    // --- LÓGICA DE NEGOCIO PÚBLICA ---

    public Contact createContact(ContactRequest request) {
       // Verificar reCAPTCHA
        if (!reCaptchaService.verifyToken(request.getRecaptchaToken())) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Verificación de reCAPTCHA fallida. Por favor, intenta nuevamente.");
            throw new IllegalArgumentException("Verificación de reCAPTCHA fallida.");
        }

        // 2. Validaciones básicas
        if (request.getCorreo() == null || !request.getCorreo().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Correo electrónico inválido.");
        }
        if (request.getMensaje() == null || request.getMensaje().length() < 10) {
            throw new IllegalArgumentException("El mensaje debe tener al menos 10 caracteres.");
        }

        // 3. Guardar en Base de Datos
        Contact contact = new Contact();
        contact.setNombres(request.getNombres());
        contact.setApellidos(request.getApellidos());
        contact.setDniOrRuc(request.getDniOrRuc());
        contact.setRazonSocial(request.getRazonSocial());
        contact.setCorreo(request.getCorreo());
        contact.setMensaje(request.getMensaje());
        contact.setFechaCreacion(LocalDateTime.now());

        Contact savedContact = contactRepository.save(contact);

        // 4. Enviar Correos (Asíncrono idealmente, pero síncrono funciona bien aquí)
        sendClientConfirmationEmail(savedContact); // Correo al Usuario
        sendAdminNotificationEmail(savedContact);  // Correo al Admin (Tú)

        return savedContact;
    }

    // --- MÉTODOS DE CORREO (Privados) ---

    private void sendClientConfirmationEmail(Contact contact) {
        String subject = "Hemos recibido tu mensaje - Topsell";
        String htmlBody = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #eee; padding: 20px;">
                <div style="text-align: center; margin-bottom: 20px;">
                    <img src="%s" alt="Topsell Logo" style="width: 150px;">
                </div>
                <h2 style="color: #333; text-align: center;">¡Hola, %s!</h2>
                <p style="color: #555; font-size: 16px; line-height: 1.5;">
                    Gracias por ponerte en contacto con <strong>Topsell</strong>. Hemos recibido tu mensaje correctamente.
                </p>
                <p style="color: #555; font-size: 16px; line-height: 1.5;">
                    Nuestro equipo revisará tu solicitud y te responderemos a la brevedad posible.
                </p>
                <hr style="border: 0; border-top: 1px solid #eee; margin: 20px 0;">
                <p style="text-align: center; color: #999; font-size: 12px;">
                    &copy; 2026 Topsell. Todos los derechos reservados.<br>
                    <a href="https://topsell.pe" style="color: #007bff; text-decoration: none;">Visita nuestro sitio web</a>
                </p>
            </div>
            """.formatted(LOGO_URL, contact.getNombres());

        sendEmail(contact.getCorreo(), subject, htmlBody);
    }

    private void sendAdminNotificationEmail(Contact contact) {
        String subject = "🔔 Nuevo Lead Web: " + contact.getNombres() + " " + contact.getApellidos();
        String htmlBody = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #333; padding: 20px;">
                <div style="text-align: right;">
                    <img src="%s" alt="Logo" style="width: 100px;">
                </div>
                <h2 style="color: #d9534f;">Nuevo Contacto Recibido</h2>
                <p>Un usuario ha llenado el formulario de contacto en la web.</p>
                
                <table style="width: 100%%; border-collapse: collapse; margin-top: 15px;">
                    <tr style="background-color: #f9f9f9;">
                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Nombre:</strong></td>
                        <td style="padding: 10px; border: 1px solid #ddd;">%s %s</td>
                    </tr>
                    <tr>
                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Email:</strong></td>
                        <td style="padding: 10px; border: 1px solid #ddd;"><a href="mailto:%s">%s</a></td>
                    </tr>
                    <tr style="background-color: #f9f9f9;">
                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>DNI/RUC:</strong></td>
                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                    </tr>
                    <tr>
                        <td style="padding: 10px; border: 1px solid #ddd;"><strong>Empresa:</strong></td>
                        <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                    </tr>
                </table>

                <h3 style="margin-top: 20px;">Mensaje:</h3>
                <div style="background-color: #f4f4f4; padding: 15px; border-left: 4px solid #007bff; font-style: italic;">
                    "%s"
                </div>
                
                <div style="margin-top: 30px; text-align: center;">
                    <a href="mailto:%s" style="background-color: #28a745; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">Responder Ahora</a>
                </div>
            </div>
            """.formatted(
                LOGO_URL,
                contact.getNombres(), contact.getApellidos(),
                contact.getCorreo(), contact.getCorreo(),
                contact.getDniOrRuc() != null ? contact.getDniOrRuc() : "-",
                contact.getRazonSocial() != null ? contact.getRazonSocial() : "-",
                contact.getMensaje(),
                contact.getCorreo()
        );

        sendEmail(adminEmail, subject, htmlBody);
    }

    private void sendEmail(String to, String subject, String htmlContent) {
        try {
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from("Topsell <" + fromEmail + ">")
                    .to(to)
                    .subject(subject)
                    .html(htmlContent)
                    .build();

            resend.emails().send(params);
        } catch (ResendException e) {
            // No lanzamos excepción para no romper el flujo de guardado del contacto
        }
    }
}
