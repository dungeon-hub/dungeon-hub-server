package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dungeonhub.enums.WarningAction;
import net.dungeonhub.enums.WarningComparison;
import net.dungeonhub.enums.WarningType;
import net.dungeonhub.model.warning.WarningActionModel;

@Getter
@Setter
@Entity
@Table(name = "warn_punishment", schema = "dungeon-hub")
@AllArgsConstructor
@NoArgsConstructor
public class WarningPunishment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    @JoinColumn(name = "server_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private DiscordServer server;

    @Enumerated
    @Column(name = "warning_type", nullable = false)
    private WarningType warningType;

    @Enumerated
    @Column(name = "comparison", nullable = false)
    private WarningComparison comparison;

    @Column(name = "count", nullable = false)
    private int count;

    @Enumerated
    @Column(name = "action", nullable = false)
    private WarningAction action;

    @Column(name = "extra_data")
    private String extraData;

    public boolean applies(long activeWarnings) {
        return switch (comparison) {
            case GreaterOrEqual -> activeWarnings >= count;
            case Equal -> activeWarnings == count;
        };
    }

    public WarningActionModel toAction() {
        return new WarningActionModel(action, extraData);
    }
}