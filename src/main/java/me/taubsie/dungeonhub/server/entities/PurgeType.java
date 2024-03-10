package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.taubsie.dungeonhub.common.entity.EntityModelRelation;
import me.taubsie.dungeonhub.common.model.purge_type.PurgeTypeModel;
import org.hibernate.annotations.OnDelete;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Getter
@Entity(name = "purge_type")
@Table(name = "purge_type", schema = "dungeon-hub")
@AllArgsConstructor
@NoArgsConstructor
public class PurgeType implements EntityModelRelation<PurgeTypeModel> {
    @Id
    @Column(name = "id", nullable = false)
    //final
    private long id;

    @Column(nullable = false)
    //final
    private String identifier;
    @Column(name = "display_name", nullable = false, length = 50)
    private String displayName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    @JoinColumn(name = "carry_type", nullable = false)
    //final
    private CarryType carryType;

    @OneToMany(mappedBy = "purgeType")
    private List<PurgeTypeRole> purgeTypeRoles;

    public PurgeType(String identifier, String displayName, CarryType carryType) {
        this.identifier = identifier;
        this.displayName = displayName;
        this.carryType = carryType;
    }

    public PurgeType(long id, String identifier, String displayName, CarryType carryType) {
        this.id = id;
        this.identifier = identifier;
        this.displayName = displayName;
        this.carryType = carryType;
    }

    @Override
    public @NotNull PurgeType fromModel(@NotNull PurgeTypeModel model) {
        return new PurgeType(model.getId(), model.getIdentifier(), model.getDisplayName(), carryType.fromModel(model.getCarryType()));
    }

    @Override
    public @NotNull PurgeTypeModel toModel() {
        return new PurgeTypeModel(id, identifier, displayName, carryType.toModel(), purgeTypeRoles.stream().map(PurgeTypeRole::toModel).toList());
    }
}