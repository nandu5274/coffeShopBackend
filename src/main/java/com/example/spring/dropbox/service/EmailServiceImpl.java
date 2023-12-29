package com.example.spring.dropbox.service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.example.spring.dropbox.pojo.*;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

// Annotation
@Service
// Class
// Implementing EmailService interface
public class EmailServiceImpl implements EmailService {
    private static final Logger logger = LoggerFactory.getLogger(DropboxService.class);
    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    CkAccountDropboxService ckAccountDropboxService;
    @Autowired
    DropboxService dropboxService;
    @Autowired
    CSVReaderService cSVReaderService;

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${spring.mail.username}")
    private String sender;

    // Method 1
    // To send a simple email
    public String sendSimpleMail(EmailDetails details) {

        // Try block to check for exceptions
        try {

            // Creating a simple mail message
            SimpleMailMessage mailMessage
                    = new SimpleMailMessage();
            // Setting up necessary details
            mailMessage.setFrom(sender);
            mailMessage.setTo(details.getRecipient());
            mailMessage.setText(details.getMsgBody());
            mailMessage.setSubject(details.getSubject());

            // Sending the mail
            javaMailSender.send(mailMessage);
            return "Mail Sent Successfully...";
        }

        // Catch block to handle the exceptions
        catch (Exception e) {
            return "Error while Sending Mail";
        }
    }

    // Method 2
    // To send an email with attachment
    public String sendMailWithAttachment(EmailDetails details) {
        // Creating a mime message
        MimeMessage mimeMessage
                = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper;

        try {

            // Setting multipart as true for attachments to
            // be send
            mimeMessageHelper
                    = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setFrom(sender);
            mimeMessageHelper.setTo(details.getRecipient());
            mimeMessageHelper.setText(details.getMsgBody());
            mimeMessageHelper.setSubject(
                    details.getSubject());

            // Adding the attachment
            FileSystemResource file
                    = new FileSystemResource(
                    new File(details.getAttachment().getContent()));

            mimeMessageHelper.addAttachment(
                    file.getFilename(), file);

            // Sending the mail
            javaMailSender.send(mimeMessage);
            return "Mail sent Successfully";
        }

        // Catch block to handle MessagingException
        catch (MessagingException e) {

            // Display message when exception occurred
            return "Error while sending mail!!!";
        }
    }
    public void clearDeletedFolder() {
        try {
            List<String> fileNames = new ArrayList<>();
            DbxClientV2 dbxClientV2WithToken = dropboxService.dbxClientV2WithToken();
            dropboxService.permanentlyDeleted(dbxClientV2WithToken, "/");
        }catch(Exception e)
        {

        }
    }
    @Async
    public void clearOrderFiles() {
      //  clearDeletedFolder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        String currentDate = dateFormat.format(DateUtils.addDays(new Date(), -1));
        try{
            List<String> fileNames = new ArrayList<>();
            DbxClientV2 dbxClientV2WithToken = dropboxService.dbxClientV2WithToken();
            //get all files that needs to delete
            List<Metadata> PaidOrderEntries = dbxClientV2WithToken.files().listFolder("/orders/paid_orders").getEntries();
            for (Metadata entry : PaidOrderEntries) {
                if (entry instanceof FileMetadata) {
                    fileNames.add(entry.getPathDisplay());
                }
            }
            logger.info("\n paid order  files - {} ", fileNames.toString());
            List<Metadata> approvalWaitingOrderEntries = dbxClientV2WithToken.files().listFolder("/orders/approval_waiting_orders").getEntries();
            for (Metadata entry : approvalWaitingOrderEntries) {
                if (entry instanceof FileMetadata) {
                    fileNames.add(entry.getPathDisplay());
                }
            }

            logger.info("\n approvalWaitingOrderEntries  files - {} ", fileNames.toString());

            List<Metadata> approvedOrderEntries = dbxClientV2WithToken.files().listFolder("/orders/approved_orders").getEntries();
            for (Metadata entry : approvedOrderEntries) {
                if (entry instanceof FileMetadata) {
                    fileNames.add(entry.getPathDisplay());
                }
            }
            logger.info("\n approvedOrderEntries  files - {} ", fileNames.toString());
            List<Metadata> checkoutOrdersEntries = dbxClientV2WithToken.files().listFolder("/orders/checkout_orders").getEntries();
            for (Metadata entry : checkoutOrdersEntries) {
                if (entry instanceof FileMetadata) {
                    fileNames.add(entry.getPathDisplay());
                }
            }
            logger.info("\n checkoutOrdersEntries  files - {} ", fileNames.toString());
            List<Metadata> kitchenOrdersEntries = dbxClientV2WithToken.files().listFolder("/orders/kitchen_orders").getEntries();
            for (Metadata entry : kitchenOrdersEntries) {
                if (entry instanceof FileMetadata) {
                    fileNames.add(entry.getPathDisplay());
                }
            }
            logger.info("\n kitchenOrdersEntries  files - {} ", fileNames.toString());
            List<Metadata> declineOrdersEntries = dbxClientV2WithToken.files().listFolder("/orders/decline_orders").getEntries();
            for (Metadata entry : declineOrdersEntries) {
                if (entry instanceof FileMetadata) {
                    fileNames.add(entry.getPathDisplay());
                }
            }
            logger.info("\n declineOrdersEntries  files - {} ", fileNames.toString());

            if (!fileNames.isEmpty()) {
                logger.info("\n calling batchDeleteFile for the files size - {} ", fileNames.size());
                String jobID = dropboxService.batchDeleteFile(dbxClientV2WithToken, fileNames);
                String status = checkBatchDeletionStatus(dbxClientV2WithToken, jobID);

                logger.info("\n completed clearing the order   files - {} ", fileNames.toString());
                if (status.equalsIgnoreCase("COMPLETE")) {
                    EmailDetails emailDetails = new EmailDetails();
                    emailDetails.setRecipient("cafekubera2223@gmail.com");
                    emailDetails.setMsgBody("Hey! In\n  completed clearing the files  \nThanks");
                    emailDetails.setSubject("completed clearing the files for the date  - "+ currentDate );
                    sendSimpleMail(emailDetails);
                } else {
                    EmailDetails emailDetails = new EmailDetails();
                    emailDetails.setRecipient("cafekubera2223@gmail.com");
                    emailDetails.setMsgBody("Hey! In\n  error occurred clearing the files  \n\nThanks");
                    emailDetails.setSubject("n error occurred clearing the files for the date  - "+ currentDate);
                    sendSimpleMail(emailDetails);
                }
            } else {
                EmailDetails emailDetails = new EmailDetails();
                emailDetails.setRecipient("cafekubera2223@gmail.com");
                emailDetails.setMsgBody("Hey! In\n  no files found to clear s  \n\nThanks");
                emailDetails.setSubject("no files found to clear for the date  - "+ currentDate);
                sendSimpleMail(emailDetails);
            }
        }
        catch(Exception e)
        {
            logger.info("\n error occurred clearing the files - {} ",  e.getMessage());
            EmailDetails emailDetails = new EmailDetails();
            emailDetails.setRecipient("cafekubera2223@gmail.com");
            emailDetails.setMsgBody("Hey! In\n  error occurred clearing the files  \n\nThanks");
            emailDetails.setSubject("n error occurred clearing the files for the date  - "+ currentDate);
            sendSimpleMail(emailDetails);
        }
    }

    public String checkBatchDeletionStatus(DbxClientV2 client, String jobId) throws Exception {
     //   Thread.sleep(300000);
       String status =  dropboxService.deleteBatchCheck(client, jobId);
        logger.info("\n deleteBatchCheck status - {} ",  status);
        return  status;
    }



    @Async
    public void sendEntireOrderBackupDaily() {
        try{
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
            String currentDate = dateFormat.format(DateUtils.addDays(new Date(), -1));
            DbxClientV2 dbxClientV2WithToken = dropboxService.dbxClientV2WithToken();
            File zipFIle = ckAccountDropboxService.downloadZipFile(dbxClientV2WithToken, "/orders",
                    currentDate+"_orders.zip");
            EmailDetails emailDetails = new EmailDetails();
            emailDetails.setRecipient("cafekubera2223@gmail.com");
            emailDetails.setMsgBody("Hey! In\n this zip file contains the backup for the " +currentDate+"  \n\nThanks");
            emailDetails.setSubject("complete backup for the " + currentDate+"  \n\nThanks" );
            Attachment attachment = new Attachment();
            attachment.setFileContent(zipFIle);
            attachment.setFileName(currentDate+"_orders.zip");
            emailDetails.setAttachment(attachment);
            sendMailWithFileAttachment(emailDetails);
        } catch (Exception e)
        {
            EmailDetails emailDetails = new EmailDetails();
            emailDetails.setRecipient("cafekubera2223@gmail.com");
            emailDetails.setMsgBody("Hey! In\n error occurred while sending zip file \n\nThanks");
            emailDetails.setSubject("error occurred while sending orders backup  zip file - " );
            sendSimpleMail(emailDetails);
        }

    }
    @Async
    public void consolidatePaidOrderAndTriggerMail(EmailDetails details, String currentDate) {
        logger.info("\n version v1 - consolidatePaidOrderAndTriggerMail for file - {} ", details.getAttachment().getFileName());
        logger.info("\n consolidatePaidOrderAndTriggerMail for file - {} ", details.getAttachment().getFileName());
        PaidOrderDto response = convertCsvToDto(details);
        if (response != null) {

            boolean isConsolidated = consolidateOrders(details, response, currentDate);
            if (isConsolidated) {
                sendMailWithCsvAttachment(details);
                addOrderFileInList(details, currentDate);
            }
        }
        try {
            logger.info("\n deleting the saved file for - {} ", details.getAttachment().getFileName());
            Files.deleteIfExists(Paths.get("consolidated.xlsx"));
            Files.deleteIfExists(Paths.get("consolidatedUpload.xlsx"));
            Files.deleteIfExists(Paths.get("completedIndexFile.csv"));
        } catch (Exception e) {
            logger.info("\n unable to delte the consolidated files");
        }

    }

    public void consolidatePaidOrderAndTriggerMailAutomatic(EmailDetails details, String currentDate) {
        logger.info("\n consolidatePaidOrderAndTriggerMail for file - {} ", details.getAttachment().getFileName());
        PaidOrderDto response = convertCsvToDto(details);
        if (response != null) {

            boolean isConsolidated = consolidateOrders(details, response, currentDate);
            if (isConsolidated) {
                sendMailWithCsvAttachment(details);
                addOrderFileInList(details, currentDate);
            }
        }
        try {
            logger.info("\n deleting the saved file for - {} ", details.getAttachment().getFileName());
            Files.deleteIfExists(Paths.get("consolidated.xlsx"));
            Files.deleteIfExists(Paths.get("consolidatedUpload.xlsx"));
            Files.deleteIfExists(Paths.get("completedIndexFile.csv"));
        } catch (Exception e) {
            logger.info("\n unable to delte the consolidated files");
        }

    }


    @Async
    public void consolidatePaidOrderAutomatic() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        String currentDate = dateFormat.format(DateUtils.addDays(new Date(), -1));

        String filePath = "/consolidate/" + currentDate;
        DbxClientV2 dbxClientV2WithToken = dropboxService.dbxClientV2WithToken();
        DbxClientV2 accountClientV2WithToken = ckAccountDropboxService.dbxClientV2WithToken();
        try {
            List<Metadata> entries = dbxClientV2WithToken.files().listFolder("/orders/paid_orders").getEntries();
            File consolidatedFileNamesFile = ckAccountDropboxService.downloadFile(accountClientV2WithToken,
                    filePath + "/" + "completedIndexFile.csv", "completedIndexFile.csv");
            List<String> paidOrderFileNames = new ArrayList<>();
            List<String> consolidatedFileNames = readCompletedIndexFileCsvFile(consolidatedFileNamesFile);
            List<File> downloadedFiles = new ArrayList<>();
            for (Metadata entry : entries) {
                if (entry instanceof FileMetadata) {
                    paidOrderFileNames.add(entry.getName());
                }
            }
            List<String> skippedConsolidatedPaidFileNames = paidOrderFileNames.stream()
                    .filter(item -> !consolidatedFileNames.contains(item))
                    .collect(Collectors.toList());
            if (!skippedConsolidatedPaidFileNames.isEmpty()) {
                //download the files and convert to the email details object
                skippedConsolidatedPaidFileNames.forEach(fileName -> {
                    try {
                        File downloadedFile = dropboxService.downloadFile(dbxClientV2WithToken,
                                "/orders/paid_orders" + "/" + fileName, fileName);
                        String data = convretFileToString(downloadedFile);
                        EmailDetails emailDetails = new EmailDetails();
                        emailDetails.setRecipient("cafekubera2223@gmail.com");
                        emailDetails.setMsgBody("Hey! In\nThis is a message from the cafe kubera order payment completed \n\nThanks");
                        emailDetails.setSubject("skipped file details for the orders - " + downloadedFile.getName());
                        Attachment attachment = new Attachment();
                        attachment.setContent(data);
                        attachment.setFileName(downloadedFile.getName());
                        emailDetails.setAttachment(attachment);
                        consolidatePaidOrderAndTriggerMailAutomatic(emailDetails, currentDate);
                        Files.deleteIfExists(Paths.get(downloadedFile.getName()));

                    } catch (Exception e) {
                        EmailDetails emailDetails = new EmailDetails();
                        emailDetails.setRecipient("cafekubera2223@gmail.com");
                        emailDetails.setMsgBody("Hey! In\n error occurred while running paid order automate job \n\nThanks");
                        emailDetails.setSubject("error occurred while running paid order automate job for file - " +  fileName);
                        sendSimpleMail(emailDetails);
                    }
                });

            } else {
                EmailDetails emailDetails = new EmailDetails();
                emailDetails.setRecipient("cafekubera2223@gmail.com");
                emailDetails.setMsgBody("Hey! In\n All files processed successfully  \n\nThanks");
                emailDetails.setSubject("All files processed successfully for paid orders (automatic)" );
                sendSimpleMail(emailDetails);
            }

        } catch (Exception e) {
            EmailDetails emailDetails = new EmailDetails();
            emailDetails.setRecipient("cafekubera2223@gmail.com");
            emailDetails.setMsgBody("Hey! In\n error occurred while running paid order automate job \n\nThanks");
            emailDetails.setSubject("error occurred while running paid order automate job" );
            sendSimpleMail(emailDetails);

            throw new RuntimeException(e);
        }
    }

    public List<String> readCompletedIndexFileCsvFile(File consolidatedFileNamesFile) {
        List<String> csvData = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new FileReader(consolidatedFileNamesFile))) {
            String[] line;

            while ((line = reader.readNext()) != null) {
                // Skip empty lines
                if (line.length > 0) {
                    if (line[0] != "") {
                        csvData.add(line[0]);
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return csvData;
    }


    public boolean addOrderFileInList(EmailDetails details, String currentDate) {
        try {
            DbxClientV2 dbxClientV2WithToken = ckAccountDropboxService.dbxClientV2WithToken();
            // Get the current date in a specific format (e.g., "yyyy-MM-dd")

            String filePath = "/consolidate/" + currentDate;
            // Check if the folder for the current date exists
            if (!ckAccountDropboxService.checkFileExists(filePath, "completedIndexFile.csv", dbxClientV2WithToken)) {
                //folder not exist
                logger.info("\n calling createFileAndProcess method for processing file {} ", details.getAttachment().getFileName());
                return createCompletedIndexFileFileAndProcess(dbxClientV2WithToken, filePath, details);
            } else {
                logger.info("\n calling getFileAndProcess method for processing file {} ", details.getAttachment().getFileName());
                return getCompletedIndexFileFileAndProcess(dbxClientV2WithToken, filePath, details);
            }
        } catch (Exception e) {
            // Handle IOException
            System.out.println(e.getMessage());
            //error occured while porcessing file send mail
            String subject = details.getSubject();
            details.setSubject("error while creating the index file - " + subject);
            sendSimpleMail(details);
            return false;
        }
    }
    public String sendMailWithFileAttachment(EmailDetails details) {
        MimeMessage mimeMessage
                = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper;

        try {

            // Setting multipart as true for attachments to
            // be send
            mimeMessageHelper
                    = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setFrom(sender);
            mimeMessageHelper.setTo(details.getRecipient());
            mimeMessageHelper.setText(details.getMsgBody());
            mimeMessageHelper.setSubject(
                    details.getSubject());

            // Adding the attachment


            mimeMessageHelper.addAttachment(
                    details.getAttachment().getFileName(), details.getAttachment().getFileContent());

            // Sending the mail
            javaMailSender.send(mimeMessage);
            logger.info("\n Mail sent Successfully for - {} ", details.getAttachment().getFileName());
            return "Mail sent Successfully";
        }

        // Catch block to handle MessagingException
        catch (MessagingException e) {
            logger.info("\n Error while sending mail for - {} ", details.getAttachment().getFileName());
            // Display message when exception occurred
            return "Error while sending mail!!!";
        }
    }
    public String sendMailWithCsvAttachment(EmailDetails details) {
        // Creating a mime message
        MimeMessage mimeMessage
                = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper;

        try {

            // Setting multipart as true for attachments to
            // be send
            mimeMessageHelper
                    = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setFrom(sender);
            mimeMessageHelper.setTo(details.getRecipient());
            mimeMessageHelper.setText(details.getMsgBody());
            mimeMessageHelper.setSubject(
                    details.getSubject());

            // Adding the attachment
            ByteArrayResource file = new ByteArrayResource(details.getAttachment().getContent().getBytes());

            mimeMessageHelper.addAttachment(
                    details.getAttachment().getFileName(), file);

            // Sending the mail
            javaMailSender.send(mimeMessage);
            logger.info("\n Mail sent Successfully for - {} ", details.getAttachment().getFileName());
            logger.info("\n Mail sent Successfully to - {} ", details.getRecipient());
            return "Mail sent Successfully";
        }

        // Catch block to handle MessagingException
        catch (MessagingException e) {
            logger.info("\n Error while sending mail for - {} ", details.getAttachment().getFileName());
            // Display message when exception occurred
            return "Error while sending mail!!!";
        }
    }

    public PaidOrderDto convertCsvToDto(EmailDetails details) {
        PaidOrderDto response = null;
        try {
            response = cSVReaderService.getDataForField(details.getAttachment().getContent());
        } catch (Exception e) {
            logger.info("unable to process the file ", details.getAttachment().getFileName());
        }
        return response;
    }

    public boolean consolidateOrders(EmailDetails details, PaidOrderDto order, String currentDate) {
        try {
            DbxClientV2 dbxClientV2WithToken = ckAccountDropboxService.dbxClientV2WithToken();
            // Get the current date in a specific format (e.g., "yyyy-MM-dd")
            String filePath = "/consolidate/" + currentDate;
            // Check if the folder for the current date exists
            if (!ckAccountDropboxService.folderExists(dbxClientV2WithToken, filePath)) {
                //folder not exist
                if (ckAccountDropboxService.createFolder(filePath)) {
                    logger.info("\n calling createFileAndProcess method for processing file {} ", details.getAttachment().getFileName());
                    return createFileAndProcess(dbxClientV2WithToken, filePath, details, order);
                } else {
                    logger.info("\n error occurred while processing file {} ", details.getAttachment().getFileName());
                    throw new RuntimeException("error ocuured while creating the folder");
                }
            } else {
                logger.info("\n calling getFileAndProcess method for processing file {} ", details.getAttachment().getFileName());
                return getFileAndProcess(dbxClientV2WithToken, filePath, details, order);
                // if folder exist get the file

            }
        } catch (Exception e) {
            // Handle IOException

            System.out.println(e.getMessage());
            //error occured while porcessing file send mail
            String subject = details.getSubject();
            details.setSubject("error - " + subject);
            sendSimpleMail(details);
            return false;
        }
    }


    public boolean createCompletedIndexFileFileAndProcess(DbxClientV2 dbxClientV2WithToken, String filePath,
                                                          EmailDetails details)
            throws Exception {

        String output = "completedIndexFile.csv";
        File newFile = Paths.get(output).toFile();
        convertStringToCsv(details.getAttachment().getFileName(), output);
        boolean status = ckAccountDropboxService.uploadFileToDropbox(newFile,
                dbxClientV2WithToken, filePath + "/" + output);
        if (status) {
            return status;
        } else {
            logger.info("/n error occurred while processing file {} ", details.getAttachment().getFileName());
            throw new RuntimeException("error occurred while uploading ");
        }

    }

    public String convretFileToString(File file) {
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(file);

            BufferedReader bufferedReader = new BufferedReader(fileReader);

            // Initialize a StringBuilder to build the CSV content as a string
            StringBuilder csvContent = new StringBuilder();
            String line;

            // Read each line of the CSV file and append it to the StringBuilder
            while ((line = bufferedReader.readLine()) != null) {
                csvContent.append(line).append("\n");
            }

            // Close the resources
            bufferedReader.close();
            fileReader.close();

            // Convert the StringBuilder to a String
            String csvString = csvContent.toString();

            // Print or use the CSV content as needed
            return csvString;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public boolean getCompletedIndexFileFileAndProcess(DbxClientV2 dbxClientV2WithToken, String filePath,
                                                       EmailDetails details)
            throws Exception {

        String output = "completedIndexFile.csv";
        File newFile = Paths.get(output).toFile();

        String fileName = "completedIndexFile.csv";
        filePath = filePath + "/" + fileName;
        File exsitingFile = ckAccountDropboxService.downloadFile(dbxClientV2WithToken, filePath, fileName);
        addNewLineToFile(exsitingFile, details.getAttachment().getFileName());
        //  convertStringToCsv(details.getAttachment().getFileName(), output );
        boolean status = ckAccountDropboxService.uploadFileToDropbox(exsitingFile,
                dbxClientV2WithToken, filePath);
        if (status) {
            return status;
        } else {
            logger.info("/n error occurred while processing file {} ", details.getAttachment().getFileName());
            throw new RuntimeException("error occurred while uploading ");
        }

    }

    public static void convertStringToCsv(String inputString, String outputFilePath) throws IOException {
        try (CSVWriter csvWriter = new CSVWriter(new FileWriter(outputFilePath))) {
            // Assuming the inputString contains CSV data
            String[] data = inputString.split(","); // Adjust this based on your actual data structure

            // Write the data to the CSV file
            csvWriter.writeNext(data);
        }
    }

    public void addNewLineToFile(File file, String newLine) throws IOException {
        try (FileWriter writer = new FileWriter(file, true)) {
            writer.write("\n");
            writer.write(newLine);

        }
    }

    public boolean createFileAndProcess(DbxClientV2 dbxClientV2WithToken, String filePath, EmailDetails details, PaidOrderDto order)
            throws Exception {
//
//        //String fileName = "consolidated-"+currentDate+".xlsx";
        String fileName = "consolidated.xlsx";
        String tempFilePath = "/template/" + fileName;
//         Resource companyDataResource = resourceLoader.getResource("classpath:templates/consolidated.xlsx");
       File exsitingFile = ckAccountDropboxService.downloadFile(dbxClientV2WithToken, tempFilePath, fileName);


        // after getting the file add the paid order details to the file
        FileOutputStream file = addOrdersToFile(exsitingFile, order);
        if (file != null) {
            String uploadFileName = "consolidated.xlsx";
            String fileNam = "consolidatedUpload.xlsx";
            File newFile = Paths.get(fileNam).toFile();
            boolean status = ckAccountDropboxService.uploadFileToDropbox(newFile,
                    dbxClientV2WithToken, filePath + "/" + uploadFileName);
            if (status) {
                return status;
            } else {
                logger.info("/n error occurred while processing file {} ", details.getAttachment().getFileName());
                throw new RuntimeException("error occurred while uploading ");
            }
        } else {
            logger.info("/n error occurred while processing file {} ", details.getAttachment().getFileName());
            throw new RuntimeException("error occurred while converting to xlsx  ");
        }
    }

    public boolean getFileAndProcess(DbxClientV2 dbxClientV2WithToken, String filePath, EmailDetails details, PaidOrderDto order)
            throws Exception {

//            String fileName = "consolidated-"+currentDate+".xlsx";

        String fileName = "consolidated.xlsx";
        filePath = filePath + "/" + fileName;
        File exsitingFile = ckAccountDropboxService.downloadFile(dbxClientV2WithToken, filePath, fileName);
        // after getting the file add the paid order details to the file
        FileOutputStream file = addOrdersToFile(exsitingFile, order);
        if (file != null) {
            String newFileName = "consolidatedUpload.xlsx";
            File newFile = Paths.get(newFileName).toFile();
            boolean status = ckAccountDropboxService.uploadFileToDropbox(newFile, dbxClientV2WithToken, filePath);
            if (status) {
                return status;
            } else {
                logger.info("/n error occurred while processing file {} ", details.getAttachment().getFileName());
                throw new RuntimeException("error occured while uploading ");
            }

        } else {
            logger.info("/n error occurred while processing file {} ", details.getAttachment().getFileName());
            throw new RuntimeException("error occurred while converting to xlsx  ");
        }
    }

    private FileOutputStream addOrdersToFile(File exsitingFile, PaidOrderDto order) {
        FileOutputStream fileOutputStream = null;
        try {
            Workbook workbook = new XSSFWorkbook(exsitingFile.getPath());
            appendToPaymentSheet(workbook.getSheet("payments"), order.getPayment());
            appendToOrdersSheet(workbook.getSheet("orders"), order.getOrders());
            appendToItemsSheet(workbook.getSheet("orderDetails"), order.getItems());

            // Save the changes
            fileOutputStream = new FileOutputStream("consolidatedUpload.xlsx");
            workbook.write(fileOutputStream);


        } catch (Exception e) {
            return null;
        }
        return fileOutputStream;

    }

    private void appendToPaymentSheet(Sheet paymentSheet, Payment payment) {
        // Find the last row or start from the first row if the sheet is empty
        int rowNum = paymentSheet.getLastRowNum() + 1;
        Row row = paymentSheet.createRow(rowNum);
        Cell cell = row.createCell(0);
        cell.setCellValue(payment.getPaid_amount());
        cell = row.createCell(1);
        cell.setCellValue(payment.getActual_amount());
        cell = row.createCell(2);
        cell.setCellValue(payment.getMode());
        cell = row.createCell(3);
        cell.setCellValue(payment.getPeriod());
    }

    private void appendToOrdersSheet(Sheet ordersSheet, List<Order> orders) {
        // Find the last row or start from the first row if the sheet is empty
        int rowNum = ordersSheet.getLastRowNum() + 1;

        // Append order details to the sheet
        for (Order order : orders) {
            Row row = ordersSheet.createRow(rowNum++);
            row.createCell(0).setCellValue(String.valueOf(order.getOrder_ref_id()));
            row.createCell(1).setCellValue(order.getTable_no().toString());
            row.createCell(2).setCellValue(order.getOrder_summary_amount().toString());
            row.createCell(3).setCellValue(order.getOrder_additional_service_amount().toString());
            row.createCell(4).setCellValue(order.getOrder_total_amount().toString());
            row.createCell(5).setCellValue(order.getId());
            row.createCell(7).setCellValue(order.getBillNo());
            // Add other order details as needed
        }
    }

    private void appendToItemsSheet(Sheet itemsSheet, List<OrderItem> items) {
        // Find the last row or start from the first row if the sheet is empty
        int rowNum = itemsSheet.getLastRowNum() + 1;

        // Append item details to the sheet
        for (OrderItem item : items) {
            Row row = itemsSheet.createRow(rowNum++);
            row.createCell(0).setCellValue(item.getOrder_ref_id());
            row.createCell(1).setCellValue(item.getItem_name());
            row.createCell(2).setCellValue(item.getItem_description());
            row.createCell(3).setCellValue(item.getItem_quantity());
            row.createCell(4).setCellValue(item.getItem_cost());
            // Add other item details as needed
        }
    }

}
