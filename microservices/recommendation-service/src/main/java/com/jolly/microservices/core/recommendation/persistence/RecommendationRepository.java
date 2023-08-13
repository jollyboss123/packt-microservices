package com.jolly.microservices.core.recommendation.persistence;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @author jolly
 */
public interface RecommendationRepository extends CrudRepository<RecommendationEntity, String> {
    List<RecommendationEntity> findByProductId(int productId);
}
