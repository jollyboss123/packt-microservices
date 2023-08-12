package com.jolly.microservices.composite.product.productcompositeservice;

import com.jolly.microservices.api.core.product.Product;
import com.jolly.microservices.api.core.recommendation.Recommendation;
import com.jolly.microservices.api.core.review.Review;
import com.jolly.microservices.api.exceptions.InvalidInputException;
import com.jolly.microservices.api.exceptions.NotFoundException;
import com.jolly.microservices.composite.product.services.ProductCompositeIntegration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProductCompositeServiceApplicationTests {
	private static final int PRODUCT_ID_OK = 1;
	private static final int PRODUCT_ID_NOT_FOUND = 2;
	private static final int PRODUCT_ID_INVALID = 3;

	@MockBean
	private ProductCompositeIntegration integration;

	@Autowired
	private WebTestClient client;

	@BeforeEach
	void setUp() {
		when(integration.getProduct(PRODUCT_ID_OK))
				.thenReturn(new Product(PRODUCT_ID_OK, "name", 1, "mock-addr"));
		when(integration.getRecommendations(PRODUCT_ID_OK))
				.thenReturn(Collections.singletonList(
						new Recommendation(PRODUCT_ID_OK, 1, "author", 1, "content", "mockAddr")
				));
		when(integration.getReviews(PRODUCT_ID_OK))
				.thenReturn(Collections.singletonList(
						new Review(PRODUCT_ID_OK, 1, "author", "subj", "content", "mockAddr")
				));

		when(integration.getProduct(PRODUCT_ID_NOT_FOUND))
				.thenThrow(new NotFoundException("NOT FOUND: ".concat(String.valueOf(PRODUCT_ID_NOT_FOUND))));

		when(integration.getProduct(PRODUCT_ID_INVALID))
				.thenThrow(new InvalidInputException("INVALID: ".concat(String.valueOf(PRODUCT_ID_INVALID))));
	}

	@Test
	void getProductById() {
		client.get()
				.uri("/product-composite/".concat(String.valueOf(PRODUCT_ID_OK)))
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isOk()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
				.jsonPath("$.recommendations.length()").isEqualTo(1)
				.jsonPath("$.reviews.length()").isEqualTo(1);
	}

	@Test
	public void getProductNotFound() {
		client.get()
				.uri("/product-composite/".concat(String.valueOf(PRODUCT_ID_NOT_FOUND)))
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isNotFound()
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.path").isEqualTo("/product-composite/".concat(String.valueOf(PRODUCT_ID_NOT_FOUND)))
				.jsonPath("$.message").isEqualTo("NOT FOUND: ".concat(String.valueOf(PRODUCT_ID_NOT_FOUND)));
	}

	@Test
	void getProductInvalidInput() {
		client.get()
				.uri("/product-composite/".concat(String.valueOf(PRODUCT_ID_INVALID)))
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody()
				.jsonPath("$.path").isEqualTo("/product-composite/".concat(String.valueOf(PRODUCT_ID_INVALID)))
				.jsonPath("$.message").isEqualTo("INVALID: ".concat(String.valueOf(PRODUCT_ID_INVALID)));
	}
}
