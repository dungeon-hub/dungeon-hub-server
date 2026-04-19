package me.taubsie.dungeonhub.server.entities;

import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dungeonhub.model.purge_type.PurgeTypeModel;
import net.dungeonhub.model.purge_type.SimplePurgeTypeModel;
import org.hibernate.annotations.OnDelete;
import org.jetbrains.annotations.NotNull;

@Getter
@Entity(name = "purge_type")
@Table(name = "purge_type", schema = "dungeon-hub")
@AllArgsConstructor
@NoArgsConstructor
public class PurgeType implements net.dungeonhub.structure.entity.Entity<PurgeTypeModel> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    //final
    private long id;

    @Column(nullable = false)
    //final
    private String identifier;

    @Setter
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
    public @NotNull PurgeTypeModel toModel() {
        return new PurgeTypeModel(id, identifier, displayName, carryType.toModel(), purgeTypeRoles.stream().map(PurgeTypeRole::toModel).toList());
    }

    public @NotNull SimplePurgeTypeModel toSimpleModel() {
        return new SimplePurgeTypeModel(id, identifier, displayName, carryType.toModel());
    }
}