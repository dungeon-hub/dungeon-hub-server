package me.taubsie.dungeonhub.server.security.group;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.taubsie.dungeonhub.common.DungeonHubService;
import me.taubsie.dungeonhub.common.entity.EntityModelRelation;
import me.taubsie.dungeonhub.server.security.group.model.GroupModel;
import me.taubsie.dungeonhub.server.security.privilege.PrivilegeEntity;
import me.taubsie.dungeonhub.server.security.user.UserEntity;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Entity
@Table(name = "group", schema = "api", catalog = "api")
@Getter
@NoArgsConstructor
public class GroupEntity implements EntityModelRelation<GroupModel> {
    @Id
    @GeneratedValue
    private long id;
    private String name;

    @ManyToMany(mappedBy = "groups")
    @JsonIgnore
    private Set<UserEntity> users;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "group_privileges", schema = "api", catalog = "api", joinColumns = @JoinColumn(name = "group_id",
            referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "privilege_id",
            referencedColumnName = "id"))
    private Set<PrivilegeEntity> privileges;

    public GroupEntity(long id, String name, Set<PrivilegeEntity> privileges) {
        this.id = id;
        this.name = name;
        this.privileges = privileges;
    }

    @Override
    public GroupEntity fromModel(GroupModel model) {
        return new GroupEntity(model.id(), model.name(),
                model.privileges().stream().map(privilege -> new PrivilegeEntity(privilege.id(), privilege.name())).collect(Collectors.toSet()));
    }

    @Override
    public GroupModel toModel() {
        return new GroupModel(id, name,
                users.stream().map(UserEntity::toSimpleModel).collect(Collectors.toSet()),
                privileges.stream().map(PrivilegeEntity::toSimpleModel).collect(Collectors.toSet()));
    }

    public boolean grantPrivilege(PrivilegeEntity... privilegeEntities) {
        Predicate<PrivilegeEntity> privilegeEntityPredicate = requestedPrivilege -> privileges.stream()
                .noneMatch(privilegeEntity -> Objects.equals(privilegeEntity, requestedPrivilege));
        return privileges.addAll(Arrays.stream(privilegeEntities).filter(privilegeEntityPredicate).collect(Collectors.toSet()));
    }

    public boolean revokePrivilege(Long... id) {
        Collection<Long> revokeIds = Arrays.asList(id);
        return privileges.removeIf(privilege -> revokeIds.contains(privilege.getId()));
    }

    public Set<PrivilegeEntity> getPrivileges() {
        return Collections.unmodifiableSet(privileges);
    }

    @Override
    public String toString() {
        return DungeonHubService.getInstance()
                .getGson()
                .toJson(this);
    }
}