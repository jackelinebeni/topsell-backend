package com.topsell.backend.service;

import com.topsell.backend.dto.QuoteItemDto;
import com.topsell.backend.dto.QuoteRequest;
import com.topsell.backend.entity.Product;
import com.topsell.backend.entity.Quote;
import com.topsell.backend.entity.QuoteItem;
import com.topsell.backend.entity.User;
import com.topsell.backend.repository.ProductRepository;
import com.topsell.backend.repository.QuoteRepository;
import com.topsell.backend.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuoteService {

    private final JavaMailSender mailSender;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final QuoteRepository quoteRepository;

    public void sendQuote(QuoteRequest request) throws MessagingException {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // --- Lógica de negocio (Cálculos y Persistencia) ---
        BigDecimal granTotal = BigDecimal.ZERO;
        List<QuoteItem> quoteItems = new ArrayList<>();

        // Crear la cotización
        Quote quote = new Quote();
        quote.setUser(user);
        quote.setDate(LocalDateTime.now());
        quote.setTotalAmount(BigDecimal.ZERO); // Se actualizará después
        
        // --- Construcción del HTML Estilizado ---
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<div style='font-family: Arial, sans-serif; max-width: 800px; margin: auto; padding: 20px; border: 1px solid #eee;'>");
        
        // Contenedor del Logo en la esquina superior derecha
        htmlContent.append("<div style='text-align: right; width: 100%; margin-bottom: 10px;'>");
        htmlContent.append("<img src='cid:logoTopsell' style='width: 120px; height: auto;' alt='Logo'>");
        htmlContent.append("</div>");

        htmlContent.append("<h1 style='font-size: 22px; color: #333; margin-top: 0;'>Hola ").append(user.getFirstName()).append(",</h1>");
        htmlContent.append("<p style='color: #555;'>Gracias por cotizar con <strong>Topsell</strong>. Aquí está el detalle de tu solicitud:</p>");

        // Tabla de productos
        htmlContent.append("<table style='width: 100%; border-collapse: collapse; margin-top: 20px;'>");
        htmlContent.append("<tr style='background-color: #f9f9f9;'>")
                .append("<th style='border: 1px solid #ddd; padding: 12px; text-align: left;'>Producto</th>")
                .append("<th style='border: 1px solid #ddd; padding: 12px; text-align: center;'>Cant</th>")
                .append("<th style='border: 1px solid #ddd; padding: 12px; text-align: right;'>Precio Unit.</th>")
                .append("<th style='border: 1px solid #ddd; padding: 12px; text-align: right;'>Subtotal</th>")
                .append("</tr>");

        for (QuoteItemDto item : request.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + item.getProductId()));

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            granTotal = granTotal.add(subtotal);

            // Crear QuoteItem
            QuoteItem quoteItem = new QuoteItem();
            quoteItem.setProduct(product);
            quoteItem.setQuantity(item.getQuantity());
            quoteItem.setUnitPrice(product.getPrice());
            quoteItem.setSubtotal(subtotal);
            quoteItems.add(quoteItem);

            htmlContent.append("<tr>");
            htmlContent.append("<td style='border: 1px solid #ddd; padding: 12px;'>").append(product.getName()).append("</td>");
            htmlContent.append("<td style='border: 1px solid #ddd; padding: 12px; text-align: center;'>").append(item.getQuantity()).append("</td>");
            htmlContent.append("<td style='border: 1px solid #ddd; padding: 12px; text-align: right;'>S/ ").append(String.format("%.2f", product.getPrice())).append("</td>");
            htmlContent.append("<td style='border: 1px solid #ddd; padding: 12px; text-align: right;'>S/ ").append(String.format("%.2f", subtotal)).append("</td>");
            htmlContent.append("</tr>");
        }

        htmlContent.append("</table>");

        // Total y Pie de página
        htmlContent.append("<div style='text-align: right; margin-top: 20px;'>");
        htmlContent.append("<h3 style='color: #1a1a1a;'>Total Cotizado: S/ ").append(String.format("%.2f", granTotal)).append("</h3>");
        htmlContent.append("</div>");
        
        htmlContent.append("<p style='margin-top: 40px; color: #777; font-size: 13px;'>Un asesor comercial se pondrá en contacto contigo pronto.</p>");
        htmlContent.append("</div>");

        // Guardar en la base de datos
        quote.setTotalAmount(granTotal);
        quote.setItems(quoteItems);
        quoteRepository.save(quote);

        // Envío del correo procesando el recurso inline
        sendEmailWithInlineImage(user.getEmail(), "Tu Cotización Topsell", htmlContent.toString());
    }

    private void sendEmailWithInlineImage(String to, String subject, String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);

        // Buscamos el logo en la ruta que mencionaste
        ClassPathResource image = new ClassPathResource("static/logo.png");
        helper.addInline("logoTopsell", image);

        mailSender.send(message);
    }
}