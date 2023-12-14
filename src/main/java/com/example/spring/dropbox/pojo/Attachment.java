package com.example.spring.dropbox.pojo;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Attachment {

    private String fileName;
    private String content;
}
