package com.shop.inventory.service;

import com.shop.inventory.dto.InventoryDto;
import com.shop.inventory.dto.StockCheckResponse;
import com.shop.inventory.model.Inventory;
import com.shop.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {
    
    private final InventoryRepository inventoryRepository;
    
    public List<InventoryDto> getAllInventory() {
        return inventoryRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    public InventoryDto getByProductId(Long productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + productId));
        return toDto(inventory);
    }
    
    public StockCheckResponse checkStock(Long productId, Integer requiredQuantity) {
        return inventoryRepository.findByProductId(productId)
                .map(inv -> StockCheckResponse.builder()
                        .productId(productId)
                        .inStock(inv.getAvailableQuantity() >= requiredQuantity)
                        .availableQuantity(inv.getAvailableQuantity())
                        .build())
                .orElse(StockCheckResponse.builder()
                        .productId(productId)
                        .inStock(false)
                        .availableQuantity(0)
                        .build());
    }
    
    @Transactional
    public InventoryDto addStock(Long productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElse(Inventory.builder()
                        .productId(productId)
                        .quantity(0)
                        .reservedQuantity(0)
                        .build());
        
        inventory.setQuantity(inventory.getQuantity() + quantity);
        inventory = inventoryRepository.save(inventory);
        return toDto(inventory);
    }
    
    @Transactional
    public InventoryDto reserveStock(Long productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + productId));
        
        if (inventory.getAvailableQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock for product: " + productId);
        }
        
        inventory.setReservedQuantity(inventory.getReservedQuantity() + quantity);
        inventory = inventoryRepository.save(inventory);
        return toDto(inventory);
    }
    
    @Transactional
    public InventoryDto confirmReservation(Long productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + productId));
        
        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventory.setReservedQuantity(inventory.getReservedQuantity() - quantity);
        inventory = inventoryRepository.save(inventory);
        return toDto(inventory);
    }
    
    @Transactional
    public InventoryDto releaseReservation(Long productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + productId));
        
        inventory.setReservedQuantity(Math.max(0, inventory.getReservedQuantity() - quantity));
        inventory = inventoryRepository.save(inventory);
        return toDto(inventory);
    }
    
    private InventoryDto toDto(Inventory inventory) {
        return InventoryDto.builder()
                .id(inventory.getId())
                .productId(inventory.getProductId())
                .quantity(inventory.getQuantity())
                .reservedQuantity(inventory.getReservedQuantity())
                .availableQuantity(inventory.getAvailableQuantity())
                .build();
    }
}
