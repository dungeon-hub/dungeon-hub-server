package me.taubsie.dungeonhub.server.security.privilege.model;

import me.taubsie.dungeonhub.common.entity.model.Model;

public record PrivilegeModel(Long id, String name) implements Model {
}