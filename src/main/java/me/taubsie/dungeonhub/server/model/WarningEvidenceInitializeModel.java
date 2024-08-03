package me.taubsie.dungeonhub.server.model;

import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.common.entity.model.InitializeModel;
import me.taubsie.dungeonhub.common.model.warning.WarningEvidenceCreationModel;
import me.taubsie.dungeonhub.server.entities.DiscordUser;
import me.taubsie.dungeonhub.server.entities.Warning;
import me.taubsie.dungeonhub.server.entities.WarningEvidence;

@AllArgsConstructor
public class WarningEvidenceInitializeModel implements InitializeModel<WarningEvidence, WarningEvidenceCreationModel> {
    private Warning warning;
    private String evidence;
    private DiscordUser submitter;

    @Override
    public WarningEvidence toEntity() {
        return new WarningEvidence(warning, evidence, submitter);
    }

    public WarningEvidenceInitializeModel(Warning warning, DiscordUser submitter) {
        this.warning = warning;
        this.submitter = submitter;
    }

    @Override
    public WarningEvidenceInitializeModel fromCreationModel(WarningEvidenceCreationModel creationModel) {
        return new WarningEvidenceInitializeModel(warning, creationModel.getEvidence(), submitter);
    }
}