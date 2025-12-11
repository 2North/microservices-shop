package com.shop.product.config;

import com.shop.product.model.Product;
import com.shop.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final ProductRepository productRepository;
    
    @Override
    public void run(String... args) {
        if (productRepository.count() == 0) {
            List<Product> products = List.of(
                Product.builder()
                    .name("MacBook Pro 14")
                    .description("Apple M3 Pro chip, 18GB RAM, 512GB SSD")
                    .price(new BigDecimal("1999.99"))
                    .category("Electronics")
                    .imageUrl("https://picsum.photos/seed/macbook/400/300")
                    .build(),
                Product.builder()
                    .name("iPhone 15 Pro")
                    .description("A17 Pro chip, 256GB, Titanium")
                    .price(new BigDecimal("1199.99"))
                    .category("Electronics")
                    .imageUrl("https://picsum.photos/seed/iphone/400/300")
                    .build(),
                Product.builder()
                    .name("Sony WH-1000XM5")
                    .description("Wireless Noise Cancelling Headphones")
                    .price(new BigDecimal("399.99"))
                    .category("Electronics")
                    .imageUrl("https://picsum.photos/seed/headphones/400/300")
                    .build(),
                Product.builder()
                    .name("Nike Air Max 90")
                    .description("Classic running shoes, White/Black")
                    .price(new BigDecimal("129.99"))
                    .category("Footwear")
                    .imageUrl("https://picsum.photos/seed/nike/400/300")
                    .build(),
                Product.builder()
                    .name("Levi's 501 Original")
                    .description("Classic fit jeans, Dark Blue")
                    .price(new BigDecimal("89.99"))
                    .category("Clothing")
                    .imageUrl("https://picsum.photos/seed/jeans/400/300")
                    .build(),
                Product.builder()
                    .name("Samsung 4K Smart TV 55\"")
                    .description("Crystal UHD, HDR, Smart Hub")
                    .price(new BigDecimal("599.99"))
                    .category("Electronics")
                    .imageUrl("https://picsum.photos/seed/tv/400/300")
                    .build()
            );
            
            productRepository.saveAll(products);
        }
    }
}
