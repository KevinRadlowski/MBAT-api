// package com.mbat.mbatapi.auth;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.web.cors.CorsConfiguration;

// @Configuration
// public class SecurityConfig {

//     @Bean
//     public SecurityFilterChain filterChain(HttpSecurity http) throws Exception
//     {
//         http.httpBasic();

//         http.cors().configurationSource(request -> corsConfiguration());

//         http.authorizeRequests()
//             .mvcMatchers("/api/user").permitAll()
//             .mvcMatchers("/api/admin/*").hasAuthority("ROLE_ADMIN")
//             .anyRequest().authenticated()
//             .and().csrf().disable()
//             .logout().logoutSuccessHandler((req,res,auth) -> {
//                 res.setStatus(204);
//             });

//         return http.build();
//     }

//     @Bean
//     public PasswordEncoder passwordEncoder() {
//         return new BCryptPasswordEncoder(12);
//     }

//     private CorsConfiguration corsConfiguration() {
//         CorsConfiguration configuration = new CorsConfiguration();
//         configuration.setAllowCredentials(true);
        
//         configuration.addAllowedOrigin("http://localhost:4200");
//         configuration.addAllowedHeader("*");
//         configuration.addAllowedMethod("*");
        
//         return configuration;
//     }
    
    
// }
