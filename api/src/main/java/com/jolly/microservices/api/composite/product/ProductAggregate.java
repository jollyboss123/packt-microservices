package com.jolly.microservices.api.composite.product;

import java.util.List;

/**
 * @author jolly
 */
public record ProductAggregate(
        int productId,
        String name,
        int weight,
        List<RecommendationSummary> recommendations,
        List<ReviewSummary> reviews,
        ServiceAddresses serviceAddresses
) {
    public ProductAggregate {}
}
