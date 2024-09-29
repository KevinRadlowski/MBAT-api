package com.mbat.mbatapi.auth;

import com.mbat.mbatapi.entity.User;
import com.mbat.mbatapi.repository.UserRepository;
import com.mbat.mbatapi.security.services.UserDetailsServiceImpl;
import com.mbat.mbatapi.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {


    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Identifiant ou mot de passe incorrect.");
    }

}