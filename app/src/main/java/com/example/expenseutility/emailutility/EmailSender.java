package com.example.expenseutility.emailutility;

import android.os.AsyncTask;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import android.util.Log;
import java.io.File;

public class EmailSender extends AsyncTask<Void, Void, Void> {
    private String recipientEmail;
    private String subject;
    private String messageBody;
    private File fileAttachment;

    public EmailSender(String recipientEmail, String subject, String messageBody, File fileAttachment) {
        this.recipientEmail = recipientEmail;
        this.subject = subject;
        this.messageBody = messageBody;
        this.fileAttachment = fileAttachment;
    }

    @Override
    protected Void doInBackground(Void... params) {
        // Set up mail server properties
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com"); // Use Google's SMTP server
        props.put("mail.smtp.port", "465"); // SMTP port (TLS/SSL)
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        // Create a new session
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                // Sender email and password (use app-specific password for Gmail)
                return new PasswordAuthentication("rkurbetti30@gmail.com", "dvgz etbv paxv piko");
            }
        });

        try {
            // Create a default MimeMessage object
            MimeMessage message = new MimeMessage(session);

            // Set sender email
            message.setFrom(new InternetAddress("rkurbetti30@gmail.com"));

            // Set recipient email
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress("kurbettirohit75@gmail.com"));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress("rohitbackup0001@gmail.com"));

            // Set subject
            message.setSubject(subject);

            // Create the message part
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(messageBody);

            // Create a multipart message for attachment
            MimeMultipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            // Attach the file
            if (fileAttachment != null && fileAttachment.exists()) {
                MimeBodyPart attachmentBodyPart = new MimeBodyPart();
                attachmentBodyPart.attachFile(fileAttachment);
                multipart.addBodyPart(attachmentBodyPart);
            }

            // Set the multipart message to the email
            message.setContent(multipart);

            // Send the message
            Transport.send(message);

            Log.d("EmailSender", "Email sent successfully");
        } catch (MessagingException | java.io.IOException e) {
            e.printStackTrace();
            Log.e("EmailSender", "Failed to send email", e);
        }

        return null;
    }
}
