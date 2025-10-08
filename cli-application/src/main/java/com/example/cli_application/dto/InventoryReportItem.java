package com.example.cli_application.dto;

import lombok.Data;

@Data
public class InventoryReportItem {
    @Data
    public static class Product {
        private String sku;
        private String name;
    }

    private Product product;
    private Integer quantity;
    private String location;
}
