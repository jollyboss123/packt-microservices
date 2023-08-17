package com.jolly.microservices.composite.product.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author jolly
 */

/**
 * to extend to the product composite's health endpoint,
 * so it also includes the health of the three core services
 * <p>
 * product composite health endpoint will only respond with UP if itself and the three core microservices are healthy
 */
@Configuration
public class CoreServicesHealthConfig {
    private final ProductCompositeIntegration integration;

    @Autowired
    public CoreServicesHealthConfig(ProductCompositeIntegration integration) {
        this.integration = integration;
    }

    @Bean
    ReactiveHealthContributor coreServices() {
        final Map<String, ReactiveHealthIndicator> registry = new LinkedHashMap<>();

        registry.put("product", integration::getProductHealth);
        registry.put("recommendation", integration::getRecommendationHealth);
        registry.put("review", integration::getReviewHealth);

        return CompositeReactiveHealthContributor.fromMap(registry);
    }
}
