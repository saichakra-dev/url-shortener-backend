package com.urlshortener.service.impl;

import com.urlshortener.domain.entity.User;
import com.urlshortener.domain.enums.Role;
import com.urlshortener.dto.request.LoginRequest;
import com.urlshortener.dto.request.RegisterRequest;
import com.urlshortener.dto.response.AuthResponse;
import com.urlshortener.exception.DuplicateResourceException;
import com.urlshortener.exception.ResourceNotFoundException;
import com.urlshortener.repository.UserRepository;
import com.urlshortener.security.JwtService;
import com.urlshortener.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already registered");
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.ROLE_USER)
                .build();

                User savedUser = userRepository.save(user);  // capture returned entity

                UserDetails userDetails = buildUserDetails(savedUser);  // use savedUser
                String token = jwtService.generateToken(userDetails);
                
                log.info("User registered: {}", savedUser.getEmail());
                return AuthResponse.of(token, savedUser.getEmail(), savedUser.getRole().name());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()));

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + request.email()));

        UserDetails userDetails = buildUserDetails(user);
        String token = jwtService.generateToken(userDetails);

        log.info("User logged in: {}", user.getEmail());
        return AuthResponse.of(token, user.getEmail(), user.getRole().name());
    }

    private UserDetails buildUserDetails(User user) {
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(List.of(
                        new SimpleGrantedAuthority(user.getRole().name())))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.isEnabled())
                .build();
    }
}
