package com.wiredbraincoffee.product_api_functional;

import com.wiredbraincoffee.product_api_functional.model.Product;
import com.wiredbraincoffee.product_api_functional.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class ProductApiFunctionalApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductApiFunctionalApplication.class, args);
	}


	@Bean
	CommandLineRunner init(ProductRepository productRepository) {
		return args -> {
			Flux<Product> productFlux = Flux.just(
							new Product(null, "Big Latte", 99.9),
							new Product(null, "Espresso", 79.9),
							new Product(null, "Green Tea", 59.9)
					)
					.flatMap(productRepository::save);


			productFlux
					.thenMany(productRepository.findAll())
					.subscribe(System.out::println);
		};

	}
}
