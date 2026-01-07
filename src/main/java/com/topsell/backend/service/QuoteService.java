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
        // 1. Obtener usuario autenticado (desde el JWT)
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 2. CREAR OBJETO QUOTE PARA BD
        Quote quote = new Quote();
        quote.setUser(user);
        quote.setDate(LocalDateTime.now());

        List<QuoteItem> quoteItems = new ArrayList<>();
        BigDecimal granTotal = BigDecimal.ZERO;

        // 3. Construir el contenido del correo y calcular totales
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<h1>Hola ").append(user.getFirstName()).append(",</h1>");
        htmlContent.append("<p>Gracias por cotizar con Topsell. Aquí está el detalle de tu solicitud:</p>");
        htmlContent.append("<table border='1' cellpadding='10' style='border-collapse: collapse; width: 100%;'>");
        htmlContent.append("<tr style='background-color: #f2f2f2;'><th>Producto</th><th>Cant</th><th>Precio Unit.</th><th>Subtotal</th></tr>");



        for (QuoteItemDto item : request.getItems()) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + item.getProductId()));

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())); // Precio real de BD
            granTotal = granTotal.add(subtotal);

            QuoteItem dbItem = new QuoteItem();
            dbItem.setProduct(product);
            dbItem.setQuantity(item.getQuantity());
            dbItem.setUnitPrice(product.getPrice());
            dbItem.setSubtotal(subtotal);
            quoteItems.add(dbItem);

            htmlContent.append("<tr>");
            htmlContent.append("<td>").append(product.getName()).append("</td>");
            htmlContent.append("<td style='text-align: center;'>").append(item.getQuantity()).append("</td>");
            htmlContent.append("<td style='text-align: right;'>S/ ").append(String.format("%.2f", product.getPrice())).append("</td>");
            htmlContent.append("<td style='text-align: right;'>S/ ").append(String.format("%.2f", subtotal)).append("</td>");
            htmlContent.append("</tr>");
        }

        htmlContent.append("</table>");
        htmlContent.append("<h3 style='text-align: right;'>Total Cotizado: S/ ").append(String.format("%.2f", granTotal.doubleValue())).append("</h3>");
        htmlContent.append("<p>Un asesor comercial se pondrá en contacto contigo pronto.</p>");

        // Finalizar objeto BD
        quote.setItems(quoteItems);
        quote.setTotalAmount(granTotal);

        // 4. GUARDAR EN BD
        quoteRepository.save(quote);

        // 5. Enviar el Correo
        sendHtmlEmail(user.getEmail(), "Tu Cotización Topsell", htmlContent.toString());
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true); // true indica que es HTML

        mailSender.send(message);
    }
}