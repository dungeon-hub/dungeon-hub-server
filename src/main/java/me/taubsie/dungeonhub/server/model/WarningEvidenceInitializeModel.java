package me.taubsie.dungeonhub.server.model;

import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.server.entities.DiscordUser;
import me.taubsie.dungeonhub.server.entities.Warning;
import me.taubsie.dungeonhub.server.entities.WarningEvidence;
import net.dungeonhub.model.warning.WarningEvidenceCreationModel;
import net.dungeonhub.model.warning.WarningEvidenceModel;
import net.dungeonhub.structure.model.InitializeModel;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class WarningEvidenceInitializeModel implements InitializeModel<WarningEvidence, WarningEvidenceModel, WarningEvidenceCreationModel> {
    private Warning warning;
    private String evidence;
    private DiscordUser submitter;

    public WarningEvidenceInitializeModel(Warning warning, DiscordUser submitter) {
        this.warning = warning;
        this.submitter = submitter;
    }

    @Override
    public @NotNull WarningEvidence toEntity() {
        return new WarningEvidence(warning, evidence, submitter);
    }

    @Override
    public @NotNull WarningEvidenceInitializeModel fromCreationModel(WarningEvidenceCreationModel creationModel) {
        return new WarningEvidenceInitializeModel(warning, creationModel.getEvidence(), submitter);
    }
}