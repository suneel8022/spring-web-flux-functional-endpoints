package com.wiredbraincoffee.product_api_functional;

import com.wiredbraincoffee.product_api_functional.handler.ProductHandler;
import com.wiredbraincoffee.product_api_functional.model.Product;
import com.wiredbraincoffee.product_api_functional.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;


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

					// defining routes using chain routing

//	RouterFunction<ServerResponse> routes(ProductHandler productHandler){
//		return route().
//				GET("/products/events",accept(MediaType.TEXT_EVENT_STREAM),productHandler::getProductEvents)
//				.GET("/products/{id}",accept(MediaType.APPLICATION_JSON),productHandler::getProduct)
//				.GET("/products",accept(MediaType.APPLICATION_JSON),productHandler::getAllProducts)
//				.PUT("/products/{id}",accept(MediaType.APPLICATION_JSON),productHandler::updateProduct)
//				.POST("/products",contentType(MediaType.APPLICATION_JSON),productHandler::saveProduct)
//				.DELETE("/products/{id}",accept(MediaType.APPLICATION_JSON),productHandler::deleteProduct)
//				.DELETE("/products",accept(MediaType.APPLICATION_JSON),productHandler::deleteAllProducts)
//				.build();
//	}

	@Bean
	RouterFunction<ServerResponse> routes(ProductHandler productHandler){
		return route()
				.path("/products",
						builder -> builder
								.nest(accept(MediaType.APPLICATION_JSON).or(contentType(MediaType.APPLICATION_JSON)).or(accept(MediaType.TEXT_EVENT_STREAM)),
										nestedBuilder -> nestedBuilder
												.GET("/events",productHandler::getProductEvents)
												.GET("/{id}",productHandler::getProduct)
												.GET(productHandler::getAllProducts)
												.PUT("/{id}", productHandler::updateProduct)
												.POST(productHandler::saveProduct)
								)
								.DELETE("/{id}", productHandler::deleteProduct)
								.DELETE(productHandler::deleteAllProducts)
				).build();
	}

}
