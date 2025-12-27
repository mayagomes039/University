package pt.uminho.di.aa.miradourum.utils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String to, String comment) throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();

        // true = multipart message
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("Your Point of Interest Was Rejected");

        String htmlContent = "<html>" +
                "<body style='font-family: Arial, sans-serif; background-color: #000000; color: #ffffff;'>" +
                "<div style='max-width: 600px; margin: auto; padding: 20px; border: 1px solid #444; background-color: #111111;'>" +
                "<img src='cid:logoImage' style='width: 150px; display: block; margin-bottom: 20px;'/>" +
                "<h2 style='color: #e74c3c;'>Point of Interest Rejected</h2>" +
                "<p>Hello,</p>" +
                "<p>Your submitted point of interest was <strong>rejected</strong> for the following reason:</p>" +
                "<blockquote style='border-left: 4px solid #e74c3c; margin: 10px 0; padding-left: 15px; color: #dddddd;'>" +
                comment +
                "</blockquote>" +
                "<p>We appreciate your interest in contributing to <strong style='color: #ffffff;'>Miradourum</strong>.</p>" +
                "<p>Regards,<br/>The Miradourum Team</p>" +
                "</div>" +
                "</body>" +
                "</html>";


        helper.setText(htmlContent, true);

        // Embed the logo
        ClassPathResource logoImage = new ClassPathResource("/static/images/logo2.png"); // Make sure this path is correct
        helper.addInline("logoImage", logoImage);
        mailSender.send(message);
    }
}
