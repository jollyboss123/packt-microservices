package com.jolly.microservices.api.core.product;

/**
 * @author jolly
 */
public record Product(
        int productId,
        String name,
        int weight,
        String serviceAddress
) {
    public Product(){
        this(0, null, 0, null);
    }

    public Product {}
}
