package com.wiredbraincoffee.product_api_functional.handler;


import com.wiredbraincoffee.product_api_functional.model.Product;
import com.wiredbraincoffee.product_api_functional.model.ProductEvent;
import com.wiredbraincoffee.product_api_functional.repository.ProductRepository;
import org.jgrapht.util.RadixSort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Component
public class ProductHandler {

    private ProductRepository productRepository;

    public ProductHandler(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }


                    // get All Products
    public Mono<ServerResponse> getAllProducts(ServerRequest serverRequest){
        Flux<Product> products = productRepository.findAll();

        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(products,Product.class);
    }

                    // get a single product
    public Mono<ServerResponse> getProduct(ServerRequest serverRequest){
        String id = serverRequest.pathVariable("id");

        Mono<Product> productMono = this.productRepository.findById(id);
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();

        return productMono
                .flatMap(product ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(fromValue(product))
                )
                .switchIfEmpty(notFound);
    }



                        // Add a product
    public Mono<ServerResponse> saveProduct(ServerRequest serverRequest){
        Mono<Product> productMono = serverRequest.bodyToMono(Product.class);

        return productMono
                .flatMap(product ->
                        ServerResponse.status(HttpStatus.CREATED)
                                .contentType(APPLICATION_JSON)
                                .body(productRepository.save(product),Product.class)
                        );
    }


                        // update a product

    public Mono<ServerResponse> updateProduct(ServerRequest serverRequest){
        String id = serverRequest.pathVariable("id");
        Mono<Product> existingProductMono = this.productRepository.findById(id);
        Mono<Product> productMono = serverRequest.bodyToMono(Product.class);


        Mono<ServerResponse> notFound = ServerResponse.notFound().build();

        return productMono.zipWith(existingProductMono,
                (product, existingProduct) ->
                        new Product(existingProduct.getId(),product.getName(),product.getPrice())
                )
                .flatMap(product ->
                        ServerResponse.ok()
                                .contentType(APPLICATION_JSON)
                                .body(productRepository.save(product),Product.class)
                        )
                .switchIfEmpty(notFound);
    }


                        // delete a product

    public Mono<ServerResponse> deleteProduct(ServerRequest serverRequest){
        String id = serverRequest.pathVariable("id");
        Mono<Product> productMono = this.productRepository.findById(id);

        Mono<ServerResponse> notFound = ServerResponse.notFound().build();

        return productMono
                .flatMap(existingProduct ->
                        ServerResponse.ok()
                                .build(productRepository.delete(existingProduct))
                        )
                .switchIfEmpty(notFound);
    }



                        // delete all products

    public Mono<ServerResponse> deleteAllProducts(ServerRequest serverRequest){
        return ServerResponse.ok()
                .build(productRepository.deleteAll());
    }


                        // adding product events

    public Mono<ServerResponse> getProductEvents(ServerRequest serverRequest){
        Flux<ProductEvent> eventsFlux = Flux.interval(Duration.ofSeconds(1))
                .map(val -> new ProductEvent(val, "Product Event"));

        return ServerResponse.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(eventsFlux, ProductEvent.class);


    }



}
