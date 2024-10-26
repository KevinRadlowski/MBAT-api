package com.mbat.mbatapi.auth.security.services;

import com.mbat.mbatapi.auth.security.jwt.AuthEntryPointJwt;
import com.mbat.mbatapi.auth.security.jwt.AuthTokenFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableAsync
@EnableGlobalMethodSecurity(
        prePostEnabled = true)
public class WebSecurityConfig implements WebMvcConfigurer {

  @Autowired
  UserDetailsService userDetailsService;

  @Autowired
  private AuthEntryPointJwt unauthorizedHandler;

  @Bean
  public AuthTokenFilter authenticationJwtTokenFilter() {
    return new AuthTokenFilter();
  }

  @Bean
  public DaoAuthenticationProvider authenticationProvider() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
    authProvider.setUserDetailsService(userDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return authProvider;
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
    return authConfig.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
            .cors().and().csrf().disable()
            .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .authorizeHttpRequests(auth -> auth
                    .antMatchers("/api/auth/**", "/oauth2/**", "/api/unlock/**", "/api/user/check-email/**", "/api/user/signup", "/api/user/verify-email", "/api/twofactor/verify-2fa").permitAll()
                    .antMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", "/swagger-resources/**", "/webjars/**", "/swagger.json").permitAll() // Ajout des endpoints Swagger
                    .antMatchers("/api/admin/update/**").hasRole("ROLE_ADMIN") // Protéger la route avec le rôle ADMIN
                    .anyRequest().authenticated()
            )
            .exceptionHandling()
            .authenticationEntryPoint(unauthorizedHandler).and()
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
//            .headers(headers -> headers
//                    .contentSecurityPolicy("default-src 'self'; script-src 'self'; style-src 'self'")
//                    .and()  // Ferme le bloc de configuration pour CSP
//                    .frameOptions().deny()  // Pour prévenir le clickjacking
//                    .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER) // Politique Referrer
//            );
      // TODO : Header à réactiver en production pour utiliser HTTPS
//            .headers()
//            .httpStrictTransportSecurity()
//            .maxAgeInSeconds(31536000)
//            .includeSubDomains(true)
//            .preload(true);

    return http.build();
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
            .allowedOrigins("*") // Permet toutes les origines
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(false); // Désactive la transmission des informations d'authentification
  }

}
