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
        
        // Configuraciones específicas para saltar firewalls de Cloud
        props.put("mail.smtp.ssl.enable", "true"); // Usar SSL directo
        props.put("mail.smtp.starttls.enable", "false"); // Desactivar STARTTLS
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");

        // Tiempos de espera más largos para Railway
        props.put("mail.smtp.connectiontimeout", "15000");
        props.put("mail.smtp.timeout", "15000");
        props.put("mail.smtp.writetimeout", "15000");
        
        // Activa esto para ver el log en la consola de Railway
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
