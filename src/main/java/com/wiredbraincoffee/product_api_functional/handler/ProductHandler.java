package com.wiredbraincoffee.product_api_functional.handler;


import com.wiredbraincoffee.product_api_functional.repository.ProductRepository;
import org.springframework.stereotype.Component;

@Component
public class ProductHandler {

    private ProductRepository productRepository;

    public ProductHandler(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
}
