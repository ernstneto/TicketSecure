package com.ticketsecure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Permite POST para criar conta (com e sem barra final)
                .requestMatchers(HttpMethod.POST, "/api/users", "/api/users/").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/events", "/api/events/").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/ticket-lots", "/api/ticket-lots/").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/reserves/**").permitAll()
                // Permite GET para buscar a conta
                .requestMatchers(HttpMethod.GET, "/api/users/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/events/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/ticket-lots/**").permitAll()
                // MÁGICA: Permite que o Spring mostre os erros verdadeiros de validação!
                .requestMatchers("/error").permitAll()
                // Tudo o resto está bloqueado
                .anyRequest().authenticated()
            );
        
        return http.build();
    }
}