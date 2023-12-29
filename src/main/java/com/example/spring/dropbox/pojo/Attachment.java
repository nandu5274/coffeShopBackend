package com.example.spring.dropbox.pojo;

import lombok.*;

import java.io.File;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Attachment {

    private String fileName;
    private String content;
    private File fileContent;
}
