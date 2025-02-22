package com.wiredbraincoffee.product_api_functional;

import com.wiredbraincoffee.product_api_functional.model.Product;
import com.wiredbraincoffee.product_api_functional.model.ProductEvent;
import com.wiredbraincoffee.product_api_functional.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureWebTestClient
public class TestJUnit5AutoConfigureWebTestClient {

    @Autowired
    private WebTestClient webTestClient;

    private List<Product> expectedList;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void beforeEach(){
        this.expectedList =
                productRepository.findAll().collectList().block();

        this.webTestClient =
                this.webTestClient.mutate().baseUrl("/products").build();
    }


    @Test
    public void testAllProducts(){
        webTestClient
                .get()
                .uri("")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Product.class);
    }


    @Test
    public void testProductInvalidIdNotFound(){
        String id = "nice";
        webTestClient
                .get()
                .uri("/{id}", id)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    @Test
    public void testProductIdFound(){
        Product expectedProduct = expectedList.get(0);

        webTestClient
                .get()
                .uri("/{id}",expectedProduct.getId())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Product.class);
    }


    @Test
    public void testProductEvents() {
        FluxExchangeResult<ProductEvent> result =
                webTestClient.get().uri("/events")
                        .accept(MediaType.TEXT_EVENT_STREAM)
                        .exchange()
                        .expectStatus().isOk()
                        .returnResult(ProductEvent.class);

        ProductEvent expectedEvent =
                new ProductEvent(0L, "Product Event");

        StepVerifier.create(result.getResponseBody())
                .expectNext(expectedEvent)
                .expectNextCount(2)
                .consumeNextWith(event ->
                        assertEquals(Long.valueOf(3), event.getEventId()))
                .thenCancel()
                .verify();
    }


}
