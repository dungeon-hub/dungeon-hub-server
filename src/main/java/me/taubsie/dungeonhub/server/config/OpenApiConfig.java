package me.taubsie.dungeonhub.server.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenApiCustomizer securitySchemeCustomizer() {
        return openApi -> {
            if(openApi.getComponents() == null) {
                openApi.setComponents(new Components());
            }

            openApi.getComponents().addSecuritySchemes(
                    "AccessToken",
                    new SecurityScheme()
                            .type(SecurityScheme.Type.OPENIDCONNECT)
                            .openIdConnectUrl("https://auth.dungeon-hub.net/realms/dungeon-hub/.well-known/openid-configuration")
            );

            openApi.addSecurityItem(
                    new SecurityRequirement()
                            .addList("AccessToken")
            );
        };
    }
}
