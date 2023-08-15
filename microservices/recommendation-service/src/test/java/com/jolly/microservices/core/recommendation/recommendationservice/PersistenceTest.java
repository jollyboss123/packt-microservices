package com.jolly.microservices.core.recommendation.recommendationservice;

import com.jolly.microservices.core.recommendation.persistence.RecommendationEntity;
import com.jolly.microservices.core.recommendation.persistence.RecommendationRepository;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;

/**
 * @author jolly
 */
@DataMongoTest
public class PersistenceTest extends MongoTestBase {
    @Autowired
    private RecommendationRepository repository;
    private RecommendationEntity savedEntity;

    @BeforeEach
    void setupDb() {
        repository.deleteAll().block();

        RecommendationEntity entity = new RecommendationEntity(1, 2, "a", 3, "c");
        savedEntity = repository.save(entity).block();

        assertEqualsRecommendation(entity, savedEntity);
    }


    @Test
    void create() {
        RecommendationEntity newEntity = new RecommendationEntity(1, 3, "a", 3, "c");
        repository.save(newEntity).block();

        RecommendationEntity foundEntity = repository.findById(newEntity.getId()).block();
        assertEqualsRecommendation(newEntity, foundEntity);

        Assertions.assertEquals(2, repository.count().block());
    }

    @Test
    void update() {
        savedEntity.setAuthor("a2");
        repository.save(savedEntity).block();

        RecommendationEntity foundEntity = repository.findById(savedEntity.getId()).block();
        Assertions.assertEquals(1, (long)foundEntity.getVersion());
        Assertions.assertEquals("a2", foundEntity.getAuthor());
    }

    @Test
    void delete() {
        repository.delete(savedEntity).block();
        Assertions.assertNotEquals(Boolean.TRUE, repository.existsById(savedEntity.getId()).block());
    }

    @Test
    void getByProductId() {
        List<RecommendationEntity> entityList = repository.findByProductId(savedEntity.getProductId()).collectList().block();

        MatcherAssert.assertThat(entityList, hasSize(1));
        assertEqualsRecommendation(savedEntity, entityList.get(0));
    }

    @Test
    void duplicateError() {
        Assertions.assertThrows(DuplicateKeyException.class, () -> {
            RecommendationEntity entity = new RecommendationEntity(1, 2, "a", 3, "c");
            repository.save(entity).block();
        });
    }

    @Test
    void optimisticLockError() {

        // Store the saved entity in two separate entity objects
        RecommendationEntity entity1 = repository.findById(savedEntity.getId()).block();
        RecommendationEntity entity2 = repository.findById(savedEntity.getId()).block();

        // Update the entity using the first entity object
        entity1.setAuthor("a1");
        repository.save(entity1).block();

        //  Update the entity using the second entity object.
        // This should fail since the second entity now holds an old version number, i.e. an Optimistic Lock Error
        Assertions.assertThrows(OptimisticLockingFailureException.class, () -> {
            entity2.setAuthor("a2");
            repository.save(entity2).block();
        });

        // Get the updated entity from the database and verify its new state
        RecommendationEntity updatedEntity = repository.findById(savedEntity.getId()).block();
        Assertions.assertEquals(1, (int)updatedEntity.getVersion());
        Assertions.assertEquals("a1", updatedEntity.getAuthor());
    }

    private void assertEqualsRecommendation(RecommendationEntity expectedEntity, RecommendationEntity actualEntity) {
        Assertions.assertEquals(expectedEntity.getId(), actualEntity.getId());
        Assertions.assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
        Assertions.assertEquals(expectedEntity.getProductId(), actualEntity.getProductId());
        Assertions.assertEquals(expectedEntity.getRecommendationId(), actualEntity.getRecommendationId());
        Assertions.assertEquals(expectedEntity.getAuthor(), actualEntity.getAuthor());
        Assertions.assertEquals(expectedEntity.getRating(), actualEntity.getRating());
        Assertions.assertEquals(expectedEntity.getContent(), actualEntity.getContent());
    }
}
