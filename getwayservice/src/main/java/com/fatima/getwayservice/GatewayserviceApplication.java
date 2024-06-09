package com.fatima.getwayservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@SpringBootApplication
@EnableDiscoveryClient
@RestController
public class GatewayserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayserviceApplication.class, args);
	}

	@Bean
	public RouteLocator myRoutes(RouteLocatorBuilder builder, JwtAuthenticationFilter jwtAuthenticationFilter) {
		return builder.routes()
	.route(p -> p
		.path("/api/auth/**")
			.filters(f -> f.circuitBreaker(config -> config
			.setName("authCircuitBreaker")
			.setFallbackUri("forward:/fallback-error")))
						.uri("lb://userservice"))
	.route(p -> p
		.path("/users/**")
			.filters(f -> f
			.filter(jwtAuthenticationFilter)
			.circuitBreaker(config -> config
							.setName("usersCircuitBreaker")
							.setFallbackUri("forward:/fallback-user")).addRequestHeader("Authorization", "Bearer"+JwtAuthenticationFilter.too)).uri("lb://userservice"))
	.route(p -> p
		.path("/posts/**")
			.filters(f -> f
			.filter(jwtAuthenticationFilter)
			.circuitBreaker(config -> config
							.setName("postsCircuitBreaker")
							.setFallbackUri("forward:/fallback-posts")).addRequestHeader("Authorization", "Bearer"+JwtAuthenticationFilter.too)
			.addRequestHeader("Content-Type", "application/json"))
						.uri("lb://postsservice"))
	
	.route(p -> p
		.path("/search/**")
			.filters(f -> f
			.filter(jwtAuthenticationFilter)
			.circuitBreaker(config -> config
							.setName("serchCircuitBreaker")
							.setFallbackUri("forward:/fallback-search")).addRequestHeader("Authorization", "Bearer"+JwtAuthenticationFilter.too))
						.uri("lb://searchservice"))
							.build();
	}

	@RequestMapping("/fallback-user")
	public Mono<String> userFallback() {
		return Mono.just(" Please try again later.");
	}
	@RequestMapping("/fallback-posts")
	public Mono<String> postsFallback() {
		return Mono.just(" Please try again later.");
	}
	@RequestMapping("/fallback-search")
	public Mono<String> searchFallback() {
		return Mono.just(". Please try again later.");
	}
	
	@RequestMapping("/fallback-error")
	public Mono<String> fallback() {
		return Mono.just(" please try again later.");
	}
}