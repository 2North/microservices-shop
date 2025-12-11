package service;

import com.shop.order.client.ServiceClients;
import com.shop.order.dto.OrderDto;
import com.shop.order.model.Order;
import com.shop.order.model.OrderItem;
import com.shop.order.model.OrderStatus;
import com.shop.order.repository.OrderRepository;
import com.shop.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Юнит-тест для OrderService
 *
 * Тестирует:
 * - Отмена заказа со статусом PENDING
 * - Проверка авторизации (только владелец может отменить)
 * - Запрет отмены подтверждённых заказов
 * - Взаимодействие с Inventory и Notification сервисами
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ServiceClients serviceClients;

    @InjectMocks
    private OrderService orderService;

    private Order pendingOrder;
    private Order confirmedOrder;

    @BeforeEach
    void setUp() {
        // Заказ со статусом PENDING
        OrderItem item = OrderItem.builder()
                .id(1L)
                .productId(100L)
                .productName("MacBook Pro")
                .quantity(2)
                .price(new BigDecimal("1999.99"))
                .build();

        pendingOrder = Order.builder()
                .id(1L)
                .userId(1L)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("3999.98"))
                .shippingAddress("Test Address")
                .items(new ArrayList<>(List.of(item)))
                .createdAt(LocalDateTime.now())
                .build();
        item.setOrder(pendingOrder);

        // Заказ со статусом CONFIRMED
        confirmedOrder = Order.builder()
                .id(2L)
                .userId(1L)
                .status(OrderStatus.CONFIRMED)
                .totalAmount(new BigDecimal("999.99"))
                .shippingAddress("Test Address")
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void cancelOrder_PendingOrder_CancelsSuccessfully() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));

        Order cancelledOrder = Order.builder()
                .id(1L)
                .userId(1L)
                .status(OrderStatus.CANCELLED)
                .totalAmount(pendingOrder.getTotalAmount())
                .items(pendingOrder.getItems())
                .build();
        when(orderRepository.save(any(Order.class))).thenReturn(cancelledOrder);

        // Act
        OrderDto result = orderService.cancelOrder(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(OrderStatus.CANCELLED, result.getStatus());

        // Проверяем, что резерв был освобождён
        verify(serviceClients).releaseStock(100L, 2);

        // Проверяем, что уведомление было отправлено
        verify(serviceClients).sendNotification(any(ServiceClients.NotificationRequest.class));
    }

    @Test
    void cancelOrder_NotOwner_ThrowsException() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));

        // Act & Assert (пользователь 2 пытается отменить заказ пользователя 1)
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.cancelOrder(1L, 2L);
        });

        assertEquals("Not authorized to cancel this order", exception.getMessage());

        // Проверяем, что save и releaseStock НЕ были вызваны
        verify(orderRepository, never()).save(any(Order.class));
        verify(serviceClients, never()).releaseStock(anyLong(), anyInt());
    }

    @Test
    void cancelOrder_ConfirmedOrder_ThrowsException() {
        // Arrange
        when(orderRepository.findById(2L)).thenReturn(Optional.of(confirmedOrder));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderService.cancelOrder(2L, 1L);
        });

        assertEquals("Only pending orders can be cancelled", exception.getMessage());

        // Проверяем, что ничего не изменилось
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getOrderById_OrderExists_ReturnsOrderDto() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));

        // Act
        OrderDto result = orderService.getOrderById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getUserId());
        assertEquals(OrderStatus.PENDING, result.getStatus());
        assertEquals(new BigDecimal("3999.98"), result.getTotalAmount());
        assertEquals(1, result.getItems().size());

        verify(orderRepository).findById(1L);
    }
}