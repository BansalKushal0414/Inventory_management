package com.example.inventory_service.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.inventory_service.dto.InventoryReportItem;
import com.example.inventory_service.dto.StockUpdateRequest;
import com.example.inventory_service.model.Inventory;
import com.example.inventory_service.service.InventoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @GetMapping("/product/{productId}")
    public ResponseEntity<Inventory> getStock(@PathVariable Long productId) {
        return inventoryService.getStock(productId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // In InventoryController.java

    // ... inside the public class InventoryController { ... }

    @GetMapping("/report")
    public List<InventoryReportItem> getFullReport() {
        return inventoryService.getFullInventoryReport();
    }

    @PostMapping("/stock-in")
    public ResponseEntity<?> addStock(@RequestBody StockUpdateRequest request) {
        try {
            Inventory updatedInventory = inventoryService.addStock(request.getProductId(), request.getQuantity());
            return ResponseEntity.ok(updatedInventory);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/stock-out")
    public ResponseEntity<?> removeStock(@RequestBody StockUpdateRequest request) {
        try {
            Inventory updatedInventory = inventoryService.removeStock(request.getProductId(), request.getQuantity());
            return ResponseEntity.ok(updatedInventory);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
