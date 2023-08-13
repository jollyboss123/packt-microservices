package com.jolly.microservices.core.product.productservice;

import com.jolly.microservices.core.product.persistence.ProductEntity;
import com.jolly.microservices.core.product.persistence.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author jolly
 */
@DataMongoTest
public class PersistenceTest extends MongoTestBase {
    @Autowired
    private ProductRepository repository;
    private ProductEntity savedEntity;

    @BeforeEach
    void setupDb() {
        repository.deleteAll();

        ProductEntity entity = new ProductEntity(1, "name", 1);
        savedEntity = repository.save(entity);
        assertEqualsProduct(entity, savedEntity);
    }

    @Test
    void create() {
        ProductEntity newEntity = new ProductEntity(2, "name", 2);
        repository.save(newEntity);

        ProductEntity foundEntity = repository.findById(newEntity.getId())
                .orElseThrow(() -> new AssertionError("Expected product entity was not found."));
        assertEqualsProduct(newEntity, foundEntity);

        Assertions.assertEquals(2, repository.count());
    }

    @Test
    void update() {
        savedEntity.setName("name2");
        repository.save(savedEntity);

        ProductEntity foundEntity = repository.findById(savedEntity.getId())
                .orElseThrow(() -> new AssertionError("Expected product entity was not found."));
        Assertions.assertEquals(1, foundEntity.getVersion());
        Assertions.assertEquals("name2", foundEntity.getName());
    }

    @Test
    void delete() {
        repository.delete(savedEntity);
        Assertions.assertFalse(repository.existsById(savedEntity.getId()));
    }

    @Test
    void getByProductId() {
        Optional<ProductEntity> entity = repository.findByProductId(savedEntity.getProductId());
        Assertions.assertTrue(entity.isPresent());
        assertEqualsProduct(savedEntity, entity.get());
    }

    @Test
    void duplicateError() {
        Assertions.assertThrows(DuplicateKeyException.class, () -> {
            ProductEntity entity = new ProductEntity(savedEntity.getProductId(), "n", 1);
            repository.save(entity);
        });
    }

    @Test
    void optimisticLockError() {
        // Store the saved entity in 2 separate entities
        Optional<ProductEntity> optEntity1 = repository.findById(savedEntity.getId());
        Optional<ProductEntity> optEntity2 = repository.findById(savedEntity.getId());
        Assertions.assertTrue(optEntity1.isPresent());
        Assertions.assertTrue(optEntity2.isPresent());
        ProductEntity entity1 = optEntity1.get();
        ProductEntity entity2 = optEntity2.get();

        // Update the 1st entity object
        entity1.setName("name1");
        repository.save(entity1);

        // Update the 2nd entity object
        // This should fail since the 2nd entity now holds an older version
        // number -> Optimistic Lock Error
        Assertions.assertThrows(OptimisticLockingFailureException.class, () -> {
            entity2.setName("name2");
            repository.save(entity2);
        });

        // Get the updated entity from the db & verify its new state
        ProductEntity updatedEntity = repository.findById(savedEntity.getId())
                .orElseThrow(() -> new AssertionError("Expected product entity was not found."));
        Assertions.assertEquals(1, updatedEntity.getVersion());
        Assertions.assertEquals("name1", updatedEntity.getName());
    }

    @Test
    void paging() {
        repository.deleteAll();
        List<ProductEntity> newProducts = IntStream.rangeClosed(1001, 1010)
                .mapToObj(i -> new ProductEntity(i, "name".concat(String.valueOf(i)), i))
                .toList();
        repository.saveAll(newProducts);

        Pageable nextPage = PageRequest.of(0, 4, Sort.Direction.ASC, "productId");
        nextPage = testNextPage(nextPage, "[1001, 1002, 1003, 1004]", true);
        nextPage = testNextPage(nextPage, "[1005, 1006, 1007, 1008]", true);
        testNextPage(nextPage, "[1009, 1010]", false);
    }

    private Pageable testNextPage(Pageable nextPage, String expectedProductIds, boolean expectedNextPage) {
        Page<ProductEntity> productPage = repository.findAll(nextPage);
        Assertions.assertEquals(expectedProductIds, productPage
                .getContent()
                .stream()
                .map(ProductEntity::getProductId).toList().toString()
        );
        Assertions.assertEquals(expectedNextPage, productPage.hasNext());
        return productPage.nextPageable();
    }

    private void assertEqualsProduct(ProductEntity expectEntity, ProductEntity givenEntity) {
        Assertions.assertEquals(expectEntity.getId(), givenEntity.getId());
        Assertions.assertEquals(expectEntity.getProductId(), givenEntity.getProductId());
        Assertions.assertEquals(expectEntity.getVersion(), givenEntity.getVersion());
        Assertions.assertEquals(expectEntity.getName(), givenEntity.getName());
        Assertions.assertEquals(expectEntity.getWeight(), givenEntity.getWeight());
    }
}
