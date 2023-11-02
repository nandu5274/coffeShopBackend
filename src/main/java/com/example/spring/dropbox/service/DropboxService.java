package com.example.spring.dropbox.service;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.UploadErrorException;
import com.example.spring.dropbox.model.DropboxItem;
import com.example.spring.dropbox.util.DropboxAction;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static com.dropbox.core.v2.files.WriteMode.OVERWRITE;

/**
 * @author kyeongjinkim
 * @version 1.0.0
 * @since 2017-05-23
 */
@Service
public class DropboxService {

	private static final Logger logger = LoggerFactory.getLogger(DropboxService.class);

	@Autowired
	DbxClientV2 dropboxClient;

	private final RestTemplate restTemplate;
	@Autowired
	public DropboxService(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public void uploadFile(MultipartFile file, String filePath) throws IOException, DbxException {

		ByteArrayInputStream inputStream = new ByteArrayInputStream(file.getBytes());
		Metadata uploadMetaData = dropboxClient.files().uploadBuilder(filePath).uploadAndFinish(inputStream);
		logger.info("upload meta data =====> {}", uploadMetaData.toString());
		inputStream.close();
	}

	public List<Map<String, Object>> getFileList(String target) throws IOException, DbxException {

		List<Metadata> entries = dbxClientV2WithToken().files().listFolder(target).getEntries();
		List<Map<String, Object>> result = new ArrayList<>();

		for (Metadata entry : entries ) {
			if (entry instanceof FileMetadata) {
				logger.info("{} is file", entry.getName());
			}
			String metaDataString = entry.toString();
			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> map = new HashMap<>();
			map = mapper.readValue(metaDataString, new TypeReference<Map<String, Object>>(){});
			result.add(map);
//			if ("file".equals(map.get(".tag"))) {
//				GetTemporaryLinkResult temp = dropboxClient.files().getTemporaryLink(entry.getPathLower());
//				logger.info("thumbnail ==> {}", temp);
//			}
		}

		return result;
	}

	public void backupOrderFiles() throws Exception {
		DbxClientV2 dbxClientV2WithToken = dbxClientV2WithToken();
		List<Metadata> entries = dbxClientV2WithToken.files().listFolder("/orders/current_orders/orders").getEntries();
		List<String> orderFileNames = new ArrayList<>();
		List<File> downloadedFiles =  new ArrayList<>();
		//download the master file
		File mainOrderFile = downloadFile(dbxClientV2WithToken,
				"/orders/sum_orders/orders.csv", "orders.csv");
		System.out.println("mainOrderFile - "+ mainOrderFile);
		for (Metadata entry : entries ) {
			if (entry instanceof FileMetadata) {
				orderFileNames.add(entry.getName());
				downloadedFiles.add(downloadFile(dbxClientV2WithToken, entry.getPathDisplay(), entry.getName()));
			}

		}
		for (File file : downloadedFiles) {
			String data = convretFileToString(file);
			String headerRemovedContent = removeHeader(data);
			addNewLineToFile(mainOrderFile, headerRemovedContent);
		}
		uploadFileToDropbox(mainOrderFile,dbxClientV2WithToken, "/orders/sum_orders/orders.csv");
		System.out.println("uploadFileToDropbox - ");

		Files.deleteIfExists(Paths.get("orders.csv"));
		orderFileNames.forEach(fileName ->{
			try {
				Files.deleteIfExists(Paths.get(fileName));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		System.out.println("delete Temp files ");
		/// download and read the file data
			System.out.println("orderFileNames - "+ orderFileNames.toString());
	}


	public void backupOrderItemsFiles() throws Exception {
		DbxClientV2 dbxClientV2WithToken = dbxClientV2WithToken();
		List<Metadata> entries = dbxClientV2WithToken.files().listFolder("/orders/current_orders/order_items").getEntries();
		List<String> orderFileNames = new ArrayList<>();
		List<File> downloadedFiles =  new ArrayList<>();
		//download the master file
		File mainOrderFile = downloadFile(dbxClientV2WithToken,
				"/orders/sum_orders/order_items.csv", "order_items.csv");
		System.out.println("order_items mainOrderFile - "+ mainOrderFile);
		for (Metadata entry : entries ) {
			if (entry instanceof FileMetadata) {
				orderFileNames.add(entry.getName());
				downloadedFiles.add(downloadFile(dbxClientV2WithToken, entry.getPathDisplay(), entry.getName()));
			}

		}
		for (File file : downloadedFiles) {
			String data = convretFileToString(file);
			String headerRemovedContent = removeHeader(data);
			addNewLineToFile(mainOrderFile, headerRemovedContent);
		}
		uploadFileToDropbox(mainOrderFile,dbxClientV2WithToken, "/orders/sum_orders/order_items.csv");
		System.out.println("uploadFileToDropbox order_items- ");

		Files.deleteIfExists(Paths.get("order_items.csv"));
		orderFileNames.forEach(fileName ->{
			try {
				Files.deleteIfExists(Paths.get(fileName));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		System.out.println("delete Temp files order_items ");
		/// download and read the file data
		System.out.println("orderFileNames order_items - "+ orderFileNames.toString());
	}





	public List<DropboxItem> getDropboxItems(String path) throws IOException, DbxException {
		List<Metadata> entries = dropboxClient.files().listFolder(path).getEntries();
		List<DropboxItem> result = new ArrayList<>();
		entries.stream().forEach(entry -> {
			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> map = new HashMap<>();
			try {
				map = mapper.readValue(entry.toString(), new TypeReference<Map<String, Object>>(){});
			} catch (IOException e) {
				e.printStackTrace();
			}

			DropboxItem item = new DropboxItem();
			item.setId(map.get("id").toString());
			item.setName(map.get("name").toString());
			item.setPath(map.get("path_lower").toString());
			result.add(item);
		});
		return result;
	}

	public void downloadFile(HttpServletResponse response, DropboxAction.Download download) throws IOException, DbxException {
		response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
		response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(download.getFileName(), "UTF-8")+"\";");
		response.setHeader("Content-Transfer-Encoding", "binary");

		ServletOutputStream outputStream = response.getOutputStream();
		dropboxClient.files().downloadBuilder(download.getFilePath()).download(outputStream);

		response.getOutputStream().flush();
		response.getOutputStream().close();
	}

	public void deleteFile(DropboxAction.Delete delete) throws DbxException {
		dropboxClient.files().delete(delete.getFilePath());
	}

	public DbxClientV2 dbxClientV2WithToken()
	{
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

	public void addNewLineToFile(File file, String newLine) throws IOException {
		try (FileWriter writer = new FileWriter(file, true)) {
			//writer.write("\n");
			writer.write(newLine);

		}
	}

	public void uploadFileToDropbox(File file, DbxClientV2 client, String dropboxFilePath) throws IOException, UploadErrorException, DbxException {
		try (InputStream in = new FileInputStream(file)) {
			client.files().uploadBuilder(dropboxFilePath)
					.withMode(OVERWRITE)
					.uploadAndFinish(in);
		}
	}

	public static String removeHeader(String csvContent) {
		String[] lines = csvContent.split("\n");
		if (lines.length <= 1) {
			return csvContent; // No header found
		}

		StringBuilder modifiedContent = new StringBuilder();
		for (int i = 1; i < lines.length; i++) {
			modifiedContent.append(lines[i]).append("\n");
		}
		return modifiedContent.toString();
	}

	public String convretFileToString(File file)
	{
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



}
