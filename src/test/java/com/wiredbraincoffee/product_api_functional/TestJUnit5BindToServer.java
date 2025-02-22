package com.wiredbraincoffee.product_api_functional;

import com.wiredbraincoffee.product_api_functional.model.Product;
import com.wiredbraincoffee.product_api_functional.model.ProductEvent;
import com.wiredbraincoffee.product_api_functional.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestJUnit5BindToServer {

    private WebTestClient webTestClient;

    private List<Product> expectedList;

    @Autowired
    private ProductRepository productRepository;

    @LocalServerPort
    private int port;

    @BeforeEach
    public void beforeEach(){
        this.webTestClient =
                WebTestClient.
                        bindToServer()
                        .baseUrl("http://localhost:" + port + "/products")
                        .build();

        this.expectedList =
                productRepository.findAll().collectList().block();
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
