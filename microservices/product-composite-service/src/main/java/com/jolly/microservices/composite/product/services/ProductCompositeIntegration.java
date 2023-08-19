package com.jolly.microservices.composite.product.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jolly.microservices.api.core.product.Product;
import com.jolly.microservices.api.core.product.ProductService;
import com.jolly.microservices.api.core.recommendation.Recommendation;
import com.jolly.microservices.api.core.recommendation.RecommendationService;
import com.jolly.microservices.api.core.review.Review;
import com.jolly.microservices.api.core.review.ReviewService;
import com.jolly.microservices.api.event.Event;
import com.jolly.microservices.api.exceptions.InvalidInputException;
import com.jolly.microservices.api.exceptions.NotFoundException;
import com.jolly.microservices.util.http.HttpErrorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.io.IOException;
import java.util.logging.Level;

/**
 * @author jolly
 */
@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {
    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);

    private static final String PRODUCT_SERVICE_URL = "http://product";
    private static final String RECOMMENDATION_SERVICE_URL = "http://recommendation";
    private static final String REVIEW_SERVICE_URL = "http://review";

    private final Scheduler publishEventScheduler;
    private final WebClient webClient;
    private final ObjectMapper mapper;
    private final StreamBridge streamBridge;

    @Autowired
    public ProductCompositeIntegration(
            @Qualifier("publishEventScheduler") Scheduler publishEventScheduler,
            WebClient.Builder webClientBuilder,
            ObjectMapper mapper,
            StreamBridge streamBridge
    ) {
        this.webClient = webClientBuilder.build();

        this.publishEventScheduler = publishEventScheduler;
        this.mapper = mapper;
        this.streamBridge = streamBridge;
    }

    @Override
    public Mono<Product> createProduct(Product body) {
        return Mono.fromCallable(() -> {
            sendMessage("products-out-0", new Event<>(Event.Type.CREATE, body.productId(), body));
            return body;
        }).subscribeOn(publishEventScheduler);
    }

    @Override
    public Mono<Product> getProduct(int productId) {
        String url = PRODUCT_SERVICE_URL.concat("/product/").concat(String.valueOf(productId));

        return webClient.get()
                .uri(url).retrieve()
                .bodyToMono(Product.class)
                .log(LOG.getName(), Level.FINE)
                .onErrorMap(WebClientResponseException.class, this::handleException);
    }

    @Override
    public Mono<Void> deleteProduct(int productId) {
        return Mono.fromRunnable(() -> sendMessage("products-out-0", new Event<>(Event.Type.DELETE, productId, null)))
                .subscribeOn(publishEventScheduler).then();
    }

    @Override
    public Mono<Recommendation> createRecommendation(Recommendation body) {
        return Mono.fromCallable(() -> {
            sendMessage("recommendations-out-0", new Event<>(Event.Type.CREATE, body.productId(), body));
            return body;
        }).subscribeOn(publishEventScheduler);
    }

    @Override
    public Flux<Recommendation> getRecommendations(int productId) {
        String url = RECOMMENDATION_SERVICE_URL.concat("/recommendation?productId=").concat(String.valueOf(productId));

        // Return an empty result if something goes wrong to make it
        // possible for the composite service to return partial responses
        return webClient.get()
                .uri(url).retrieve()
                .bodyToFlux(Recommendation.class)
                .log(LOG.getName(), Level.FINE)
                .onErrorResume(error -> Flux.empty());
    }

    @Override
    public Mono<Void> deleteRecommendations(int productId) {
        return Mono.fromRunnable(() -> sendMessage("recommendations-out-0", new Event<>(Event.Type.DELETE, productId, null)))
                .subscribeOn(publishEventScheduler).then();
    }

    @Override
    public Mono<Review> createReview(Review body) {
        return Mono.fromCallable(() -> {
            sendMessage("reviews-out-0", new Event<>(Event.Type.CREATE, body.productId(), body));
            return body;
        }).subscribeOn(publishEventScheduler);
    }

    @Override
    public Flux<Review> getReviews(int productId) {
        String url = REVIEW_SERVICE_URL.concat("/review?productId=").concat(String.valueOf(productId));

        LOG.debug("Will call the getReviews API on URL: {}", url);

        // Return an empty result if something goes wrong to make it possible
        // for the composite service to return partial responses
        return webClient.get()
                .uri(url).retrieve()
                .bodyToFlux(Review.class)
                .log(LOG.getName(), Level.FINE)
                .onErrorResume(error -> Flux.empty());
    }

    @Override
    public Mono<Void> deleteReviews(int productId) {
        return Mono.fromRunnable(() -> sendMessage("reviews-out-0", new Event<>(Event.Type.DELETE, productId, null)))
                .subscribeOn(publishEventScheduler).then();
    }

    public Mono<Health> getProductHealth() {
        return getHealth(PRODUCT_SERVICE_URL);
    }

    public Mono<Health> getRecommendationHealth() {
        return getHealth(RECOMMENDATION_SERVICE_URL);
    }

    public Mono<Health> getReviewHealth() {
        return getHealth(REVIEW_SERVICE_URL);
    }

    private Mono<Health> getHealth(String url) {
        url += "/actuator/health";
        LOG.debug("Will call the Health API on URL: {}", url);
        return webClient.get().uri(url).retrieve().bodyToMono(String.class)
                .map(s -> new Health.Builder().up().build())
                .onErrorResume(ex -> Mono.just(new Health.Builder().down(ex).build()))
                .log(LOG.getName(), Level.FINE);
    }

    private void sendMessage(String bindingName, Event event) {
        LOG.debug("Sending a {} message to {}", event.getEventType(), bindingName);
        Message<Event> message = MessageBuilder.withPayload(event)
                .setHeader("partitionKey", event.getKey())
                .build();
        streamBridge.send(bindingName, message);
    }

    private Throwable handleException(Throwable ex) {
        if (!(ex instanceof WebClientResponseException wre)) {
            LOG.warn("Got an unexpected error: {}, will rethrow it", ex.toString());
            return ex;
        }

        switch (HttpStatus.valueOf(wre.getStatusCode().value())) {
            case NOT_FOUND -> {
                return new NotFoundException(getErrorMessage(wre));
            }
            case UNPROCESSABLE_ENTITY -> {
                return new InvalidInputException(getErrorMessage(wre));
            }
            default -> {
                LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", wre.getStatusCode());
                LOG.warn("Error body: {}", wre.getResponseBodyAsString());
                return wre;
            }
        }
    }

    private String getErrorMessage(WebClientResponseException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).message();
        } catch (IOException ioex) {
            return ex.getMessage();
        }
    }
}
