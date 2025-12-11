package com.shop.inventory.config;

import com.shop.inventory.model.Inventory;
import com.shop.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
class GlobalExceptionHandler {
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        Map<String, String> error = new HashMap<>();
        error.put("error", e.getMessage());
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(err -> 
            errors.put(err.getField(), err.getDefaultMessage())
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}

@Component
@RequiredArgsConstructor
class DataInitializer implements CommandLineRunner {
    
    private final InventoryRepository inventoryRepository;
    
    @Override
    public void run(String... args) {
        if (inventoryRepository.count() == 0) {
            List<Inventory> inventories = List.of(
                Inventory.builder().productId(1L).quantity(50).reservedQuantity(0).build(),
                Inventory.builder().productId(2L).quantity(100).reservedQuantity(0).build(),
                Inventory.builder().productId(3L).quantity(75).reservedQuantity(0).build(),
                Inventory.builder().productId(4L).quantity(200).reservedQuantity(0).build(),
                Inventory.builder().productId(5L).quantity(150).reservedQuantity(0).build(),
                Inventory.builder().productId(6L).quantity(30).reservedQuantity(0).build()
            );
            
            inventoryRepository.saveAll(inventories);
        }
    }
}
