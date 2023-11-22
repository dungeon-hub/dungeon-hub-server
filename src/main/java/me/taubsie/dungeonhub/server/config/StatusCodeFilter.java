package me.taubsie.dungeonhub.server.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class StatusCodeFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
                                    FilterChain filterChain) throws IOException {
        try {
            filterChain.doFilter(request, response);
        }
        catch (ServletException exception) {
            if (exception.getCause() != null && exception.getCause() instanceof HttpStatusCodeException httpStatusCodeException) {
                response.setStatus(httpStatusCodeException.getStatusCode().value());
            } else {
                //Added from the true, one and only genius @js12345
                //He's like: https://www.youtube.com/watch?v=1C8z2KAnP9A
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                logger.error("Error caught, sent a response with code 500.", exception);
            }
        }
    }
}