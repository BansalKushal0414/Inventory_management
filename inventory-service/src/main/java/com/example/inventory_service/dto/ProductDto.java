package com.example.inventory_service.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductDto {
    private String sku;
    private String name;
    private BigDecimal sellingPrice;

}
