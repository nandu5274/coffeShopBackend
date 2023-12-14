package com.example.spring.dropbox.pojo;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Payment {
    private String 	paid_amount;
    private String actual_amount;
    private String mode;
    private String period;
}
