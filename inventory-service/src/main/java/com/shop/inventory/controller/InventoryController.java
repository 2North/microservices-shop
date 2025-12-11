package com.shop.inventory.controller;

import com.shop.inventory.dto.InventoryDto;
import com.shop.inventory.dto.StockCheckResponse;
import com.shop.inventory.dto.StockUpdateRequest;
import com.shop.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {
    
    private final InventoryService inventoryService;
    
    @GetMapping
    public ResponseEntity<List<InventoryDto>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }
    
    @GetMapping("/product/{productId}")
    public ResponseEntity<InventoryDto> getByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getByProductId(productId));
    }
    
    @GetMapping("/check/{productId}")
    public ResponseEntity<StockCheckResponse> checkStock(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") Integer quantity) {
        return ResponseEntity.ok(inventoryService.checkStock(productId, quantity));
    }
    
    @PostMapping("/add")
    public ResponseEntity<InventoryDto> addStock(@Valid @RequestBody StockUpdateRequest request) {
        return ResponseEntity.ok(inventoryService.addStock(request.getProductId(), request.getQuantity()));
    }
    
    @PostMapping("/reserve")
    public ResponseEntity<InventoryDto> reserveStock(@Valid @RequestBody StockUpdateRequest request) {
        return ResponseEntity.ok(inventoryService.reserveStock(request.getProductId(), request.getQuantity()));
    }
    
    @PostMapping("/confirm")
    public ResponseEntity<InventoryDto> confirmReservation(@Valid @RequestBody StockUpdateRequest request) {
        return ResponseEntity.ok(inventoryService.confirmReservation(request.getProductId(), request.getQuantity()));
    }
    
    @PostMapping("/release")
    public ResponseEntity<InventoryDto> releaseReservation(@Valid @RequestBody StockUpdateRequest request) {
        return ResponseEntity.ok(inventoryService.releaseReservation(request.getProductId(), request.getQuantity()));
    }
}
