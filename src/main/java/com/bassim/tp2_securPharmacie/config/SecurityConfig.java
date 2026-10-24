package com.bassim.tp2_securPharmacie.config;

import com.bassim.tp2_securPharmacie.Services.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(Customizer.withDefaults())
                .formLogin(Customizer.withDefaults())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/produits",
                                "/api/produits/**",
                                "/api/categories",
                                "/api/categories/**",
                                "/api/fournisseurs",
                                "/api/fournisseurs/**",
                                "/api/tags",
                                "/api/tags/**"
                        ).hasAnyRole("ADMIN", "PHARMACIEN", "CAISSIER", "CLIENT")
                        .requestMatchers(HttpMethod.POST, "/api/produits")
                        .hasAnyRole("ADMIN", "PHARMACIEN")
                        .requestMatchers(HttpMethod.PUT, "/api/produits/**")
                        .hasAnyRole("ADMIN", "PHARMACIEN")
                        .requestMatchers(HttpMethod.DELETE, "/api/produits/**")
                        .hasRole("ADMIN")
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }


}