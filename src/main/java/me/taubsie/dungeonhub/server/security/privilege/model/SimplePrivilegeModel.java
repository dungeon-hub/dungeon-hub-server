package me.taubsie.dungeonhub.server.security.privilege.model;

import io.swagger.v3.oas.annotations.media.Schema;
import me.taubsie.dungeonhub.common.entity.model.SimpleModel;

@Schema(name = "privilege")
public record SimplePrivilegeModel(long id, String name) implements SimpleModel {
}