package me.taubsie.dungeonhub.server.security.privilege.model;

import me.taubsie.dungeonhub.common.entity.model.Model;
import me.taubsie.dungeonhub.server.security.group.model.GroupModel;

import java.util.Set;

public record PrivilegeModel(Long id, String name) implements Model {
}