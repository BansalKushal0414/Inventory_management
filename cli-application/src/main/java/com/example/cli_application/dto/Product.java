package com.example.cli_application.dto;

import java.math.BigDecimal;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Product {
    private Long id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal costPrice;
    private BigDecimal sellingPrice;

    public Product(String sku, String name, String description, BigDecimal costPrice, BigDecimal sellingPrice) {
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.costPrice = costPrice;
        this.sellingPrice = sellingPrice;
    }

}
