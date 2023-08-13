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
    public Product getProduct(int productId) {
        LOG.debug("/product return the found product for productId={}", productId);

        if (productId < 1) throw new InvalidInputException("Invalid productId: " + productId);
        ProductEntity entity = repository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("No product found for productId: " + productId));
        Product product = mapper.entityToApi(entity);
        return new Product(
                product.productId(),
                product.name(),
                product.weight(),
                serviceUtil.getServiceAddress()
        );
    }

    @Override
    public Product createProduct(Product body) {
        try {
            ProductEntity entity = mapper.apiToEntity(body);
            ProductEntity newEntity = repository.save(entity);
            return mapper.entityToApi(newEntity);
        } catch (DuplicateKeyException e) {
            throw new InvalidInputException(String.format("Duplicate key, product id: %d", body.productId()));
        }
    }

    @Override
    public void deleteProduct(int productId) {
        repository.findByProductId(productId).ifPresent(repository::delete);
    }
}
