/*
    The Quartz Job that sends the email.
    Copyright © 2019 Omar Tanner
 */

package com.omartanner.emailscheduler.lib;

import com.healthmarketscience.jackcess.Row;
import com.omartanner.emailscheduler.Main;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.mail.MessagingException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class EmailerJob implements Job {
    // Empty constructor required by all Quartz Job implementers
    public EmailerJob() {

    }

    public void execute(JobExecutionContext context) throws JobExecutionException {
        // Set the JobExecutionContext result to 0 indicating failure so far
        context.setResult(0);

        // Obtain emailer properties JSON
        Map<String, Object> properties = null;
        try {
            properties = Data.loadEmailerProperties();
        }
        catch (URISyntaxException | FileNotFoundException e) { // Failure loading emailer properties
            throw new JobExecutionException("[EmailerJob] Failure to load emailer JSON properties! Stack trace: " + e.getStackTrace(), e);
        }

        // Obtain SMTP host, port, username and password from emailer properties
        String host = (String) properties.get("host");
        Integer port = ((Double) properties.get("port")).intValue();
        String username = (String) properties.get("username");
        String password = (String) properties.get("password");

        // Obtain DbConnection constructed in Main
        DbConnection connection = Main.getDbConnection();

        // Obtain certificates with property from the DbConnection object
        ArrayList<Row> certificates = null;
        try {
            certificates = connection.getCertificates();
        }
        catch (IOException e) { // Failure querying database for the certificates
            throw new JobExecutionException("[EmailerJob] Failure to query database for certificates! Stack trace: " + e.getStackTrace(), e);
        }

        // Now sort the certificates, such that the earliest to expire come first
        sortCertificates(certificates);

        // Build the email message string from the input sorted certificates
        String msg = buildMessage(certificates);

        // Construct email service
        EmailService emailer = new EmailService(host, port, username, password);

        // Obtain from and to parameters from emailer properties JSON
        String from = (String) properties.get("sender");
        ArrayList<String> recipients = (ArrayList<String>) properties.get("recipients");
        // Format recipients into a single string separated by commas
        String to = String.join(",", recipients);
        // Obtain count of certificates
        int certCount = certificates.size();
        // Construct subject
        String certsWord = (certCount == 1) ? "Certificate" : "Certificates";
        String subject = "Product Compliance Alert: " + certCount + " Expiring " + certsWord;
        // Send the email
        try {
            emailer.sendMail(msg, subject, from, to);
        }
        catch (MessagingException e) { // Failure to send email
            throw new JobExecutionException("[EmailerJob] Failure to send email! Stack trace: " + e.getStackTrace(), e);
        }

        // Store message info in a HashMap
        HashMap<String, String> msgInfo = new HashMap<>();
        msgInfo.put("subject", subject);
        msgInfo.put("from", from);
        msgInfo.put("to", to);
        msgInfo.put("message-body", msg);
        // Set the JobExecutionContext to the sent message information indicating success
        context.setResult(msgInfo);
    }

    // Sort input certificates, such that the earliest to expire come first
    private void sortCertificates(ArrayList<Row> certificates) {
        Collections.sort(certificates, new Comparator<Row>() {
                    // c1 > c2 if expiry date of c1 is after c2's
                    @Override
                    public int compare(Row c1, Row c2) {
                        Date expiry1 = (Date) c1.get("certificateExpireDate");
                        Date expiry2 = (Date) c2.get("certificateExpireDate");
                        // Same data means 0 for same
                        if (expiry1.equals(expiry2)) return 0;
                        // Expiry of cert 1 > expiry of cert2 means c1 > c2
                        if (expiry1.after(expiry2)) return 1;
                        // Otherwise cert 1 expires before cert2 so is smaller
                        return -1;
                    }
                }
        );
    }

    // Build the email message string
    private String buildMessage(ArrayList<Row> certificates) {
        // Initialise builder
        StringBuilder builder = new StringBuilder();
        // Now construct HTML message
        builder.append("<p>The following certificates have been found to expire within <b>3</b> months:</p><ul>");
        for (Row certificate : certificates) {
            // Obtain expiry Instant for the certificate
            Instant expiry = ((Date) certificate.get("certificateExpireDate")).toInstant();
            // Obtain instant for today
            Instant today = new Date().toInstant();
            // Obtain number of days between certificate expiry instant and today
            long totalDays = ChronoUnit.DAYS.between(today, expiry);
            // Compute week + days from total days
            long weeks = totalDays / 7;
            long days = totalDays % 7;
            // Format certificate info
            String certificateData = weeks + " weeks, " + days + " days";
            String certificateName = certificate.getString("certificateName");
            builder.append("<li><p><b>");
            builder.append(certificateName);
            builder.append("</b> - ");
            builder.append(certificateData);
            builder.append("</p></li>");
        }
        builder.append("</ul>");
        return builder.toString();
    }
}
