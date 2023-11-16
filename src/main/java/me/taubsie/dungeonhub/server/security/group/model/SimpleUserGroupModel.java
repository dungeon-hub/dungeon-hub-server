package me.taubsie.dungeonhub.server.security.group.model;

import io.swagger.v3.oas.annotations.media.Schema;
import me.taubsie.dungeonhub.common.entity.model.SimpleModel;
import me.taubsie.dungeonhub.server.security.privilege.model.SimplePrivilegeModel;

import java.util.Set;

@Schema(name = "group")
public record SimpleUserGroupModel(long id, String name, Set<SimplePrivilegeModel> privileges) implements SimpleModel {
}