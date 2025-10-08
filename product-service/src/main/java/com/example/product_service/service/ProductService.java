package com.example.product_service.service;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.example.product_service.model.Product;
import com.example.product_service.repository.ProductRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public Product createProduct(Product product) {
        // Check if a product with the same SKU already exists
        Optional<Product> existingProduct = productRepository.findBySku(product.getSku());
        if (existingProduct.isPresent()) {
            throw new IllegalArgumentException("Product with SKU " + product.getSku() + " already exists.");
        }
        return productRepository.save(product);
    }
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

}
