package com.example.spring.dropbox.controller;

import com.example.spring.dropbox.pojo.EmailDetails;
import com.example.spring.dropbox.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@RestController
@RequestMapping("email")
@CrossOrigin
public class EmailController {

    @GetMapping("/")
    public String index() throws Exception {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        String currentDate = dateFormat.format(new Date());

        return "version v2 email controller currentDate - " + currentDate;
    }
    @Autowired
    private EmailService emailService;

    // Sending a simple Email
    @PostMapping("/sendMail")
    public String
    sendMail(@RequestBody EmailDetails details)
    {
        String status
                = emailService.sendSimpleMail(details);

        return status;
    }

    // Sending email with attachment
    @PostMapping("/sendMailWithAttachment")
    public String sendMailWithAttachment(
            @RequestBody EmailDetails details)
    {
        String status
                = emailService.sendMailWithAttachment(details);

        return status;
    }

    @PostMapping("/sendPaidOrder")
    public String sendMailWithCsvAttachment(
            @RequestBody EmailDetails details)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        String currentDate = dateFormat.format(new Date());
        emailService.consolidatePaidOrderAndTriggerMail(details, currentDate);

        return "status";
    }

    @GetMapping("/sendPaidOrderAuto")
    public String sendPaidOrderAuto()
    {
        emailService.consolidatePaidOrderAutomatic();
        return "status";
    }


    @GetMapping("/sendEntireOrderBackupDaily")
    public String sendEntireOrderBackupDaily()
    {
        emailService.sendEntireOrderBackupDaily();
        return "status";
    }


    @GetMapping("/clearOrderFiles")
    public String clearOrderFiles()
    {
        emailService.clearOrderFiles();
        return "status";
    }
}
