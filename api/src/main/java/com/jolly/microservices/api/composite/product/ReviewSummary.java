package com.jolly.microservices.api.composite.product;

/**
 * @author jolly
 */
public record ReviewSummary(
        int reviewId,
        String author,
        String subject
) {
    public ReviewSummary {}
}
