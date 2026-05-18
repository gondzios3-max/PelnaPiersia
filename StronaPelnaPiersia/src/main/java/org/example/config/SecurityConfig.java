package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Wyłączamy CSRF dla uproszczenia (umożliwia komentarze bez tokenów)
            .authorizeHttpRequests((requests) -> requests
                .requestMatchers("/", "/posts", "/css/**", "/js/**", "/images/**", "/uploads/**", "/posts/view/**", "/posts/comment/**", "/posts/react/**", "/favicon.ico").permitAll()
                .requestMatchers("/posts/new", "/posts/edit/**", "/posts/delete/**", "/posts/comment/delete/**", "/posts/stats").authenticated()
                .anyRequest().permitAll() // Zmieniamy na permitAll, aby uniknąć niespodziewanych przekierowań do logowania
            )
            .formLogin((form) -> form
                .loginPage("/login")
                .defaultSuccessUrl("/posts")
                .permitAll()
            )
            .logout((logout) -> logout.permitAll());

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user =
             User.withDefaultPasswordEncoder()
                .username("admin")
                .password("JustynaG32145#")
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(user);
    }
}
