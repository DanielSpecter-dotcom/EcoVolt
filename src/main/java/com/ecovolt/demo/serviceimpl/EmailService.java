package com.ecovolt.demo.serviceimpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarCodigoVerificacion(String correoDestino, String codigo) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(from);
        mensaje.setTo(correoDestino);
        mensaje.setSubject("EcoVolt - Codigo de verificacion de cuenta");
        mensaje.setText(
                "Hola,\n\n"
                + "Tu codigo de verificacion de EcoVolt es: " + codigo + "\n\n"
                + "Este codigo vence en 24 horas. Si no creaste esta cuenta, ignora este correo.\n\n"
                + "Equipo EcoVolt"
        );

        try {
            mailSender.send(mensaje);
        } catch (Exception ex) {
            log.warn("No se pudo enviar el correo de verificacion a {}: {}", correoDestino, ex.getMessage());
        }
    }
}
