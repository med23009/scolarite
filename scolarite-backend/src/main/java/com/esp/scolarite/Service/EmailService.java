package com.esp.scolarite.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendBulletinEmail(String to, byte[] pdfBytes) throws MessagingException {
    // Nettoyage de l’adresse
    String cleanEmail = to.trim().replace(";", "");

    MimeMessage message = mailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

    helper.setTo(cleanEmail); // ✅ utiliser l’adresse nettoyée
    helper.setSubject("Votre relevé de notes");
    helper.setText("Bonjour,\n\nVeuillez trouver en pièce jointe votre relevé de notes.\n\nCordialement,\nLa scolarité.");

    helper.addAttachment("releve.pdf", new ByteArrayResource(pdfBytes));

    mailSender.send(message);
}

}
