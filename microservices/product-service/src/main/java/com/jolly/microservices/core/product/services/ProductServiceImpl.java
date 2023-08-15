package com.jolly.microservices.core.product.services;

import com.jolly.microservices.api.core.product.Product;
import com.jolly.microservices.api.core.product.ProductService;
import com.jolly.microservices.api.exceptions.InvalidInputException;
import com.jolly.microservices.api.exceptions.NotFoundException;
import com.jolly.microservices.core.product.persistence.ProductEntity;
import com.jolly.microservices.core.product.persistence.ProductRepository;
import com.jolly.microservices.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.logging.Level;

/**
 * @author jolly
 */
@RestController
public class ProductServiceImpl implements ProductService {
    private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ServiceUtil serviceUtil;
    private final ProductRepository repository;
    private final ProductMapper mapper;

    @Autowired
    public ProductServiceImpl(
            ServiceUtil serviceUtil,
            ProductRepository repository,
            ProductMapper mapper
    ) {
        this.serviceUtil = serviceUtil;
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Mono<Product> getProduct(int productId) {
        LOG.debug("/product return the found product for productId={}", productId);

        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);

        return repository.findByProductId(productId)
                .switchIfEmpty(Mono.error(new NotFoundException(String.format("No product found for productId: %d", productId))))
                .log(LOG.getName(), Level.FINE)
                .map(mapper::entityToApi)
                .map(e -> new Product(
                        e.productId(),
                        e.name(),
                        e.weight(),
                        serviceUtil.getServiceAddress()
                ));
    }

    @Override
    public Mono<Product> createProduct(Product body) {
        if (body.productId() < 1) {
            throw new InvalidInputException(String.format("Invalid productId: %d", body.productId()));
        }

        ProductEntity entity = mapper.apiToEntity(body);
        return repository.save(entity)
                .log(LOG.getName(), Level.FINE)
                .onErrorMap(
                        DuplicateKeyException.class,
                        ex -> new InvalidInputException(String.format("Duplicate key, productId: %d", body.productId()))
                )
                .map(mapper::entityToApi);
    }

    @Override
    public Mono<Void> deleteProduct(int productId) {
        if (productId < 1) {
            throw new InvalidInputException(String.format("Invalid productId: %d", productId));
        }

        LOG.debug("delete Product: tries to delete an entity with productId: {}", productId);
        return repository.findByProductId(productId)
                    .log(LOG.getName(), Level.FINE)
                    .map(repository::delete)
                    .flatMap(e -> e);
    }
}
