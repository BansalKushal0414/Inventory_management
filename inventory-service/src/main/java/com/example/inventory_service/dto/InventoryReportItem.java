package com.example.inventory_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class InventoryReportItem {
    private ProductDto product;
    private Integer quantity;
    private String location;

}
