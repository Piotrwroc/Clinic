package com.clinic.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.util.StringUtils; // Dodaj ten import

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filtr Spring Security, który jest wykonywany raz na każde żądanie HTTP.
 * Odpowiada za ekstrakcję i walidację tokena JWT z nagłówka autoryzacji.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, UserDetailsServiceImpl userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // 1. Spróbuj pobrać token JWT z żądania
            String jwt = getJwtFromRequest(request);

            // 2. Jeśli token istnieje i jest poprawny
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                // 3. Pobierz email użytkownika z tokena (teraz już bezpiecznie, bo token został zweryfikowany)
                String userEmail = tokenProvider.getUserEmailFromJWT(jwt); // <-- Nowa/zmodyfikowana metoda w JwtTokenProvider

                // 4. Załaduj szczegóły użytkownika i ustaw uwierzytelnienie w kontekście bezpieczeństwa
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            System.err.println("Could not set user authentication in security context: " + ex.getMessage());
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Ekstrahuje token JWT z nagłówka Authorization żądania.
     * @param request Żądanie HTTP.
     * @return Token JWT lub null, jeśli nie znaleziono.
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Usuń "Bearer "
        }
        return null;
    }
}