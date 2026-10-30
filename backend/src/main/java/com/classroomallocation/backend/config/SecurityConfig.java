package com.classroomallocation.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import com.classroomallocation.backend.security.LoginRateLimiter;
import com.classroomallocation.backend.security.LoginRateLimitFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

        @Bean
        public PasswordEncoder passwordEncoder() {
                return PasswordEncoderFactories.createDelegatingPasswordEncoder();
        }

        @Bean
        public SessionRegistry sessionRegistry() {
                return new SessionRegistryImpl();
        }

        @Bean
        public HttpSessionEventPublisher httpSessionEventPublisher() {
                return new HttpSessionEventPublisher();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(
                        HttpSecurity http,
                        LoginRateLimiter loginRateLimiter,
                        SessionRegistry sessionRegistry) throws Exception {

                http
                                .csrf(Customizer.withDefaults())

                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                org.springframework.http.HttpMethod.GET,
                                                                "/api/health")
                                                .permitAll()
                                                .requestMatchers(
                                                                org.springframework.http.HttpMethod.GET,
                                                                "/",
                                                                "/index.html",
                                                                "/assets/**",
                                                                "/favicon.svg",
                                                                "/classroom-logo.jpeg",
                                                                "/login",
                                                                "/forgot-password",
                                                                "/reset-password",
                                                                "/dashboard",
                                                                "/change-password",
                                                                "/users",
                                                                "/classrooms",
                                                                "/teachers",
                                                                "/subjects",
                                                                "/sections",
                                                                "/time-slots",
                                                                "/requirements",
                                                                "/allocations",
                                                                "/allocation-drafts",
                                                                "/student-section-assignments",
                                                                "/my-timetable")
                                                .permitAll()

                                                // Public endpoint: obtain a CSRF token.
                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/auth/csrf")
                                                .permitAll()

                                                // Login is handled by Spring Security.
                                                // CSRF protection still applies.
                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/auth/login")
                                                .permitAll()

                                                .requestMatchers("/login", "/error").permitAll()

                                                // Every signed-in user can access their own account.
                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/api/auth/me")
                                                .authenticated()

                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/auth/logout")
                                                .authenticated()

                                                // Administration.
                                                .requestMatchers(
                                                                "/api/admin/**",
                                                                "/api/users/**",
                                                                "/api/audit-logs/**")
                                                .hasRole("ADMIN")

                                                // Campus planning and management.
                                                .requestMatchers(
                                                                "/api/classrooms/**",
                                                                "/api/teachers/**",
                                                                "/api/subjects/**",
                                                                "/api/sections/**",
                                                                "/api/time-slots/**",
                                                                "/api/requirements/**",
                                                                "/api/allocations/**",
                                                                "/api/timetables/**",
                                                                "/api/reports/**")
                                                .hasAnyRole("ADMIN", "COORDINATOR")

                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/auth/change-password")
                                                .authenticated()

                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/api/auth/forgot-password",
                                                                "/api/auth/reset-password")
                                                .permitAll()

                                                .requestMatchers("/api/classrooms/**")
                                                .hasAnyRole("ADMIN", "COORDINATOR")

                                                // Specific account-linking permission first.
                                                .requestMatchers(
                                                                HttpMethod.PUT,
                                                                "/api/teachers/*/account")
                                                .hasRole("ADMIN")

                                                // General teacher-management permission afterwards.
                                                .requestMatchers("/api/teachers/**")
                                                .hasAnyRole("ADMIN", "COORDINATOR")

                                                .requestMatchers("/api/subjects/**")
                                                .hasAnyRole("ADMIN", "COORDINATOR")

                                                .requestMatchers("/api/sections/**")
                                                .hasAnyRole("ADMIN", "COORDINATOR")
                                                .requestMatchers("/api/time-slots/**")
                                                .hasAnyRole("ADMIN", "COORDINATOR")
                                                .requestMatchers("/api/requirements/**")
                                                .hasAnyRole("ADMIN", "COORDINATOR")
                                                .requestMatchers("/api/allocations/**")
                                                .hasAnyRole("ADMIN", "COORDINATOR")

                                                .requestMatchers("/api/allocation-drafts/**")
                                                .hasAnyRole("ADMIN", "COORDINATOR")

                                                .requestMatchers("/api/student-section-assignments/**")
                                                .hasRole("ADMIN")

                                                .requestMatchers("/api/student/me/**")
                                                .hasRole("STUDENT")

                                                .requestMatchers("/api/teacher/me/**")
                                                .hasRole("TEACHER")

                                                .requestMatchers("/api/allocation-drafts/**")
                                                .hasAnyRole("ADMIN", "COORDINATOR")
                                                .requestMatchers("/api/timetable-publications/**")
                                                .hasAnyRole("ADMIN", "COORDINATOR")
                                                .requestMatchers("/api/me/timetable")
                                                .hasAnyRole("TEACHER", "STUDENT")

                                                // Unlisted routes require an explicit permission rule.
                                                .anyRequest().denyAll())

                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .loginProcessingUrl("/api/auth/login")
                                                .usernameParameter("email")
                                                .passwordParameter("password")
                                                .successHandler((request, response, authentication) -> response
                                                                .setStatus(204))
                                                .failureHandler((request, response, exception) -> response
                                                                .setStatus(401))
                                                .permitAll())

                                .logout(logout -> logout
                                                .logoutUrl("/api/auth/logout")
                                                .invalidateHttpSession(true)
                                                .clearAuthentication(true)
                                                .deleteCookies("JSESSIONID")
                                                .logoutSuccessHandler(
                                                                (request, response, authentication) -> response
                                                                                .setStatus(204)))

                                .exceptionHandling(errors -> errors
                                                .authenticationEntryPoint(
                                                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))

                                .requestCache(AbstractHttpConfigurer::disable)
                                .httpBasic(AbstractHttpConfigurer::disable);
                http.addFilterBefore(
                                new LoginRateLimitFilter(loginRateLimiter),
                                UsernamePasswordAuthenticationFilter.class);

                http.sessionManagement(session -> session
                                .maximumSessions(-1)
                                .sessionRegistry(sessionRegistry)
                                .expiredSessionStrategy(event -> {
                                        event.getResponse().setStatus(401);
                                        event.getResponse().setHeader("Cache-Control", "no-store");
                                }));

                return http.build();
        }
}