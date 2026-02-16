package com.topsell.backend.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.topsell.backend.dto.QuoteItemDto;
import com.topsell.backend.dto.QuoteRequest;
import com.topsell.backend.entity.Product;
import com.topsell.backend.entity.Quote;
import com.topsell.backend.entity.QuoteItem;
import com.topsell.backend.entity.User;
import com.topsell.backend.repository.ProductRepository;
import com.topsell.backend.repository.QuoteRepository;
import com.topsell.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuoteService {

    private final Resend resend; // Inyectamos el cliente Resend
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final QuoteRepository quoteRepository;

    // Define tu dominio verificado en Resend o usa el de pruebas (onboarding@resend.dev)
    @Value("${resend.from.email.quotes}")
    private String fromEmail;

    // URL pública de tu logo. Las APIs funcionan mejor con URLs que con adjuntos inline.
    // Si no tienes una, puedes subirla a un bucket S3, Cloudinary o usar una URL temporal.
    private static final String LOGO_URL = "https://www.topsell.pe/logo.png";

    public void sendQuote(QuoteRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // --- Lógica de negocio (Cálculos y Persistencia) ---
        BigDecimal granTotal = BigDecimal.ZERO;
        List<QuoteItem> quoteItems = new ArrayList<>();

        // Crear la cotización (Entidad)
        Quote quote = new Quote();
        quote.setUser(user);
        quote.setDate(LocalDateTime.now());

        // --- Construcción del HTML Estilizado ---
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<div style='font-family: Arial, sans-serif; max-width: 800px; margin: auto; padding: 20px; border: 1px solid #eee;'>");

        // LOGO: Cambiado de 'cid:' a URL pública para mejor compatibilidad con Resend
        htmlContent.append("<div style='text-align: right; width: 100%; margin-bottom: 10px;'>");
        htmlContent.append("<img src='").append(LOGO_URL).append("' style='width: 120px; height: auto;' alt='Topsell Logo'>");
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

            // Filas de la tabla HTML
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

        // --- NUEVA SECCIÓN: Promoción y Call to Action ---
        htmlContent.append("<div style='background-color: #f0f8ff; border: 1px solid #cce5ff; padding: 15px; margin-top: 30px; border-radius: 5px;'>");

        // Título de la promoción
        htmlContent.append("<p style='color: #0056b3; font-weight: bold; font-size: 16px; margin: 0 0 10px 0;'>");
        htmlContent.append("¡Por lanzamiento de web! Envíos gratis a Lima Metropolitana para pedidos mayores de S/ 200.00.");
        htmlContent.append("</p>");

        // Condiciones (letra un poco más pequeña)
        htmlContent.append("<p style='font-size: 13px; color: #555; margin: 0 0 15px 0;'>");
        htmlContent.append("(Válido para compras confirmadas hasta el 15 marzo del 2026). <br><em>*Envíos a provincia por agencia.</em>");
        htmlContent.append("</p>");

        // Instrucción de compra (Call to Action)
        htmlContent.append("<p style='font-size: 15px; color: #333; margin: 0; border-top: 1px dashed #b8daff; padding-top: 10px;'>");
        htmlContent.append("<strong>¿Listo para comprar?</strong> Responde este correo confirmando tu solicitud, y un asesor comercial se pondrá en contacto contigo pronto para coordinar el pago y entrega.");
        htmlContent.append("</p>");

        htmlContent.append("</div>");
        // ------------------------------------------------

        htmlContent.append("</div>"); // Cierre del div principal

        // Guardar en Base de Datos
        quote.setTotalAmount(granTotal);
        quote.setItems(quoteItems);
        quoteRepository.save(quote);

        // Enviar con Resend
        sendEmailViaResend(user.getEmail(), "Tu Cotización Topsell", htmlContent.toString());
    }

    private void sendEmailViaResend(String to, String subject, String htmlBody) {
        try {
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from("Topsell <" + fromEmail + ">") // Debe ser un dominio verificado en Resend
                    .to(to)
                    .subject(subject)
                    .html(htmlBody)
                    .build();

            resend.emails().send(params);
            log.info("Correo enviado exitosamente a {}", to);

        } catch (ResendException e) {
            log.error("Error enviando correo con Resend: {}", e.getMessage());
            throw new RuntimeException("Error al enviar el correo de cotización", e);
        }
    }
}