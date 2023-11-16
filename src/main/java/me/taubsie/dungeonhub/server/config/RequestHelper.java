package me.taubsie.dungeonhub.server.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

@Component
@AllArgsConstructor
public class RequestHelper {
    private final HttpServletRequest httpServletRequest;

    public Object getPathVariable(String name) {
        final Map<String, Object> pathVariables = (Map<String, Object>) httpServletRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        return pathVariables.get(name);
    }
}