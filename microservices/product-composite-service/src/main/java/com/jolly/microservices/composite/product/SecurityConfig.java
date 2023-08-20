package com.jolly.microservices.composite.product;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * @author jolly
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
//    @Order(Ordered.HIGHEST_PRECEDENCE)
//    @Bean
//    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) throws Exception {
//        http
//                .authorizeExchange(exchanges ->
//                        exchanges
//                            .pathMatchers("/openapi/**").permitAll()
//                            .pathMatchers("/webjars/**").permitAll()
//                            .pathMatchers("/actuator/**").permitAll()
//                            .pathMatchers(HttpMethod.POST, "/product-composite/**").hasAuthority("SCOPE_product:write")
//                            .pathMatchers(HttpMethod.DELETE, "/product-composite/**").hasAuthority("SCOPE_product:write")
//                            .pathMatchers(HttpMethod.GET, "/product-composite/**").hasAuthority("SCOPE_product:read")
//                            .anyExchange().authenticated()
//                )
//                .oauth2ResourceServer(rs -> rs.jwt(Customizer.withDefaults()));
//        return http.build();
//    }

    @Order(Ordered.HIGHEST_PRECEDENCE)
    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange((authorize) -> authorize
                        .pathMatchers("/openapi/**").permitAll()
                        .pathMatchers("/webjars/**").permitAll()
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/product-composite/**").hasAuthority("SCOPE_product:write")
                        .pathMatchers(HttpMethod.DELETE, "/product-composite/**").hasAuthority("SCOPE_product:write")
                        .pathMatchers(HttpMethod.GET, "/product-composite/**").hasAuthority("SCOPE_product:read")
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer((resourceServer) -> resourceServer
                        .jwt(withDefaults())
                );
        return http.build();
    }
}
