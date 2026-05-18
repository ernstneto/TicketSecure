package com.ticketsecure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Desabilita a proteção CSRF, que é o que está causando o erro 403 nos métodos POST
            .csrf(csrf -> csrf.disable())
            
            // Libera todas as rotas temporariamente para testarmos o nosso fluxo do RabbitMQ
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() 
            );
        
        return http.build();
    }
}