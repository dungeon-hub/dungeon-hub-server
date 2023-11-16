package me.taubsie.dungeonhub.server.security.privilege;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.taubsie.dungeonhub.common.DungeonHubService;
import me.taubsie.dungeonhub.common.entity.EntityModelRelation;
import me.taubsie.dungeonhub.server.security.privilege.model.PrivilegeModel;
import me.taubsie.dungeonhub.server.security.privilege.model.SimplePrivilegeModel;

@Getter
@Entity
@Table(name = "privilege", schema = "api", catalog = "api")
@NoArgsConstructor
public class PrivilegeEntity implements EntityModelRelation<PrivilegeModel> {
    @Id
    @GeneratedValue
    @Column
    private Long id;
    @Column
    private String name;

    public PrivilegeEntity(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public SimplePrivilegeModel toSimpleModel() {
        return new SimplePrivilegeModel(getId(), getName());
    }

    @Override
    public PrivilegeEntity fromModel(PrivilegeModel model) {
        return new PrivilegeEntity(model.id(), model.name());
    }

    @Override
    public PrivilegeModel toModel() {
        return new PrivilegeModel(id, name);
    }

    @Override
    public String toString() {
        return DungeonHubService.getInstance()
                .getGson()
                .toJson(this);
    }
}