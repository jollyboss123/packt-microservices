package com.jolly.microservices.core.review.services;

import com.jolly.microservices.api.core.review.Review;
import com.jolly.microservices.api.core.review.ReviewService;
import com.jolly.microservices.api.event.Event;
import com.jolly.microservices.api.exceptions.EventProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

/**
 * @author jolly
 */
@Configuration
public class MessageProcessorConfig {
    private static final Logger LOG = LoggerFactory.getLogger(MessageProcessorConfig.class);

    private final ReviewService reviewService;

    @Autowired
    public MessageProcessorConfig(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Bean
    public Consumer<Event<Integer, Review>> messageProcessor() {
        return event -> {
            switch (event.getEventType()) {
                case CREATE -> {
                    Review review = event.getData();
                    LOG.info("Create review with ID: {}/{}", review.productId(), review.reviewId());
                    reviewService.createReview(review).block();
                }
                case DELETE -> {
                    int productId = event.getKey();
                    LOG.info("Delete reviews with productId: {}", productId);
                    reviewService.deleteReviews(productId).block();
                }
                default -> {
                    String errorMessage = String.format("Incorrect event type: %s, expected CREATE or DELETE event", event.getEventType());
                    LOG.warn(errorMessage);
                    throw new EventProcessingException(errorMessage);
                }
            }
            LOG.info("Message processing done");
        };
    }
}
