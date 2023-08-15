package com.jolly.microservices.core.product.services;

import com.jolly.microservices.api.core.product.Product;
import com.jolly.microservices.api.core.product.ProductService;
import com.jolly.microservices.api.event.Event;
import com.jolly.microservices.api.exceptions.EventProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

/**
 * @author jolly
 */
@Configuration
public class MessageProcessorConfig {
    private final ProductService productService;

    @Autowired
    public MessageProcessorConfig(ProductService productService) {
        this.productService = productService;
    }

    @Bean
    public Consumer<Event<Integer, Product>> messageProcessor() {
        return event -> {
            switch (event.getEventType()) {
                case CREATE -> {
                    Product product = event.getData();
                    productService.createProduct(product).block();
                }
                case DELETE -> {
                    int productId = event.getKey();
                    productService.deleteProduct(productId).block();
                }
                default -> {
                    String errorMessage = String.format("Incorrect event type: %s, expected CREATE or DELETE event", event.getEventType());
                    throw new EventProcessingException(errorMessage);
                }
            }
        };
    }
}
