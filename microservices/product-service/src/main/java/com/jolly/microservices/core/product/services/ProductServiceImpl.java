package com.jolly.microservices.core.product.services;

import com.jolly.microservices.api.core.product.Product;
import com.jolly.microservices.api.core.product.ProductService;
import com.jolly.microservices.api.exceptions.InvalidInputException;
import com.jolly.microservices.api.exceptions.NotFoundException;
import com.jolly.microservices.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jolly
 */
@RestController
public class ProductServiceImpl implements ProductService {
    private static final Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ServiceUtil serviceUtil;

    @Autowired
    public ProductServiceImpl(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Product getProduct(int productId) {
        LOG.debug("/product return the found product for productId={}", productId);

        if (productId < 1) {
            throw new InvalidInputException(String.format("Invalid productId: %d", productId));
        }

        if (productId == 13) {
            throw new NotFoundException(String.format("No product found for productId: %d", productId));
        }

        return new Product(productId, String.format("name-%d", productId), 123, serviceUtil.getServiceAddress());
    }
}
