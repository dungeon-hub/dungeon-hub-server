package me.taubsie.dungeonhub.server.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.taubsie.dungeonhub.common.entity.EntityModelRelation;
import me.taubsie.dungeonhub.common.model.carry_type.CarryTypeModel;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity(name = "carry_type")
@Table(name = "carry_type", schema = "dungeon-hub")
@NoArgsConstructor
public class CarryType implements EntityModelRelation<CarryTypeModel> {
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
    @Column(name = "display_name", nullable = false, length = 50)
    private String displayName;

    @Getter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "server", nullable = false)
    //final
    private Server server;
    @Column(name = "log_channel")
    private Long logChannel;
    @Column(name = "leaderboard_channel")
    private Long leaderboardChannel;
    @Column(name = "event_active")
    private Boolean eventActive;

    public CarryType(long id, String identifier, String displayName, Server server) {
        this.id = id;
        this.identifier = identifier;
        this.displayName = displayName;
        this.server = server;
    }

    public CarryType(String identifier, String displayName, Server server, Long logChannel, Long leaderboardChannel,
                     Boolean eventActive) {
        this.identifier = identifier;
        this.displayName = displayName;
        this.server = server;
        this.logChannel = logChannel;
        this.leaderboardChannel = leaderboardChannel;
        this.eventActive = eventActive;
    }

    public CarryType(long id, String identifier, String displayName, Server server, Long logChannel,
                     Long leaderboardChannel, Boolean eventActive) {
        this.id = id;
        this.identifier = identifier;
        this.displayName = displayName;
        this.server = server;
        this.logChannel = logChannel;
        this.leaderboardChannel = leaderboardChannel;
        this.eventActive = eventActive;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof CarryType carryType) {
            return id == carryType.id
                    || (identifier.equalsIgnoreCase(carryType.identifier) && server == carryType.server);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public CarryType fromModel(CarryTypeModel model) {
        return new CarryType(model.getId(), model.getIdentifier(), model.getDisplayName(),
                server.fromModel(model.getServer()), model.getActualLogChannel(), model.getActualLeaderboardChannel(),
                model.getEventActive());
    }

    @Override
    public CarryTypeModel toModel() {
        return new CarryTypeModel(id, identifier, displayName, server.toModel(), logChannel, leaderboardChannel,
                eventActive);
    }
}