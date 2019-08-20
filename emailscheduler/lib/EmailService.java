/*
    This class enables the sending of an email.
    Copyright © 2019 Omar Tanner
 */

package com.omartanner.emailscheduler.lib;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailService {
    // SMTP details
    private String host = "";
    private int port = 0;
    private String username = "";
    private String password = "";

    // Constructor sets SMTP details
    public EmailService(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    // Sends an email to the emails separated by commas in `to`, from `from`, with subject `subject` and message `msg`.
    // THROWS: MessagingException when failing to construct or send Message.
    public void sendMail(String msg, String subject, String from, String to) throws MessagingException {
        // Configure properties for email sending, in which authentication shall occur but no TLS or SSL.
        Properties prop = new Properties();
        prop.put("mail.smtp.auth", true);
        prop.put("mail.smtp.ehlo", false);
        prop.put("mail.smtp.starttls.enable", "false");
        prop.put("mail.smtp.host", host);
        prop.put("mail.smtp.port", port);
        prop.put("mail.smtp.ssl.trust", host);

        // Construct Session from an Authenticator which shall use PasswordAuthentication part of java.mail
        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        // Attempt to construct Message from input parameters and send.

        // Construct Message
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);

        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        mimeBodyPart.setContent(msg, "text/html");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(mimeBodyPart);

        message.setContent(multipart);

        System.out.println("Trying...");
        // Send Message
        Transport.send(message);
    }
}