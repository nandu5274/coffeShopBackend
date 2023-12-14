package com.example.spring.dropbox.pojo;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class OrderItem{
    public long order_ref_id;
    public String item_name;
    public String item_description;
    public int item_quantity;
    public double item_cost;
    public String __typename;
}
