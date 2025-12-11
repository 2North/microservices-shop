import com.shop.product.dto.ProductDto;
import com.shop.product.model.Product;
import com.shop.product.repository.ProductRepository;
import com.shop.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Юнит-тест для ProductService
 *
 * Тестирует:
 * - Получение товара по ID
 * - Поиск товаров по названию
 * - Обработка случая "товар не найден"
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id(1L)
                .name("MacBook Pro")
                .description("Apple laptop")
                .price(new BigDecimal("1999.99"))
                .category("Electronics")
                .imageUrl("http://example.com/macbook.jpg")
                .build();
    }

    @Test
    void getProductById_ProductExists_ReturnsProductDto() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act
        ProductDto result = productService.getProductById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("MacBook Pro", result.getName());
        assertEquals(new BigDecimal("1999.99"), result.getPrice());
        assertEquals("Electronics", result.getCategory());

        verify(productRepository).findById(1L);
    }

    @Test
    void getProductById_ProductNotFound_ThrowsException() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.getProductById(999L);
        });

        assertEquals("Product not found", exception.getMessage());
    }

    @Test
    void searchProducts_FindsMatchingProducts() {
        // Arrange
        Product product1 = Product.builder()
                .id(1L)
                .name("MacBook Pro")
                .price(new BigDecimal("1999.99"))
                .build();
        Product product2 = Product.builder()
                .id(2L)
                .name("MacBook Air")
                .price(new BigDecimal("999.99"))
                .build();

        when(productRepository.findByNameContainingIgnoreCase("MacBook"))
                .thenReturn(Arrays.asList(product1, product2));

        // Act
        List<ProductDto> results = productService.searchProducts("MacBook");

        // Assert
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(p -> p.getName().contains("MacBook")));

        verify(productRepository).findByNameContainingIgnoreCase("MacBook");
    }
}