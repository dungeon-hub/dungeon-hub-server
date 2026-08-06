package me.taubsie.dungeonhub.server.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dungeonhub.model.carry_type.CarryTypeModel;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity(name = "carry_type")
@Table(name = "carry_type", schema = "dungeon-hub")
@NoArgsConstructor
public class CarryType implements net.dungeonhub.structure.entity.Entity<CarryTypeModel> {
    @Getter
    @OneToMany(mappedBy = "carryType")
    @JsonIgnore
    private final Set<CarryTier> carryTiers = new LinkedHashSet<>();

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    //final
    private long id;

    @Getter
    @Column(nullable = false)
    //final
    private String identifier;
    @Getter
    @Setter
    @Column(name = "display_name", nullable = false, length = 50)
    private String displayName;

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "server", nullable = false)
    //final
    private DiscordServer discordServer;
    @Setter
    @Column(name = "log_channel")
    private Long logChannel;
    @Setter
    @Column(name = "event_active")
    private Boolean eventActive;

    public CarryType(long id, String identifier, String displayName, DiscordServer discordServer) {
        this.id = id;
        this.identifier = identifier;
        this.displayName = displayName;
        this.discordServer = discordServer;
    }

    public CarryType(String identifier, String displayName, DiscordServer discordServer, Long logChannel,
                     Boolean eventActive) {
        this.identifier = identifier;
        this.displayName = displayName;
        this.discordServer = discordServer;
        this.logChannel = logChannel;
        this.eventActive = eventActive;
    }

    public CarryType(long id, String identifier, String displayName, DiscordServer discordServer, Long logChannel,
                     Boolean eventActive) {
        this.id = id;
        this.identifier = identifier;
        this.displayName = displayName;
        this.discordServer = discordServer;
        this.logChannel = logChannel;
        this.eventActive = eventActive;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof CarryType carryType) {
            return id == carryType.id
                    || (identifier.equalsIgnoreCase(carryType.identifier) && discordServer == carryType.discordServer);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public @NotNull CarryTypeModel toModel() {
        return new CarryTypeModel(id, identifier, displayName, discordServer.toModel(), logChannel,
                eventActive);
    }
}