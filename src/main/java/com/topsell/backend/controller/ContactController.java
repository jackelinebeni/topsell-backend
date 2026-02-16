package com.topsell.backend.controller;

import com.topsell.backend.dto.ContactRequest;
import com.topsell.backend.entity.Contact;
import com.topsell.backend.entity.User;
import com.topsell.backend.entity.UserSuscriptores;
import com.topsell.backend.repository.ContactRepository;
import com.topsell.backend.service.ContactService;
import com.topsell.backend.service.ReCaptchaService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private ContactService contactService;

    @Autowired
    private ReCaptchaService reCaptchaService;

    // ========== ENDPOINT PÚBLICO (TIENDA) ==========

    @PostMapping
    public ResponseEntity<?> createContact(@RequestBody ContactRequest request) {
        try {
            Contact savedContact = contactService.createContact(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedContact);
        } catch (IllegalArgumentException e) {
            // Capturamos las validaciones del servicio (reCAPTCHA, regex, length)
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Ocurrió un error inesperado al procesar tu solicitud."));
        }
    }
    @PostMapping("/subscribe")
    public ResponseEntity<?> createSuscription(@RequestBody Map<String, Object> payload) {
        try {
            Object v = payload.get("verificacion");
            boolean verificacion = v instanceof Boolean ? (Boolean) v : Boolean.parseBoolean(String.valueOf(v));
            String email = (String) payload.get("email");

            if (!verificacion) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Por favor, completa la verificación reCAPTCHA."));
            }

            if (email == null || email.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Email inválido."));
            }

            UserSuscriptores savedContact = contactService.createSuscripcion(email);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedContact);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ========== ENDPOINTS ADMIN ==========

    @GetMapping("/admin")
    public List<Contact> getAllContacts() {
        return contactRepository.findAllByOrderByFechaCreacionDesc();
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<Contact> getContactById(@PathVariable Long id) {
        return contactRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/admin/{id}/read")
    public ResponseEntity<Contact> markAsRead(@PathVariable Long id) {
        return contactRepository.findById(id)
                .map(contact -> {
                    contact.setLeido(true);
                    return ResponseEntity.ok(contactRepository.save(contact));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> deleteContact(@PathVariable Long id) {
        return contactRepository.findById(id)
                .map(contact -> {
                    contactRepository.delete(contact);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
