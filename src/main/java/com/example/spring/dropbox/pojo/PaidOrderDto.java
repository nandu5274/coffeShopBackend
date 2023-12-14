package com.example.spring.dropbox.pojo;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PaidOrderDto {
    private Payment payment;
    private List<Order> orders;
    private List<OrderItem> items;
}
