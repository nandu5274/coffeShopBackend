package com.example.spring.dropbox.service;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.dropbox.core.v2.files.WriteMode.OVERWRITE;

@Service
public class CkAccountDropboxService {

    private static final Logger logger = LoggerFactory.getLogger(DropboxService.class);

    @Autowired
    @Qualifier("dropboxClientAccount")
    DbxClientV2 dropboxClient;

    private final RestTemplate restTemplate;

    @Autowired
    public CkAccountDropboxService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<Map<String, Object>> getFileList(String target) throws IOException, DbxException {

        List<Metadata> entries = dbxClientV2WithToken().files().listFolder(target).getEntries();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Metadata entry : entries) {
            if (entry instanceof FileMetadata) {
                logger.info("{} is file", entry.getName());
            }
            String metaDataString = entry.toString();
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> map = new HashMap<>();
            map = mapper.readValue(metaDataString, new TypeReference<Map<String, Object>>() {
            });
            result.add(map);
//			if ("file".equals(map.get(".tag"))) {
//				GetTemporaryLinkResult temp = dropboxClient.files().getTemporaryLink(entry.getPathLower());
//				logger.info("thumbnail ==> {}", temp);
//			}
        }

        return result;
    }

    public DbxClientV2 dbxClientV2WithToken() {
        String ACCESS_TOKEN = generteTokenForDropBox();

        DbxRequestConfig config = new DbxRequestConfig("dropbox/cafe-kubera");
        DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);
        return client;
    }

    public String generteTokenForDropBox() {
        RestTemplate restTemplate = new RestTemplate();

        String url = "https://api.dropbox.com/oauth2/token";

        // Create a MultiValueMap to hold the form parameters (variables)
        MultiValueMap<String, String> formParameters = new LinkedMultiValueMap<>();
        formParameters.add("grant_type", "refresh_token");
        formParameters.add("refresh_token", "XSWSvqrmQLAAAAAAAAAAAb8KhaxTtK_jkMfKY7z3bQEf27EQLtvWjaTS6GggiY30");
        formParameters.add("client_id", "eymirkk65sqbnuj");
        formParameters.add("client_secret", "jygho6ca8ul7416");


        // Set the request headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Create a RequestEntity with the form parameters and headers
        RequestEntity<MultiValueMap<String, String>> requestEntity = new RequestEntity<>(formParameters, headers, HttpMethod.POST, URI.create(url));

        // Send the POST request with the request entity
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            // Parse the response body as JSON
            String responseBody = responseEntity.getBody();

            // Use a JSON parsing library like Jackson to extract the access token
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                JsonNode jsonNode = objectMapper.readTree(responseBody);
                String accessToken = jsonNode.get("access_token").asText();
                return accessToken;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public File downloadFile(DbxClientV2 client, String dropboxFilePath, String dropboxFileName) throws DbxException, IOException {
        try (OutputStream outputStream = new FileOutputStream(dropboxFileName)) {
            client.files().downloadBuilder(dropboxFilePath).download(outputStream);
        }

        System.out.println("downloadFile_sub files - "+ dropboxFileName);
        return new File(dropboxFileName);
    }
    public File downloadZipFile(DbxClientV2 client, String dropboxFilePath, String dropboxFileName) throws DbxException, IOException {
        try (OutputStream outputStream = new FileOutputStream(dropboxFileName)) {
            client.files().downloadZip(dropboxFilePath).download(outputStream);
        }

        System.out.println("downloadFile_sub files - "+ dropboxFileName);
        return new File(dropboxFileName);
    }

    public boolean folderExists(DbxClientV2 client, String folderPath) {
        try {
            ListFolderResult result = client.files().listFolder(folderPath);

            // Check if the folder is not empty
            if (result.getEntries().iterator().hasNext()) {
                return true; // Folder exists and is not empty
            }

            return false; // Folder exists but is empty
        } catch (Exception e) {
            // Folder does not exist or other error occurred
            return false;
        }
    }


    public boolean checkFileExists(String folderPath, String fileName, DbxClientV2 dbxClientV2) {
        try {
            List<Metadata> fileList = listFiles(folderPath, dbxClientV2);

            for (Metadata metadata : fileList) {
                if (metadata.getName().equals(fileName)) {
                    return true; // File found
                }
            }

            return false; // File not found
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Metadata> listFiles(String folderPath, DbxClientV2 dbxClientV2) {
        List<Metadata> fileList = new ArrayList<>();

        try {
            ListFolderResult result = dbxClientV2.files().listFolder(folderPath);

            while (true) {
                fileList.addAll(result.getEntries());

                if (!result.getHasMore()) {
                    break;
                }

                result = dbxClientV2.files().listFolderContinue(result.getCursor());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileList;
    }


    public boolean createFolder(String folderPath) throws DbxException {
        try {
            dbxClientV2WithToken().files().createFolderV2(folderPath);
            // Successfully created the folder
            return true;
        } catch (CreateFolderErrorException e) {
            // If the folder already exists, return true
            if ( e.errorValue.isPath() &&
                    e.errorValue.getPathValue().isConflict()) {
                return true; // Folder already exists
            } else {
                // Other error occurred
                return false;
            }
        } catch (Exception e) {
            // Other exceptions (e.g., network error)
            return false;
        }
    }

    public boolean uploadFileToDropbox(File file, DbxClientV2 client, String dropboxFilePath) throws IOException, UploadErrorException, DbxException {
        try (InputStream in = new FileInputStream(file)) {
            client.files().uploadBuilder(dropboxFilePath)
                    .withMode(OVERWRITE)
                    .uploadAndFinish(in);
            System.out.println("upload File To Dropbox  - "+ dropboxFilePath);
            return true;
        }catch(Exception e)
        {
            return false;
        }
    }



}
