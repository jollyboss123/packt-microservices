package com.jolly.microservices.api.core.review;

/**
 * @author jolly
 */
public record Review(
        int productId,
        int reviewId,
        String author,
        String subject,
        String content,
        String serviceAddress
) {
    public Review() {
        this(0, 0, null, null, null, null);
    }

    public Review {}
}
