package com.jolly.microservices.api.core.recommendation;

/**
 * @author jolly
 */
public record Recommendation(
        int productId,
        int recommendationId,
        String author,
        int rate,
        String content,
        String serviceAddress
) {
    public Recommendation() {
        this(0, 0, null, 0, null, null);
    }

    public Recommendation {}
}
