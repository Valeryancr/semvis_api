package gt.skynet.semvis.config;

import gt.skynet.semvis.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtFilter;

    @Value("${app.security.cors.allowedOrigins:*}")
    private String allowedOrigins;

    public SecurityConfig(JwtAuthFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable());
        http.cors(cors -> cors.configurationSource(request -> {
            CorsConfiguration c = new CorsConfiguration();
            c.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
            c.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
            c.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
            c.setAllowCredentials(true);
            return c;
        }));

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/swagger-resources/**",
                        "/auth/**",
                        "/actuator/health/**",
                        "/reportes/open/**",
                        "/actuator/info/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/catalogos/**").permitAll()

                .requestMatchers("/dashboard/**", "/tendencias/**")
                .hasAnyRole("ADMIN", "SUPERVISOR")

                .requestMatchers("/dashboard/usuario/**")
                .hasAnyRole("ADMIN", "SUPERVISOR", "TECNICO")

                .requestMatchers(HttpMethod.GET, "/visitas/reportes/*/pdf")
                .hasAnyRole("ADMIN", "SUPERVISOR", "TECNICO", "CLIENTE")

                .requestMatchers(HttpMethod.GET, "/visitas/reportes/visita/*")
                .hasAnyRole("ADMIN", "SUPERVISOR", "TECNICO", "CLIENTE")

                .requestMatchers(HttpMethod.GET, "/visitas/cliente/*")
                .hasAnyRole("ADMIN", "SUPERVISOR", "CLIENTE")

                .requestMatchers("/visitas/**")
                .hasAnyRole("ADMIN", "SUPERVISOR", "TECNICO")

                .requestMatchers("/clientes/**")
                .hasAnyRole("ADMIN", "SUPERVISOR", "CLIENTE")

                .requestMatchers(HttpMethod.GET, "/users/*").authenticated()
                .requestMatchers("/users/**")
                .hasAnyRole("ADMIN", "SUPERVISOR")

                .requestMatchers("/reportes/**")
                .authenticated()
                .anyRequest().authenticated()
        );

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration c) throws Exception {
        return c.getAuthenticationManager();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${app.security.cors.allowedOrigins}") String allowed) {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(Arrays.asList(allowed.split(",")));
        cfg.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        cfg.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        cfg.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}