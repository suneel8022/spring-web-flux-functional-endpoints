package com.wiredbraincoffee.product_api_functional.repository;

import com.wiredbraincoffee.product_api_functional.model.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface ProductRepository
    extends ReactiveMongoRepository<Product,String> {

}
