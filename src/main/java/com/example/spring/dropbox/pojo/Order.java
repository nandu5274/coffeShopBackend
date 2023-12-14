package com.example.spring.dropbox.pojo;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Order {


    private String order_ref_id;

    private String table_no;
    private String  order_summary_amount;
    private String order_additional_service_amount;
    private String order_total_amount;
    private String id;
    private String isExpanded;
    private String billNo;
}
