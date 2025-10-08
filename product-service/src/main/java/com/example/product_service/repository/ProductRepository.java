package com.example.product_service.repository;

import org.springframework.stereotype.Repository;
import com.example.product_service.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


@Repository
public interface ProductRepository extends JpaRepository<Product, Long>  {
    Optional<Product> findBySku(String sku);
}