package com.jolly.microservices.core.recommendation.services;

import com.jolly.microservices.api.core.recommendation.Recommendation;
import com.jolly.microservices.api.core.recommendation.RecommendationService;
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

    private final RecommendationService recommendationService;

    @Autowired
    public MessageProcessorConfig(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @Bean
    public Consumer<Event<Integer, Recommendation>> messageProcessor() {
        return event -> {
            switch (event.getEventType()) {
                case CREATE -> {
                    Recommendation recommendation = event.getData();
                    LOG.info("Create recommendation with ID: {}/{}", recommendation.productId(), recommendation.recommendationId());
                    recommendationService.createRecommendation(recommendation).block();
                }
                case DELETE -> {
                    int productId = event.getKey();
                    LOG.info("Delete recommendations with productId: {}", productId);
                    recommendationService.deleteRecommendations(productId).block();
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
