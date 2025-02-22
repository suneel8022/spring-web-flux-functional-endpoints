package com.wiredbraincoffee.product_api_functional;

import com.wiredbraincoffee.product_api_functional.handler.ProductHandler;
import com.wiredbraincoffee.product_api_functional.model.Product;
import com.wiredbraincoffee.product_api_functional.model.ProductEvent;
import com.wiredbraincoffee.product_api_functional.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
public class TestJUnit5RouterFunction {


    private WebTestClient webTestClient;

    private List<Product> expectedList;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RouterFunction routes;

    @BeforeEach
    public void beforeEach(){
        this.webTestClient = WebTestClient
                .bindToRouterFunction(routes)
                .configureClient()
                .baseUrl("/products")
                .build();

        this.expectedList = productRepository
                .findAll().collectList().block();
    }

    @Test
    void testGetAllProducts(){

        webTestClient
                .get()
                .uri("")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Product.class)
                .isEqualTo(expectedList);
    }


    @Test
    void testProductInvalidIdNotFound(){
        String id = "aaa";

        webTestClient
                .get()
                .uri("/{id}",id)
                .exchange()
                .expectStatus()
                .isNotFound();
    }


    @Test
    void testProductIdFound(){
        Product expectedProduct = this.expectedList.get(0);

        webTestClient
                .get()
                .uri("/{id}",expectedProduct.getId())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Product.class)
                .isEqualTo(expectedProduct);
    }


    @Test
    void testProductEvents() {
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
