package service;

import com.shop.inventory.dto.InventoryDto;
import com.shop.inventory.dto.StockCheckResponse;
import com.shop.inventory.model.Inventory;
import com.shop.inventory.repository.InventoryRepository;
import com.shop.inventory.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Юнит-тест для InventoryService
 *
 * Тестирует:
 * - Резервирование товара на складе
 * - Проверка наличия достаточного количества
 * - Обработка недостаточного количества
 */
@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Inventory testInventory;

    @BeforeEach
    void setUp() {
        testInventory = Inventory.builder()
                .id(1L)
                .productId(100L)
                .quantity(50)           // Всего на складе
                .reservedQuantity(10)   // Уже зарезервировано
                .build();
        // availableQuantity = 50 - 10 = 40
    }

    @Test
    void reserveStock_SufficientStock_ReservesSuccessfully() {
        // Arrange
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));

        Inventory savedInventory = Inventory.builder()
                .id(1L)
                .productId(100L)
                .quantity(50)
                .reservedQuantity(15)  // 10 + 5 = 15
                .build();
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(savedInventory);

        // Act
        InventoryDto result = inventoryService.reserveStock(100L, 5);

        // Assert
        assertNotNull(result);
        assertEquals(15, result.getReservedQuantity());
        assertEquals(35, result.getAvailableQuantity()); // 50 - 15 = 35

        verify(inventoryRepository).findByProductId(100L);
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    void reserveStock_InsufficientStock_ThrowsException() {
        // Arrange
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));
        // Доступно только 40, пытаемся зарезервировать 50

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            inventoryService.reserveStock(100L, 50);
        });

        assertEquals("Insufficient stock for product: 100", exception.getMessage());

        // Проверяем, что save НЕ был вызван
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void checkStock_ProductInStock_ReturnsTrue() {
        // Arrange
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));

        // Act
        StockCheckResponse result = inventoryService.checkStock(100L, 30);

        // Assert
        assertTrue(result.isInStock());      // 40 >= 30
        assertEquals(40, result.getAvailableQuantity());
        assertEquals(100L, result.getProductId());
    }

    @Test
    void checkStock_ProductOutOfStock_ReturnsFalse() {
        // Arrange
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));

        // Act
        StockCheckResponse result = inventoryService.checkStock(100L, 50);

        // Assert
        assertFalse(result.isInStock());     // 40 < 50
        assertEquals(40, result.getAvailableQuantity());
    }
}