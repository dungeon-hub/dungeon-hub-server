package me.taubsie.dungeonhub.server.security.group.model;

import me.taubsie.dungeonhub.common.entity.model.Model;
import me.taubsie.dungeonhub.server.security.privilege.model.SimplePrivilegeModel;
import me.taubsie.dungeonhub.common.model.security.user.SimpleUserModel;

import java.util.Set;

public record GroupModel(Long id, String name, Set<SimpleUserModel> users,
                         Set<SimplePrivilegeModel> privileges) implements Model {
}
