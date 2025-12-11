package com.shop.order.service;

import com.shop.order.client.ServiceClients;
import com.shop.order.dto.*;
import com.shop.order.model.Order;
import com.shop.order.model.OrderItem;
import com.shop.order.model.OrderStatus;
import com.shop.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final ServiceClients serviceClients;
    
    @Transactional
    public OrderDto createOrder(Long userId, CreateOrderRequest request) {
        // Проверяем наличие товаров и собираем информацию
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (OrderItemRequest itemRequest : request.getItems()) {
            // Проверяем наличие на складе
            ServiceClients.StockCheckResponse stockCheck = 
                    serviceClients.checkStock(itemRequest.getProductId(), itemRequest.getQuantity());
            
            if (!stockCheck.isInStock()) {
                throw new RuntimeException("Product " + itemRequest.getProductId() + 
                        " is out of stock. Available: " + stockCheck.getAvailableQuantity());
            }
            
            // Получаем информацию о товаре
            ServiceClients.ProductResponse product = serviceClients.getProduct(itemRequest.getProductId());
            
            OrderItem item = OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(itemRequest.getQuantity())
                    .price(product.getPrice())
                    .build();
            
            orderItems.add(item);
            totalAmount = totalAmount.add(item.getSubtotal());
        }
        
        // Резервируем товары
        for (OrderItemRequest itemRequest : request.getItems()) {
            serviceClients.reserveStock(itemRequest.getProductId(), itemRequest.getQuantity());
        }
        
        // Создаём заказ
        Order order = Order.builder()
                .userId(userId)
                .status(OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .shippingAddress(request.getShippingAddress())
                .items(new ArrayList<>())
                .build();
        
        for (OrderItem item : orderItems) {
            order.addItem(item);
        }
        
        order = orderRepository.save(order);
        
        // Отправляем уведомление
        serviceClients.sendNotification(new ServiceClients.NotificationRequest(
                userId,
                "ORDER_CREATED",
                "Order Created",
                "Your order #" + order.getId() + " has been created successfully!"
        ));
        
        return toDto(order);
    }
    
    public OrderDto getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return toDto(order);
    }
    
    public List<OrderDto> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    public List<OrderDto> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public OrderDto updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        
        // Если заказ подтверждён, снимаем резерв и списываем товары
        if (newStatus == OrderStatus.CONFIRMED && oldStatus == OrderStatus.PENDING) {
            for (OrderItem item : order.getItems()) {
                serviceClients.confirmStock(item.getProductId(), item.getQuantity());
            }
        }
        
        // Если заказ отменён, освобождаем резерв
        if (newStatus == OrderStatus.CANCELLED && oldStatus == OrderStatus.PENDING) {
            for (OrderItem item : order.getItems()) {
                serviceClients.releaseStock(item.getProductId(), item.getQuantity());
            }
        }
        
        order = orderRepository.save(order);
        
        // Отправляем уведомление
        serviceClients.sendNotification(new ServiceClients.NotificationRequest(
                order.getUserId(),
                "ORDER_STATUS_CHANGED",
                "Order Status Updated",
                "Your order #" + order.getId() + " status changed to " + newStatus
        ));
        
        return toDto(order);
    }
    
    @Transactional
    public OrderDto cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Not authorized to cancel this order");
        }
        
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Only pending orders can be cancelled");
        }
        
        return updateOrderStatus(orderId, OrderStatus.CANCELLED);
    }
    
    private OrderDto toDto(Order order) {
        List<OrderItemDto> itemDtos = order.getItems().stream()
                .map(item -> OrderItemDto.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());
        
        return OrderDto.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .items(itemDtos)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
