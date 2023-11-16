package me.taubsie.dungeonhub.server.security.user.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import me.taubsie.dungeonhub.common.entity.model.Model;
import me.taubsie.dungeonhub.server.security.group.model.SimpleUserGroupModel;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class UserModel implements Model {
    private long id;
    private String loginName;
    private boolean enabled;
    private Set<SimpleUserGroupModel> groups;
}