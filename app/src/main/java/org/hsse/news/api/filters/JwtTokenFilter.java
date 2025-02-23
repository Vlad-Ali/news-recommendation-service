package org.hsse.news.api.filters;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hsse.news.database.user.models.UserId;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {
    private final SecretKey secretKey = Jwts.SIG.HS256.key().build();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = request.getHeader("Authorization");

        String userName = Jwts.parser().verifyWith(secretKey).build()
                .parseSignedClaims(token).getPayload().getSubject();

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey).build()
                    .parseSignedClaims(token)
                    .getPayload();

            UserId userId = new UserId(UUID.fromString(claims.getSubject()));

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userId, null,
                            List.of(new SimpleGrantedAuthority("ROLE_USER")));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
