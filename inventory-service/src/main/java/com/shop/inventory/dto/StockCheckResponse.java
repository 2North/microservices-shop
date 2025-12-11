package com.shop.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class StockCheckResponse {
    private Long productId;
    private boolean inStock;
    private Integer availableQuantity;
}
