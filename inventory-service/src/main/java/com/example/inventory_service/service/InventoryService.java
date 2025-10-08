package com.example.inventory_service.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.inventory_service.dto.InventoryReportItem;
import com.example.inventory_service.dto.ProductDto;
import com.example.inventory_service.model.Inventory;
import com.example.inventory_service.repository.InventoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final RestTemplate restTemplate;

    public Optional<Inventory> getStock(Long productId) {
        return inventoryRepository.findByProductId(productId);
    }

    public List<InventoryReportItem> getFullInventoryReport() {
        List<Inventory> allInventory = inventoryRepository.findAll();

        return allInventory.stream().map(inventoryItem -> {
            // For each item in our inventory, call the product-service to get details
            String productServiceUrl = "http://PRODUCT-SERVICE/api/products/" + inventoryItem.getProductId();
            ProductDto productDto = restTemplate.getForObject(productServiceUrl, ProductDto.class);

            // Combine the data and return a new report item
            return new InventoryReportItem(productDto, inventoryItem.getQuantity(), inventoryItem.getLocation());
        }).collect(Collectors.toList());
    }

    public Inventory addStock(Long productId, Integer quantityToAdd) {
        // First, verify the product exists by calling the product-service
        try {
            String productServiceUrl = "http://PRODUCT-SERVICE/api/products/" + productId;
            ResponseEntity<Object> response = restTemplate.getForEntity(productServiceUrl, Object.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new IllegalArgumentException("Product not found with ID: " + productId);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Product not found or Product Service is down. ID: " + productId);
        }

        // Find existing inventory or create a new one
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElse(new Inventory(null, productId, 0, "MAIN_WAREHOUSE", null));

        inventory.setQuantity(inventory.getQuantity() + quantityToAdd);
        return inventoryRepository.save(inventory);
    }

    public Inventory removeStock(Long productId, Integer quantityToRemove) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(
                        () -> new IllegalArgumentException("No inventory record found for product ID: " + productId));

        if (inventory.getQuantity() < quantityToRemove) {
            throw new IllegalArgumentException("Insufficient stock for product ID: " + productId);
        }

        inventory.setQuantity(inventory.getQuantity() - quantityToRemove);
        return inventoryRepository.save(inventory);
    }

}
