package com.topsell.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Value("${mail.quotes.host}")
    private String quotesHost;

    @Value("${mail.quotes.port}")
    private int quotesPort;

    @Value("${mail.quotes.username}")
    private String quotesUsername;

    @Value("${mail.quotes.password}")
    private String quotesPassword;

    @Value("${mail.contacts.host}")
    private String contactsHost;

    @Value("${mail.contacts.port}")
    private int contactsPort;

    @Value("${mail.contacts.username}")
    private String contactsUsername;

    @Value("${mail.contacts.password}")
    private String contactsPassword;

    private void setCommonProperties(JavaMailSenderImpl mailSender) {
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        
        // Propiedades críticas para evitar el timeout
        props.put("mail.smtp.connectiontimeout", "5000"); // 5 segundos para conectar
        props.put("mail.smtp.timeout", "5000");           // 5 segundos para leer datos
        props.put("mail.smtp.writetimeout", "5000");      // 5 segundos para enviar
        
        // Importante para Ferozo si hay temas de certificados
        props.put("mail.smtp.ssl.trust", "*"); 
        
        // Activa esto temporalmente a "true" para ver el log exacto en consola si falla
        props.put("mail.debug", "true"); 
    }

    @Bean(name = "quotesMailSender")
    @Primary
    public JavaMailSender quotesMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(quotesHost);
        mailSender.setPort(quotesPort);
        mailSender.setUsername(quotesUsername);
        mailSender.setPassword(quotesPassword);
        setCommonProperties(mailSender);
        return mailSender;
    }

    @Bean(name = "contactsMailSender")
    public JavaMailSender contactsMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(contactsHost);
        mailSender.setPort(contactsPort);
        mailSender.setUsername(contactsUsername);
        mailSender.setPassword(contactsPassword);
        setCommonProperties(mailSender);
        return mailSender;
    }
}
