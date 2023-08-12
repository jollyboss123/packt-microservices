package com.jolly.microservices.composite.product.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jolly.microservices.api.core.product.Product;
import com.jolly.microservices.api.core.product.ProductService;
import com.jolly.microservices.api.core.recommendation.Recommendation;
import com.jolly.microservices.api.core.recommendation.RecommendationService;
import com.jolly.microservices.api.core.review.Review;
import com.jolly.microservices.api.core.review.ReviewService;
import com.jolly.microservices.api.exceptions.InvalidInputException;
import com.jolly.microservices.api.exceptions.NotFoundException;
import com.jolly.microservices.util.http.HttpErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jolly
 */
@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {
    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    private final String productServiceUrl;
    private final String recommendationServiceUrl;
    private final String reviewServiceUrl;

    @Autowired
    public ProductCompositeIntegration(
            RestTemplate restTemplate,
            ObjectMapper mapper,
            @Value("${app.product-service.host}") String productServiceHost,
            @Value("${app.product-service.port}") int productServicePort,
            @Value("${app.recommendation-service.host}") String recommendationServiceHost,
            @Value("${app.recommendation-service.port}") int recommendationServicePort,
            @Value("${app.review-service.host}") String reviewServiceHost,
            @Value("${app.review-service.port}") int reviewServicePort
    ) {
        this.restTemplate = restTemplate;
        this.mapper = mapper;

        this.productServiceUrl = String.format("http://%s:%s/product/", productServiceHost, productServicePort);
        this.recommendationServiceUrl = String.format("http://%s:%s/product/?productId=", recommendationServiceHost, recommendationServicePort);
        this.reviewServiceUrl = String.format("http://%s:%s/product/?productId=", reviewServiceHost, reviewServicePort);
    }

    @Override
    public Product getProduct(int productId) {
        try {
            String url = productServiceUrl.concat(String.valueOf(productId));
            LOG.debug("Calling getProduct API on url: {}", url);

            Product product = restTemplate.getForObject(url, Product.class);
            assert product != null;
            LOG.debug("Found product with id: {}", product.productId());

            return product;
        } catch (HttpClientErrorException e) {
            switch (HttpStatus.valueOf(e.getStatusCode().value())) {
                case NOT_FOUND -> throw new NotFoundException(getErrorMessage(e));
                case UNPROCESSABLE_ENTITY -> throw new InvalidInputException(getErrorMessage(e));
                default -> {
                    LOG.warn("Got an unexpected HTTP error: {}, will rethrow", e.getStatusCode());
                    LOG.warn("Error body: {}", e.getResponseBodyAsString());
                    throw e;
                }
            }
        }
    }

    @Override
    public List<Recommendation> getRecommendations(int productId) {
        try {
            String url = recommendationServiceUrl.concat(String.valueOf(productId));
            LOG.debug("Calling getRecommendations API on url: {}", url);

            List<Recommendation> recommendations = restTemplate
                    .exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Recommendation>>() {})
                    .getBody();
            assert recommendations != null;
            LOG.debug("Found {} recommendations for product with id: {}", recommendations.size(), productId);

            return recommendations;
        } catch (Exception e) {
            LOG.warn("Got an exception when requesting recommendations, will return 0 recommendations: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<Review> getReviews(int productId) {
        try {
            String url = reviewServiceUrl.concat(String.valueOf(productId));
            LOG.debug("Calling getReviews API on url: {}", url);

            List<Review> reviews = restTemplate
                    .exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<Review>>() {})
                    .getBody();
            LOG.debug("Found {} reviews for product with id: {}", reviews.size(), productId);

            return reviews;
        } catch (Exception e) {
            LOG.warn("Got an exception when requesting reviews, will return 0 reviews: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    private String getErrorMessage(HttpClientErrorException e) {
        try {
            return mapper.readValue(e.getResponseBodyAsString(), HttpErrorInfo.class).message();
        } catch (IOException ex) {
            return ex.getMessage();
        }
    }
}
