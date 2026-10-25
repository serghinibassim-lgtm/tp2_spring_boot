package com.bassim.tp2_securPharmacie.config;

import com.bassim.tp2_securPharmacie.Services.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize

                        .requestMatchers(
                                "/login",
                                "/login.html",
                                "/access-denied",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/favicon.ico",
                                "/error"
                        ).permitAll()

                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/produits").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/produits/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/produits/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.PATCH, "/api/produits/**")
                        .hasAnyRole("ADMIN", "PHARMACIEN")
                        .requestMatchers(HttpMethod.POST, "/api/ventes")
                        .hasAnyRole("ADMIN", "PHARMACIEN", "CAISSIER")
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

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            if (request.getRequestURI().startsWith("/api/")) {
                                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                                response.setCharacterEncoding("UTF-8");
                                String message = accessDeniedException instanceof CsrfException
                                        ? "Jeton CSRF invalide ou manquant. Rechargez la page et réessayez."
                                        : "Accès refusé : votre rôle ne permet pas cette opération.";
                                response.getWriter().write(
                                        "{\"message\":\"" + message + "\"}");
                            } else {
                                response.sendRedirect(
                                        request.getContextPath() + "/access-denied");
                            }
                        })
                )
                .httpBasic(Customizer.withDefaults());

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
