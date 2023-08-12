package com.jolly.microservices.api.composite.product;

/**
 * @author jolly
 */
public record RecommendationSummary(
        int recommendationId,
        String author,
        int rate
) {
    public RecommendationSummary {}
}
