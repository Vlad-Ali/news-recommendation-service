package org.hsse.news.api.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Locale;

@Component
@Slf4j
public class LogRequestFilter extends OncePerRequestFilter {
    private static final Logger LOG = LoggerFactory.getLogger(LogRequestFilter.class);
    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain filterChain)
            throws ServletException, IOException {
        LOG.info(
                "{} - {} {}",
                request.getRemoteAddr(),
                request.getMethod().toUpperCase(Locale.ROOT),
                request.getRequestURI()
        );

        filterChain.doFilter(request, response);
    }
}
