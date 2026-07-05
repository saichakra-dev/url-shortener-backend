package com.urlshortener.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Extract Authorization header
        final String authHeader = request.getHeader("Authorization");

        // 2. If no Bearer token, skip this filter entirely
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extract JWT from header (remove "Bearer " prefix)
        final String jwt = authHeader.substring(7);

        try {
            // 4. Extract username (email) from token
            final String userEmail = jwtService.extractUsername(jwt);

            // 5. Only authenticate if not already authenticated
            if (userEmail != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

                // 6. Load user from database
                UserDetails userDetails =
                        userDetailsService.loadUserByUsername(userEmail);

                // 7. Validate token against loaded user
                if (jwtService.isTokenValid(jwt, userDetails)) {

                    // 8. Create authentication token and set in context
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,              // no credentials needed post-auth
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request)
                    );

                    // 9. Store in SecurityContextHolder (thread-local)
                    SecurityContextHolder.getContext()
                            .setAuthentication(authToken);

                    log.debug("Authenticated user: {}", userEmail);
                }
            }
        } catch (Exception e) {
            log.warn("JWT authentication failed: {}", e.getMessage());
            // Don't throw — let the filter chain continue
            // Spring Security will return 401 automatically
        }

        // 10. Always continue the filter chain
        filterChain.doFilter(request, response);
    }
}