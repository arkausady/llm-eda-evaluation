package com.example.ecommerce;

import com.example.ecommerce.entity.InventoryItem;
import com.example.ecommerce.repository.InventoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EcommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceApplication.class, args);
    }

    @Bean
    CommandLineRunner initializeInventory(InventoryRepository inventoryRepository) {
        return args -> {
            inventoryRepository.findById("SKU-1")
                    .orElseGet(() -> inventoryRepository.save(new InventoryItem("SKU-1", 100)));
            inventoryRepository.findById("SKU-2")
                    .orElseGet(() -> inventoryRepository.save(new InventoryItem("SKU-2", 50)));
        };
    }
}
