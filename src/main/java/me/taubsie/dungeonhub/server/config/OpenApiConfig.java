package me.taubsie.dungeonhub.server.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenApiCustomizer permissionsSchemaCustomizer() {
        return openApi -> {
            IntegerSchema permissionsSchema = new IntegerSchema();
            permissionsSchema.setFormat("int64");
            if(openApi.getComponents() == null) {
                openApi.setComponents(new Components());
            }
            if(openApi.getComponents().getSchemas() == null) {
                openApi.getComponents().setSchemas(new LinkedHashMap<>());
            }
            openApi.getComponents().getSchemas().put("Permissions", permissionsSchema);

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
