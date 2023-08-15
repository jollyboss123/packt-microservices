package com.jolly.microservices.core.recommendation.services;

import com.jolly.microservices.api.core.recommendation.Recommendation;
import com.jolly.microservices.api.core.recommendation.RecommendationService;
import com.jolly.microservices.api.exceptions.InvalidInputException;
import com.jolly.microservices.core.recommendation.persistence.RecommendationEntity;
import com.jolly.microservices.core.recommendation.persistence.RecommendationRepository;
import com.jolly.microservices.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static java.util.logging.Level.FINE;

/**
 * @author jolly
 */
@RestController
public class RecommendationServiceImpl implements RecommendationService {
    private static final Logger LOG = LoggerFactory.getLogger(RecommendationServiceImpl.class);

    private final ServiceUtil serviceUtil;
    private final RecommendationMapper mapper;
    private final RecommendationRepository repository;
    @Autowired
    public RecommendationServiceImpl(
            ServiceUtil serviceUtil,
            RecommendationMapper mapper,
            RecommendationRepository repository
    ) {
        this.serviceUtil = serviceUtil;
        this.mapper = mapper;
        this.repository = repository;
    }

    @Override
    public Mono<Recommendation> createRecommendation(Recommendation body) {
        if (body.productId() < 1) {
            throw new InvalidInputException("Invalid productId: " + body.productId());
        }

        RecommendationEntity entity = mapper.apiToEntity(body);

        return repository.save(entity)
                .log(LOG.getName(), FINE)
                .onErrorMap(
                        DuplicateKeyException.class,
                        ex -> new InvalidInputException("Duplicate key, Product Id: " + body.productId() + ", Recommendation Id:" + body.recommendationId()))
                .map(mapper::entityToApi);
    }

    @Override
    public Flux<Recommendation> getRecommendations(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        LOG.info("Will get recommendations for product with id={}", productId);

        return repository.findByProductId(productId)
                .log(LOG.getName(), FINE)
                .map(mapper::entityToApi)
                .map(e -> new Recommendation(
                        e.productId(),
                        e.recommendationId(),
                        e.author(),
                        e.rate(),
                        e.content(),
                        serviceUtil.getServiceAddress()
                ));
    }

    @Override
    public Mono<Void> deleteRecommendations(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        LOG.debug("deleteRecommendations: tries to delete recommendations for the product with productId: {}", productId);
        return repository.deleteAll(repository.findByProductId(productId));
    }
}
