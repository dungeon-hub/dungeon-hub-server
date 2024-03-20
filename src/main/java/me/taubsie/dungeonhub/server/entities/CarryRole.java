package me.taubsie.dungeonhub.server.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import me.taubsie.dungeonhub.common.entity.EntityModelRelation;
import me.taubsie.dungeonhub.common.model.carryrole.CarryRoleModel;
import me.taubsie.dungeonhub.common.model.carryrolerequirement.CarryRoleRequirementModel;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@Entity
@Getter
@Setter
public class CarryRole implements EntityModelRelation<CarryRoleModel> {

    @Id
    @GeneratedValue
    @Setter(AccessLevel.NONE) // id is final
    private long id;
    @ManyToOne(optional = false) @JoinColumn(name = "discord_role_id", referencedColumnName = "id", nullable = false)
    private DiscordRole discordRole;
    private String displayName;
    @JsonBackReference // TODO discuss
    @OneToMany(mappedBy = "role")
    private Set<CarryRoleRequirement> requirements;
    private boolean manual; // if manual approval is required
    private boolean enabled;

    @Override
    public @NotNull CarryRole fromModel(@NotNull CarryRoleModel model) {
        CarryRole carryRole = new CarryRole();
        carryRole.setDiscordRole(discordRole.fromModel(model.getDiscordRole()));
        carryRole.setDisplayName(model.getDisplayName());
        carryRole.setManual(model.isManual());
        carryRole.setEnabled(model.isEnabled());
        return carryRole;
    }

    @Override
    public @NotNull CarryRoleModel toModel() {
        CarryRoleRequirementModel[] requirements = getRequirements().stream().map(CarryRoleRequirement::toModel).toArray(CarryRoleRequirementModel[]::new);
        return new CarryRoleModel(id, getDiscordRole().toModel(), getDisplayName(), requirements, isManual(), isEnabled());
    }
}
