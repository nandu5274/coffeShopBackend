package com.example.spring.dropbox.pojo;

import lombok.*;
import lombok.Data;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EmailDetails {

    // Class data members
    private String recipient;
    private String msgBody;
    private String subject;
    private Attachment attachment;
}