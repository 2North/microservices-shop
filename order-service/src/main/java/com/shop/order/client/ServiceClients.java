package com.shop.order.client;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

@Component
public class ServiceClients {
    
    private final WebClient productClient;
    private final WebClient inventoryClient;
    private final WebClient notificationClient;
    
    public ServiceClients(
            @Value("${services.product-service.url}") String productUrl,
            @Value("${services.inventory-service.url}") String inventoryUrl,
            @Value("${services.notification-service.url}") String notificationUrl) {
        this.productClient = WebClient.builder().baseUrl(productUrl).build();
        this.inventoryClient = WebClient.builder().baseUrl(inventoryUrl).build();
        this.notificationClient = WebClient.builder().baseUrl(notificationUrl).build();
    }
    
    public ProductResponse getProduct(Long productId) {
        return productClient.get()
                .uri("/api/products/{id}", productId)
                .retrieve()
                .bodyToMono(ProductResponse.class)
                .block();
    }
    
    public StockCheckResponse checkStock(Long productId, Integer quantity) {
        return inventoryClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/inventory/check/{productId}")
                        .queryParam("quantity", quantity)
                        .build(productId))
                .retrieve()
                .bodyToMono(StockCheckResponse.class)
                .block();
    }
    
    public void reserveStock(Long productId, Integer quantity) {
        inventoryClient.post()
                .uri("/api/inventory/reserve")
                .bodyValue(new StockRequest(productId, quantity))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }
    
    public void confirmStock(Long productId, Integer quantity) {
        inventoryClient.post()
                .uri("/api/inventory/confirm")
                .bodyValue(new StockRequest(productId, quantity))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }
    
    public void releaseStock(Long productId, Integer quantity) {
        inventoryClient.post()
                .uri("/api/inventory/release")
                .bodyValue(new StockRequest(productId, quantity))
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }
    
    public void sendNotification(NotificationRequest request) {
        notificationClient.post()
                .uri("/api/notifications/send")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe(); // Fire and forget
    }
    
    @Data
    public static class ProductResponse {
        private Long id;
        private String name;
        private String description;
        private BigDecimal price;
        private String category;
    }
    
    @Data
    public static class StockCheckResponse {
        private Long productId;
        private boolean inStock;
        private Integer availableQuantity;
    }
    
    @Data
    public static class StockRequest {
        private Long productId;
        private Integer quantity;
        
        public StockRequest(Long productId, Integer quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }
    }
    
    @Data
    public static class NotificationRequest {
        private Long userId;
        private String type;
        private String title;
        private String message;
        
        public NotificationRequest(Long userId, String type, String title, String message) {
            this.userId = userId;
            this.type = type;
            this.title = title;
            this.message = message;
        }
    }
}
