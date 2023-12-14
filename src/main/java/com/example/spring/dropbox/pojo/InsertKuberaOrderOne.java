package com.example.spring.dropbox.pojo;

import java.util.ArrayList;

public class InsertKuberaOrderOne{
    public int id;
    public String order_status;
    public long order_ref_id;
    public double order_summary_amount;
    public double order_additional_service_amount;
    public double order_total_amount;
    public long table_no;
    public ArrayList<OrderItem> order_items;
    public String __typename;
}
