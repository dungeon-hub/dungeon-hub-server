package me.taubsie.dungeonhub.server.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import me.taubsie.dungeonhub.common.entity.EntityModelRelation;
import me.taubsie.dungeonhub.common.model.carryrolerequirement.CarryRoleRequirementModel;
import me.taubsie.dungeonhub.common.model.carryrolerequirement.RequirementType;
import org.jetbrains.annotations.NotNull;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class CarryRoleRequirement implements EntityModelRelation<CarryRoleRequirementModel> {

    @Id
    @Setter(AccessLevel.NONE)
    private long id;
    @ManyToOne(optional = false)
    @JsonManagedReference // TODO discuss
    @JoinColumn(name = "role_id", referencedColumnName = "id", nullable = false)
    private CarryRole role;
    @Enumerated //ordinal
    private RequirementType type;
    private String textValue;
    private long value;

    @Override
    public @NotNull CarryRoleRequirement fromModel(@NotNull CarryRoleRequirementModel model) {
        return new CarryRoleRequirement(model.getId(), role.fromModel(model.getCarryRoleModel()), model.getType(), model.getTextValue(), model.getValue());
    }

    @Override
    public @NotNull CarryRoleRequirementModel toModel() {
        return new CarryRoleRequirementModel(getId(), getRole().toModel(), getType(), getTextValue(), getValue());
    }
}
