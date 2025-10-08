package com.example.cli_application;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.example.cli_application.dto.Inventory;
import com.example.cli_application.dto.InventoryReportItem;
import com.example.cli_application.dto.Product;

import reactor.core.publisher.Mono;

@SpringBootApplication
public class CliApplication implements CommandLineRunner {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_CYAN = "\u001B[36m";

    private final WebClient webClient = WebClient.create("http://localhost:8085");
    private final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(CliApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE); // <-- This is the fix
        app.run(args);
    }

    @Override
    public void run(String... args) {
        while (true) {
            showMenu();
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1 -> addProduct();
                case 2 -> getProductById();
                case 3 -> listProducts();
                case 4 -> addStock();
                case 5 -> removeStock();
                case 6 -> checkStock();
                case 7 -> viewInventoryReport();
                case 0 -> {
                    System.out.println(ANSI_BLUE + "Exiting application. Goodbye!" + ANSI_RESET);
                    return;
                }
                default -> System.out.println(ANSI_RED + "Invalid choice. Please try again." + ANSI_RESET);
            }
        }
    }

    private void showMenu() {
        System.out.println(ANSI_CYAN + "\n=============================================");
        System.out.println("  Inventory Management System CLI");
        System.out.println("=============================================" + ANSI_RESET);
        System.out.println("1. Add New Product");
        System.out.println("2. List Product");
        System.out.println("3. List All Products");
        System.out.println("4. Add Stock (Stock-In)");
        System.out.println("5. Remove Stock (Stock-Out)");
        System.out.println("6. Check Stock for a Product");
        System.out.println("7. View Full Inventory Report");
        System.out.println(ANSI_RED + "0. Exit" + ANSI_RESET);
        System.out.print(ANSI_GREEN + "Enter your choice: " + ANSI_RESET);
    }

    // Add this new method to the CliApplication class

    private void viewInventoryReport() {
        try {
            List<InventoryReportItem> reportItems = webClient.get()
                    .uri("/api/inventory/report")
                    .retrieve()
                    .bodyToFlux(InventoryReportItem.class)
                    .collectList()
                    .block();

            System.out.println(ANSI_CYAN + "\n--- Full Inventory Report ---" + ANSI_RESET);
            System.out.println("-----------------------------------------------------------------");
            System.out.printf("%-15s | %-30s | %-10s%n", "SKU", "Product Name", "Quantity");
            System.out.println("-----------------------------------------------------------------");

            if (reportItems == null || reportItems.isEmpty()) {
                System.out.println("Inventory is empty.");
            } else {
                reportItems.forEach(item -> System.out.printf("%-15s | %-30s | %-10d%n",
                        item.getProduct().getSku(),
                        item.getProduct().getName(),
                        item.getQuantity()));
            }
            System.out.println("-----------------------------------------------------------------");

        } catch (Exception e) {
            handleError(e);
        }
    }

    private void addProduct() {
        try {
            System.out.print("Enter SKU: ");
            String sku = scanner.nextLine();
            System.out.print("Enter Name: ");
            String name = scanner.nextLine();
            System.out.print("Enter Description: ");
            String description = scanner.nextLine();
            System.out.print("Enter Cost Price: ");
            BigDecimal costPrice = scanner.nextBigDecimal();
            System.out.print("Enter Selling Price: ");
            BigDecimal sellingPrice = scanner.nextBigDecimal();
            scanner.nextLine(); // Consume newline

            Product newProduct = new Product(sku, name, description, costPrice, sellingPrice);

            Product createdProduct = webClient.post()
                    .uri("/api/products")
                    .body(Mono.just(newProduct), Product.class)
                    .retrieve()
                    .bodyToMono(Product.class)
                    .block();

            System.out.println(ANSI_GREEN + "\nProduct created successfully: " + createdProduct + ANSI_RESET);
        } catch (Exception e) {
            handleError(e);
        }
    }

    private void listProducts() {
        try {
            List<Product> products = webClient.get()
                    .uri("/api/products")
                    .retrieve()
                    .bodyToFlux(Product.class)
                    .collectList()
                    .block();

            System.out.println(ANSI_CYAN + "\n--- All Products ---" + ANSI_RESET);
            products.forEach(
                    p -> System.out.println("ID: " + p.getId() + ", SKU: " + p.getSku() + ", Name: " + p.getName()));
        } catch (Exception e) {
            handleError(e);
        }
    }

    private void getProductById() {
        try {
            System.out.print("Enter Product ID: ");
            long productId = scanner.nextLong();
            scanner.nextLine(); // Consume newline
            Product p = webClient.get()
                    .uri("/api/products/" + productId)
                    .retrieve()
                    .bodyToMono(Product.class)
                    .block();
            System.out.println("\nID: " + p.getId() + ", SKU: " + p.getSku() + ", Name: " + p.getName());
        } catch (Exception e) {
            handleError(e);
        }
    }

    private void addStock() {
        updateStock("/api/inventory/stock-in", "Stock added successfully!");
    }

    private void removeStock() {
        updateStock("/api/inventory/stock-out", "Stock removed successfully!");
    }

    private void updateStock(String uri, String successMessage) {
        try {
            System.out.print("Enter Product ID: ");
            long productId = scanner.nextLong();
            System.out.print("Enter Quantity: ");
            int quantity = scanner.nextInt();
            scanner.nextLine(); // Consume newline
            
            while(quantity <= 0){
                System.out.println(ANSI_RED + "Quantity should be positive" + ANSI_RESET);
                System.out.print("Enter Quantity: ");
                quantity = scanner.nextInt();
            }

            Inventory request = new Inventory();
            request.setProductId(productId);
            request.setQuantity(quantity);

            Inventory updatedInventory = webClient.post()
                    .uri(uri)
                    .body(Mono.just(request), Inventory.class)
                    .retrieve()
                    .bodyToMono(Inventory.class)
                    .block();

            System.out.println(ANSI_GREEN + "\n" + successMessage + " New quantity: " + updatedInventory.getQuantity()
                    + ANSI_RESET);
        } catch (Exception e) {
            handleError(e);
        }
    }

    private void checkStock() {
        try {
            System.out.print("Enter Product ID: ");
            long productId = scanner.nextLong();
            scanner.nextLine(); // Consume newline

            Inventory inventory = webClient.get()
                    .uri("/api/inventory/product/" + productId)
                    .retrieve()
                    .bodyToMono(Inventory.class)
                    .block();

            System.out.println(ANSI_GREEN + "\nStock for Product ID " + productId + ": " + inventory.getQuantity()
                    + " units." + ANSI_RESET);
        } catch (Exception e) {
            handleError(e);
        }
    }

    private void handleError(Exception e) {
        if (e instanceof WebClientResponseException ex) {
            System.out.println(ANSI_RED + "\nError from server: " + ex.getResponseBodyAsString() + ANSI_RESET);
        } else {
            System.out.println(ANSI_RED + "\nAn unexpected error occurred: " + e.getMessage() + ANSI_RESET);
        }
    }

}
