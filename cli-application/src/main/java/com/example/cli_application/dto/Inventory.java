package com.example.cli_application.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Inventory {
    private Long id;
    private Long productId;
    private Integer quantity;
    private String location;
    private LocalDateTime lastUpdated;

}
