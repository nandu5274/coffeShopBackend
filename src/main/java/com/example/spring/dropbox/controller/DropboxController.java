package com.example.spring.dropbox.controller;

import com.dropbox.core.v2.DbxClientV2;
import com.example.spring.dropbox.config.MyWebSocketHandler;
import com.example.spring.dropbox.model.DropboxItem;
import com.example.spring.dropbox.pojo.OrderRequestDto;
import com.example.spring.dropbox.service.CkAccountDropboxService;
import com.example.spring.dropbox.service.DropboxService;
import com.example.spring.dropbox.util.DropboxAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author kyeongjinkim
 * @version 1.0.0
 * @since 2017-05-23
 */
@RestController
@RequestMapping("dropbox")
@CrossOrigin
public class DropboxController {

	private static final Logger logger = LoggerFactory.getLogger(DropboxController.class);
	@Autowired
	private MyWebSocketHandler myWebSocketHandler;
	@Autowired
	DropboxService dropboxService;

	@Autowired
	CkAccountDropboxService ckAccountDropboxService;

	@Autowired
	DbxClientV2 dropboxClient;

	@PostMapping("/upload")
	public String handleFileUplad(@RequestParam("file") MultipartFile file, @RequestParam("filePath") String filePath) throws Exception {
		dropboxService.uploadFile(file, filePath);
		return "You successfully uploaded " + filePath + "!!";
	}

	@GetMapping("/list")
	public List<Map<String, Object>> index(@RequestParam(value = "target", required = false, defaultValue = "") String target) throws Exception {
		return dropboxService.getFileList(target);
	}
	@GetMapping("/list2")
	public List<Map<String, Object>> index2(@RequestParam(value = "target", required = false, defaultValue = "") String target) throws Exception {
		return ckAccountDropboxService.getFileList(target);
	}
	@GetMapping("/browse")
	public Map<String, Object> brwose(@RequestParam(value = "target", required = false, defaultValue = "") String target) throws Exception {
		Map<String, Object> data = new HashMap<>();
		data.put("data", dropboxService.getDropboxItems(target));

		return data;
	}

	@GetMapping("/download")
	public void downloadFile(HttpServletResponse response, @RequestBody DropboxAction.Download download) throws Exception {
		dropboxService.downloadFile(response, download);
	}

	@DeleteMapping("/delete")
	public ResponseEntity deleteFile(@RequestBody DropboxAction.Delete delete, BindingResult result) throws Exception {
		dropboxService.deleteFile(delete);

		DropboxAction.Response response = new DropboxAction.Response(200, "success");
		return new ResponseEntity<>(response, HttpStatus.OK);
	}

	@GetMapping("/backup/orders")
	public List<Map<String, Object>> backupOrders() throws Exception {
		 dropboxService.backupOrderFiles();
		return null;
	}

	@GetMapping("/backup/orderitems")
	public List<Map<String, Object>> backupOrderItems() throws Exception {
		dropboxService.backupOrderItemsFiles();
		return null;
	}

	@GetMapping("/backup/approvedorders")
	public List<Map<String, Object>> backupCurrentOrders() throws Exception {
		dropboxService.backupApprovedOrders();
		return null;
	}


	@GetMapping("/heath")
	public String heath() throws Exception {
		return "OK";
	}


	@GetMapping("/broadcast")
	public void broadcast(@RequestParam(value = "message", required = false, defaultValue = "") String message) throws Exception {
		myWebSocketHandler.broadcastMessage(message);
	}

	@PostMapping("/broadcastobject")
	public String broadcastObject(@RequestBody OrderRequestDto request) throws Exception {
		System.out.println("request - " + request);
		myWebSocketHandler.broadcastObject(request);
		return "";
	}


}
