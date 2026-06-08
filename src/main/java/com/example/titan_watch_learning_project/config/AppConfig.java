package com.example.titan_watch_learning_project.config;
import com.example.titan_watch_learning_project.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class AppConfig implements WebMvcConfigurer {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${cors.allowed.origins:http://localhost:5173,https://titan-front-end-nu.vercel.app}")
    private String allowedOriginsCsv;

    public AppConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    private String[] getAllowedOriginsArray() {
        return Arrays.stream(allowedOriginsCsv.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toArray(String[]::new);
    }

    private List<String> getAllowedOriginsList() {
        return Arrays.stream(getAllowedOriginsArray()).toList();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                                // Find this block and add /api/auth/signup:
                                .requestMatchers(
                                        "/api/auth/login",
                                        "/api/auth/signup",    // ← ADD THIS
                                        "/api/public/**",
                                        "/actuator/health"
                                ).permitAll()

                                .requestMatchers("/api/test/**").permitAll()

                        // Karix webhook public - very important
                        .requestMatchers(
                                "/webhook",
                                "/webhook/**",
                                "/api/webhook",
                                "/api/webhook/**"
                        ).permitAll()

                        // Dashboard currently public because login functionality is ignored for now
//                        .requestMatchers(
//                                "/api/dashboard",
//                                "/api/dashboard/**",
//                                "/api/bot-sessions",
//                                "/api/bot-sessions/**",
//                                "/api/leads",
//                                "/api/leads/**",
//                                "/api/auth/me",
//                                "/api/auth/users",
//                                "/api/auth/users/**"
//                        ).hasRole("ADMIN")

                                // NEW:
                                .requestMatchers(
                                        "/api/dashboard",
                                        "/api/dashboard/**",
                                        "/api/bot-sessions",
                                        "/api/bot-sessions/**",
                                        "/api/leads",
                                        "/api/leads/**",
                                        "/api/auth/me",
                                        "/api/auth/users",
                                        "/api/auth/users/**"
                                ).permitAll()

                        // Admin future APIs protected
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Everything else also public for now, so existing functionality does not break
                        .anyRequest().permitAll()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(getAllowedOriginsList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);

        CorsConfiguration webhookConfig = new CorsConfiguration();
        webhookConfig.setAllowedOrigins(List.of("*"));
        webhookConfig.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
        webhookConfig.setAllowedHeaders(List.of("*"));
        webhookConfig.setAllowCredentials(false);

        source.registerCorsConfiguration("/webhook", webhookConfig);
        source.registerCorsConfiguration("/webhook/**", webhookConfig);
        source.registerCorsConfiguration("/api/webhook", webhookConfig);
        source.registerCorsConfiguration("/api/webhook/**", webhookConfig);

        return source;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(getAllowedOriginsArray())
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Authorization");

        registry.addMapping("/webhook")
                .allowedOrigins("*")
                .allowedMethods("POST", "GET", "OPTIONS")
                .allowedHeaders("*");

        registry.addMapping("/webhook/**")
                .allowedOrigins("*")
                .allowedMethods("POST", "GET", "OPTIONS")
                .allowedHeaders("*");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }


    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(4);
        scheduler.setThreadNamePrefix("titan-bot-scheduler-");
        scheduler.initialize();
        return scheduler;
    }
}