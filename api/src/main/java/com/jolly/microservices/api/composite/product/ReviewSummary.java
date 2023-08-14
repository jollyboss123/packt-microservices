package com.jolly.microservices.api.composite.product;

/**
 * @author jolly
 */
public record ReviewSummary(
        int reviewId,
        String author,
        String subject,
        String content
) {
    public ReviewSummary {}
}
