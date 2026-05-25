package me.taubsie.dungeonhub.server.config;

import io.swagger.v3.oas.models.media.IntegerSchema;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenApiCustomizer permissionsSchemaCustomizer() {
        return openApi -> {
            IntegerSchema permissionsSchema = new IntegerSchema();
            permissionsSchema.setFormat("int64");
            openApi.getComponents().getSchemas().put("Permissions", permissionsSchema);
        };
    }
}
