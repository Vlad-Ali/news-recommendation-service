package org.hsse.news.api.filters;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hsse.news.database.jwt.JwtService;
import org.hsse.news.database.role.RolesService;
import org.hsse.news.database.role.model.Role;
import org.hsse.news.database.user.models.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@AllArgsConstructor
@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {
    private JwtService jwtService;
    private RolesService rolesService;
    private static final Logger LOG = LoggerFactory.getLogger(JwtTokenFilter.class);

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request,
            final HttpServletResponse response, final FilterChain filterChain)
            throws ServletException, IOException {
        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);

            try {
                final UserId userId = jwtService.getUserId(token);
                final Set<Role> roles = rolesService.getUserRoles(userId);
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                for (final Role role : roles){
                    authorities.add(new SimpleGrantedAuthority(role.name()));
                }
                final UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId, null,
                                authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                LOG.warn("Failed to parse JWT token: {}", e.toString());
            }
        }

        filterChain.doFilter(request, response);
    }
}
