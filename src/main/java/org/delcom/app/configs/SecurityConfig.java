package org.delcom.app.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> {
                    // Mencegah redirect loop jika request adalah aset statis
                    String path = req.getRequestURI();
                    if (path.startsWith("/css") || path.startsWith("/js") || path.startsWith("/assets")) {
                        res.setStatus(404);
                    } else {
                        res.sendRedirect("/auth/login");
                    }
                }))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/auth/**", 
                    "/assets/**", 
                    "/api/**",
                    "/css/**", 
                    "/js/**", 
                    "/error", 
                    "/favicon.ico", 
                    "/uploads/**" 
                )
                .permitAll()
                .anyRequest().authenticated())

            .formLogin(form -> form.disable())
            .logout(logout -> logout
                .logoutUrl("/auth/logout") 
                .logoutSuccessUrl("/auth/login")
                .invalidateHttpSession(true) 
                .deleteCookies("JSESSIONID") 
                .permitAll())
            .rememberMe(remember -> remember
                .key("uniqueAndSecret")
                .tokenValiditySeconds(86400)
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}