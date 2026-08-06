package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dungeonhub.model.warning.WarningEvidenceModel;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@Entity
@Table(name = "warn_proofs", schema = "dungeon-hub")
@AllArgsConstructor
@NoArgsConstructor
public class WarningEvidence implements net.dungeonhub.structure.entity.Entity<WarningEvidenceModel> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    @JoinColumn(name = "warn_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Warning warning;

    @Column(name = "proof", nullable = false)
    private String evidence;

    @JoinColumn(name = "submitter", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private DiscordUser submitter;

    public WarningEvidence(Warning warning, String evidence, DiscordUser submitter) {
        this.warning = warning;
        this.evidence = evidence;
        this.submitter = submitter;
    }

    @Override
    public @NotNull WarningEvidenceModel toModel() {
        return new WarningEvidenceModel(id, warning.toModel(), evidence, submitter.toModel());
    }
}