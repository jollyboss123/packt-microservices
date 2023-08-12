package com.jolly.microservices.api.composite.product;

/**
 * @author jolly
 */
public record ServiceAddresses(
        String cmp,
        String pro,
        String rev,
        String rec
) {
    public ServiceAddresses() {
        this(null, null, null, null);
    }
}
