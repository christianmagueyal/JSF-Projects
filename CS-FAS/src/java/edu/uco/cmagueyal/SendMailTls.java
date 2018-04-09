package edu.uco.cmagueyal;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class SendMailTls implements Serializable {
    private final SimpleDateFormat dFormat = new SimpleDateFormat("EEEE MMMM dd, YYYY");
    private final SimpleDateFormat tFormat = new SimpleDateFormat("hh:mm a");
    private final String username = "DoNotReplyCMagueyal@gmail.com";
    private final String password = "DNRCMAGUEYAL";

    public void registerEmail(String firstName, String recipient, String code) {
        String htmlMessage = "<h1>Thank You For Registering!</h1>\n"
                + "<br/>\n"
                + "<p style='font-size:20px'>Hello " + firstName + ", below is your registration code. Please enter it</p>\n"
                + "<p style='font-size:20px'>in the confirmation page to validate your account.</p>\n"
                + "<br/>\n"
                + "<b><p style='font-size:24px'> "+ code + "</p></b>";
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        try {
            Message message = new MimeMessage(session);
            message.setContent(htmlMessage, "text/html; charset=utf-8");
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(recipient));
            message.setSubject("Account Confirmation");
            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
    public void appointmentSignUpEmail(Appointment app, User student) {
        String htmlMessage = "<h1>New Advising Appointment!</h1>\n"
                + "<br></br>\n"
                + "<p style='font-size:20px'>" + student.getFirstName() + " " + student.getLastName() + " wants to be advised on:</p>\n"
                + "<b><p style='font-size:24px'>"+ dFormat.format(app.getDate()) + " at " + tFormat.format(app.getTime()) + "</p></b>\n"
                + "<p style='font-size:20px'>Login to CS Faculty Advisement System to view all appointments </p>\n"
                + "\n"
                + "   <br></br>\n";
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        try {
            Message message = new MimeMessage(session);
            message.setContent(htmlMessage, "text/html; charset=utf-8");
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(app.getAdvisor()));
            message.setSubject(""+ student.getUcoID() +" Advising Appointment");
            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }    
    public void studentCancelledEmail(Date day, Date time, String recipient) {
        String htmlMessage = "<h1>Student Cancelled Advising Appointment!</h1>\n"
                + "<br/>\n"
                + "<p style='font-size:20px'>Advisment appointment on: </p>\n"
                + "<b><p style='font-size:24px'>" +dFormat.format(day)+ " at "+tFormat.format(time)+ "</p></b>\n"                
                + "<p style='font-size:20px'>has been cancelled.</p>\n"
                + "<p style='font-size:20px'>Login to CS Faculty Advisement System to view all appointments.</p>\n";
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        try {
            Message message = new MimeMessage(session);
            message.setContent(htmlMessage, "text/html; charset=utf-8");
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(recipient));
            message.setSubject("Student Advisment Cancellation");
            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
    public void advisorCancelledEmail(Appointment app) {
        String htmlMessage = "<h1>Your Advisment Appointment is Cancelled</h1>\n"
                + "<br/>\n"
                + "<p style='font-size:20px'>Professor " + app.getAdvisorLastName() + " has cancelled your appointment scheduled on:</p>\n"
                + "<b><p style='font-size:24px'>"+ dFormat.format(app.getDate()) + " at " + tFormat.format(app.getTime()) + "</p></b>\n"
                + "<p style='font-size:20px'> Please contact your advisor at: "+ app.getAdvisor() +"</p>\n" 
                + "<p style='font-size:20px'>  with any questions regarding this message" +".</p>\n";
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        try {
            Message message = new MimeMessage(session);
            message.setContent(htmlMessage, "text/html; charset=utf-8");
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(app.getStudent()));
            message.setSubject("CS Advisment Canceled");
            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }
    public void passwordResetEmail(String studentName, String pw, String recipient) {
        String htmlMessage = "<h1>CS Faculty Advisement System Password Reset</h1>\n"
                + "<br/>\n"
                + "<p style='font-size:20px'>Hello " + studentName + ", your password has been reset.</p>\n"
                + "<p style='font-size:20px'>The following password has been assigned to your account:</p>\n"
                + "<b><p style='font-size:24px'> "+pw +"</p></b>\n"
                + "<p style='font-size:20px'>Use it to access your CS Faculty Advisement System account.</p>\n"
                + "<b><p style='font-size:20px'>We encourage you to change your password after logging in.</p></b>\n";
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        try {
            Message message = new MimeMessage(session);
            message.setContent(htmlMessage, "text/html; charset=utf-8");
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(recipient));
            message.setSubject("CS-FAS Password Reset");
            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }    
}
