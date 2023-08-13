package com.jolly.microservices.core.product.productservice;

import com.jolly.microservices.api.core.product.Product;
import com.jolly.microservices.core.product.persistence.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductServiceApplicationTests extends MongoTestBase {
	@Autowired
	private WebTestClient client;
	@Autowired
	private ProductRepository repository;

	@BeforeEach
	void setupDb() {
		repository.deleteAll();
	}

	@Test
	void getProductById() {
		int productId = 1;
		postAndVerifyProduct(productId, HttpStatus.OK);

		Assertions.assertTrue(repository.findByProductId(productId).isPresent());

		getAndVerifyProduct(productId, HttpStatus.OK)
				.jsonPath("$.productId").isEqualTo(productId);
	}

	@Test
	void duplicateError() {
		int productId = 1;
		postAndVerifyProduct(productId, HttpStatus.OK);
		Assertions.assertTrue(repository.findByProductId(productId).isPresent());

		postAndVerifyProduct(productId, HttpStatus.UNPROCESSABLE_ENTITY)
				.jsonPath("$.path").isEqualTo("/product")
				.jsonPath("$.message").isEqualTo(String.format("Duplicate key, product id: %d", productId));
	}

	@Test
	void deleteProduct() {
		int productId = 1;
		postAndVerifyProduct(productId, HttpStatus.OK);
		Assertions.assertTrue(repository.findByProductId(productId).isPresent());

		deleteAndVerifyProduct(productId, HttpStatus.OK);
		Assertions.assertFalse(repository.findByProductId(productId).isPresent());

		// to test API idempotency
		deleteAndVerifyProduct(productId, HttpStatus.OK);
	}

	private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expectedStatus) {
		return getAndVerifyProduct("/".concat(String.valueOf(productId)), expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyProduct(String productIdPath, HttpStatus expectedStatus) {
		return client.get()
				.uri("/product".concat(productIdPath))
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody();
	}

	private WebTestClient.BodyContentSpec postAndVerifyProduct(int productId, HttpStatus expectedStatus) {
		Product product = new Product(productId, "Name ".concat(String.valueOf(productId)), productId, "SA");
		return client.post()
				.uri("/product")
				.body(Mono.just(product), Product.class)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody();
	}

	private WebTestClient.BodyContentSpec deleteAndVerifyProduct(int productId, HttpStatus expectedStatus) {
		return client.delete()
				.uri("/product/".concat(String.valueOf(productId)))
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectBody();
	}
}
