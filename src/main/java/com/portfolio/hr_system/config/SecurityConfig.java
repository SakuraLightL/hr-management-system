package com.portfolio.hr_system.config;

import com.portfolio.hr_system.service.CustomOidcUserService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CustomOidcUserService oidc,
            ObjectProvider<ClientRegistrationRepository> registrations) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/error", "/css/**", "/js/**").permitAll()
                .requestMatchers("/users/**", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").hasRole("ADMIN")
                .requestMatchers("/employees/new", "/employees/edit/**", "/employees/delete/**",
                        "/departments/new", "/departments/edit/**", "/departments/delete/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/**", "/employees/**", "/departments/**").authenticated()
                .requestMatchers(HttpMethod.HEAD, "/api/**", "/employees/**", "/departments/**").authenticated()
                .requestMatchers("/api/**", "/employees/**", "/departments/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .formLogin(login -> login.loginPage("/login").defaultSuccessUrl("/dashboard", true).permitAll())
            .logout(logout -> logout.logoutSuccessUrl("/login?logout").permitAll())
            .exceptionHandling(errors -> errors.defaultAuthenticationEntryPointFor(
                    (request, response, exception) -> {
                        response.setStatus(401);
                        response.setContentType("application/json;charset=UTF-8");
                        response.getWriter().write("{\"status\":\"error\",\"message\":\"ログインが必要です\",\"data\":null}");
                    }, new AntPathRequestMatcher("/api/**")));
        // The REST API uses the same session cookies as MVC, so CSRF stays enabled for both.
        if (registrations.getIfAvailable() != null) {
            http.oauth2Login(oauth -> oauth.loginPage("/login")
                    .userInfoEndpoint(info -> info.oidcUserService(oidc))
                    .defaultSuccessUrl("/dashboard", true));
        }
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}
