package com.topsell.backend.controller;

import com.topsell.backend.dto.ContactRequest;
import com.topsell.backend.entity.Contact;
import com.topsell.backend.repository.ContactRepository;
import com.topsell.backend.service.ReCaptchaService;
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
    private ReCaptchaService reCaptchaService;

    // ========== ENDPOINT PÚBLICO (TIENDA) ==========

    @PostMapping
    public ResponseEntity<?> createContact(@RequestBody ContactRequest request) {
        // Verificar reCAPTCHA
        if (!reCaptchaService.verifyToken(request.getRecaptchaToken())) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Verificación de reCAPTCHA fallida. Por favor, intenta nuevamente.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        // Validaciones adicionales
        if (request.getCorreo() == null || !request.getCorreo().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Correo electrónico inválido");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        if (request.getMensaje() == null || request.getMensaje().length() < 10) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "El mensaje debe tener al menos 10 caracteres");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        // Crear la entidad Contact desde el DTO
        Contact contact = new Contact();
        contact.setNombres(request.getNombres());
        contact.setApellidos(request.getApellidos());
        contact.setDniOrRuc(request.getDniOrRuc());
        contact.setRazonSocial(request.getRazonSocial());
        contact.setCorreo(request.getCorreo());
        contact.setMensaje(request.getMensaje());

        Contact savedContact = contactRepository.save(contact);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedContact);
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
