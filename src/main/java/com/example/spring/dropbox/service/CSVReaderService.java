package com.example.spring.dropbox.service;


import com.example.spring.dropbox.pojo.Order;
import com.example.spring.dropbox.pojo.OrderItem;
import com.example.spring.dropbox.pojo.PaidOrderDto;
import com.example.spring.dropbox.pojo.Payment;
import com.opencsv.CSVReader;
import org.springframework.stereotype.Service;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class CSVReaderService {
    public PaidOrderDto getDataForField(String csvString) throws Exception {
        StringReader stringReader = new StringReader(csvString);
        Payment payment = new Payment();
        List<Order> orderList = new ArrayList<>();
        List<OrderItem> orderItemList = new ArrayList<>();
        try (CSVReader reader = new CSVReader(stringReader)) {
            List<String[]> lines = reader.readAll();

            // Find the header row index based on the field name
            int paymentHeaderRowIndex = findHeaderRowIndex(lines, "paid_amount");
            int ordersHeaderRowIndex = findHeaderRowIndex(lines, "order_summary_amount");
            int orderItemsHeaderRowIndex = findHeaderRowIndex(lines, "item_quantity");

            // Ensure startIndex and endIndex are within valid bounds
            int paymentStartIndex = paymentHeaderRowIndex + 1;
            int paymentEndIndex = ordersHeaderRowIndex;

            int orderStartIndex = ordersHeaderRowIndex + 1;
            int orderEndIndex = orderItemsHeaderRowIndex ;

            int orderItemsStartIndex = orderItemsHeaderRowIndex + 1;
            int orderItemsEndIndex = lines.size();

            // Extract details within the specified range and store them in objects

            lines.subList(paymentStartIndex, paymentEndIndex).forEach(line -> {
                payment.setPaid_amount(line[0]);
                payment.setActual_amount(line[1]);
                payment.setMode(line[2]);
                payment.setPeriod(line[3]);
            });



            lines.subList(orderStartIndex, orderEndIndex).forEach(line -> {
                Order order = new Order(line[0],line[1], line[2], line[3], line[4], line[5], line[6], null);
                if(line.length>7)
                {
                    order.setBillNo(line[6]);
                }
                orderList.add(order);
            });


            lines.subList(orderItemsStartIndex, orderItemsEndIndex).forEach(line -> {
                OrderItem orderItem = new OrderItem(Long.parseLong(line[0]),line[1], line[2], Integer.parseInt(line[3]),
                        Double.parseDouble(line[4]),null);
                orderItemList.add(orderItem);
            });
        }
        return new PaidOrderDto(payment,orderList, orderItemList);
    }
    private int findHeaderRowIndex(List<String[]> lines, String fieldName) {
        for (int i = 0; i < lines.size(); i++) {
            String[] headers = lines.get(i);
            for (int j = 0; j < headers.length; j++) {
                if (fieldName.equals(headers[j])) {
                    return i; // Return the row index
                }
            }
        }

        throw new RuntimeException("Field not found in any header row");
    }
}