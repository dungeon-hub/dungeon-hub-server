package me.taubsie.dungeonhub.server.security.encryption;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.server.security.user.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Order(1)
@AllArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        String prefix = "Bearer ";

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(prefix)) {
            try {
                String token = header.substring(prefix.length());
                userService.validate(token).ifPresent((usernamePasswordAuthenticationToken ->
                        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken)));
            }
            catch (HttpClientErrorException httpClientErrorException) {
                response.setStatus(httpClientErrorException.getStatusCode().value());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}