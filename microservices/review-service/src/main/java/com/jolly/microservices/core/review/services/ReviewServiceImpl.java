package com.jolly.microservices.core.review.services;

import com.jolly.microservices.api.core.review.Review;
import com.jolly.microservices.api.core.review.ReviewService;
import com.jolly.microservices.api.exceptions.InvalidInputException;
import com.jolly.microservices.core.review.persistence.ReviewEntity;
import com.jolly.microservices.core.review.persistence.ReviewRepository;
import com.jolly.microservices.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * @author jolly
 */
@RestController
public class ReviewServiceImpl implements ReviewService {
    private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ServiceUtil serviceUtil;
    private final ReviewRepository repository;
    private final ReviewMapper mapper;
    private final Scheduler jdbcScheduler;

    @Autowired
    public ReviewServiceImpl(
            ServiceUtil serviceUtil,
            ReviewRepository repository,
            ReviewMapper mapper,
            @Qualifier("jdbcScheduler") Scheduler jdbcScheduler
    ) {
        this.serviceUtil = serviceUtil;
        this.repository = repository;
        this.mapper = mapper;
        this.jdbcScheduler = jdbcScheduler;
    }

    @Override
    public Flux<Review> getReviews(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        LOG.info("Will get reviews for product with id={}", productId);

        return Mono.fromCallable(() -> internalGetReviews(productId))
                .flatMapMany(Flux::fromIterable)
                .log(LOG.getName(), Level.FINE)
                .subscribeOn(jdbcScheduler);
    }

    private List<Review> internalGetReviews(int productId) {
        List<ReviewEntity> entityList = repository.findByProductId(productId);
        List<Review> list = mapper.entityListToApiList(entityList);
        List<Review> mutatedList = new ArrayList<>();
        list.forEach(e -> mutatedList.add(new Review(
                e.productId(),
                e.reviewId(),
                e.author(),
                e.subject(),
                e.content(),
                serviceUtil.getServiceAddress()
        )));

        LOG.debug("Response size: {}", mutatedList.size());
        return mutatedList;
    }

    @Override
    public Mono<Review> createReview(Review body) {
        if (body.productId() < 1) {
            throw new InvalidInputException("Invalid productId: " + body.productId());
        }

        return Mono.fromCallable(() -> internalCreateReview(body))
                .subscribeOn(jdbcScheduler);
    }

    private Review internalCreateReview(Review body) {
        try {
            ReviewEntity entity = mapper.apiToEntity(body);
            ReviewEntity newEntity = repository.save(entity);

            LOG.debug("createReview: created a review entity: {}/{}", body.productId(), body.reviewId());
            return mapper.entityToApi(newEntity);
        } catch (DataIntegrityViolationException dive) {
            throw new InvalidInputException(String.format("Duplicate key, Product Id: %d, Review Id: %d", body.productId(), body.reviewId()));
        }
    }

    @Override
    public Mono<Void> deleteReviews(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        return Mono.fromRunnable(() -> internalDeleteReviews(productId))
                .subscribeOn(jdbcScheduler)
                .then();
    }

    private void internalDeleteReviews(int productId) {
        LOG.debug("deleteReviews: tries to delete reviews for the product with productId: {}", productId);
        repository.deleteAll(repository.findByProductId(productId));
    }
}
