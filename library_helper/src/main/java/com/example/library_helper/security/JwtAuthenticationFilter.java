package com.example.library_helper.security;

import com.example.library_helper.exception.UnauthorizedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filter that authenticates requests using Bearer JWT tokens.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenService jwtTokenService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length()).trim();

            // Strip redundant "Bearer " prefixes if caller sent "Bearer Bearer ..."
            while (token.startsWith(BEARER_PREFIX)) {
                token = token.substring(BEARER_PREFIX.length()).trim();
            }

            // Strip accidental quotes if caller sent "\"eyJ...\""
            if (token.startsWith("\"") && token.endsWith("\"") && token.length() > 1) {
                token = token.substring(1, token.length() - 1).trim();
            }

            if (!StringUtils.hasText(token)) {
                log.warn("Bearer token is empty in request to {}", request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }

            if (token.startsWith("{{") && token.endsWith("}}")) {
                log.warn("Bearer token is an unresolved Postman/client placeholder variable: '{}' in request to {}",
                        token, request.getRequestURI());
            }

            try {
                String loginId = jwtTokenService.getLoginId(token);
                List<String> roles = jwtTokenService.getRoles(token);

                if (StringUtils.hasText(loginId) && SecurityContextHolder.getContext().getAuthentication() == null) {
                    List<SimpleGrantedAuthority> authorities = roles.stream()
                            .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(loginId, null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Authenticated user '{}' with authorities: {}", loginId, authorities);
                }
            } catch (UnauthorizedException e) {
                log.warn("Unauthorized request to {}: {}", request.getRequestURI(), e.getMessage());
            } catch (Exception e) {
                log.error("Failed to authenticate JWT token: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
