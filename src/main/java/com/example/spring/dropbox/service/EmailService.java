package com.example.spring.dropbox.service;


import com.example.spring.dropbox.pojo.EmailDetails;

// Interface
public interface EmailService {

    // Method
    // To send a simple email
    String sendSimpleMail(EmailDetails details);

    // Method
    // To send an email with attachment
    String sendMailWithAttachment(EmailDetails details);

    String sendMailWithCsvAttachment(EmailDetails details);

    void consolidatePaidOrderAndTriggerMail(EmailDetails details, String currentDate);

    void consolidatePaidOrderAutomatic();

}
