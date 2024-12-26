package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dungeonhub.enums.RoleRequirementComparison;
import net.dungeonhub.enums.RoleRequirementType;
import net.dungeonhub.model.role_requirement.RoleRequirementModel;
import org.jetbrains.annotations.NotNull;

@Entity(name = "role_requirement")
@Table(name = "role_requirement", schema = "dungeon-hub")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class RoleRequirement implements net.dungeonhub.structure.entity.Entity<RoleRequirementModel> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "discord_role", nullable = false)
    private DiscordRole discordRole;

    @Enumerated
    @Column(name = "requirement_type", nullable = false)
    private RoleRequirementType requirementType;

    @Enumerated
    @Column(name = "comparison", nullable = false)
    @Setter
    private RoleRequirementComparison comparison;

    @Column(name = "count", nullable = false)
    @Setter
    private int count;

    @Column(name = "extra_data")
    @Setter
    private String extraData;

    public RoleRequirement(DiscordRole discordRole, RoleRequirementType requirementType, RoleRequirementComparison comparison, int count, String extraData) {
        this.discordRole = discordRole;
        this.requirementType = requirementType;
        this.comparison = comparison;
        this.count = count;
        this.extraData = extraData;
    }

    @NotNull
    @Override
    public RoleRequirementModel toModel() {
        return new RoleRequirementModel(id, discordRole.toModel(), requirementType, comparison, count, extraData);
    }
}