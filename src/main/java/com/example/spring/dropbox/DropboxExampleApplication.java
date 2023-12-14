package com.example.spring.dropbox;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.http.StandardHttpRequestor;
import com.dropbox.core.v2.DbxClientV2;
import com.example.spring.dropbox.config.WebConfig;
import com.example.spring.dropbox.config.WebSocketConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ComponentScan(basePackages = {
		"com.example.spring.dropbox.controller",
		"com.example.spring.dropbox.service"
})
@Import({WebConfig.class, WebSocketConfig.class})
@EnableAsync
public class DropboxExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(DropboxExampleApplication.class, args);
	}

	@Bean("dropboxClient")
	public DbxClientV2 dropboxClient() throws DbxException {
		String ACCESS_TOKEN = "7M79wfVVx2EAAAAAAAAAD0FVaMlP_FIJ3LPuf1JYUvg";
		DbxRequestConfig config = new DbxRequestConfig("dropbox/java-tutorial");
		DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);
		return client;
	}

	@Bean("dropboxClientAccount")
	public DbxClientV2 dropboxClientAccount2() throws DbxException {
		String ACCESS_TOKEN = "XSWSvqrmQLAAAAAAAAAAAb8KhaxTtK_jkMfKY7z3bQEf27EQLtvWjaTS6GggiY30";
		DbxRequestConfig config = new DbxRequestConfig("dropbox/java-tutorial");
		DbxClientV2 client = new DbxClientV2(config, ACCESS_TOKEN);
		return client;
	}
}
