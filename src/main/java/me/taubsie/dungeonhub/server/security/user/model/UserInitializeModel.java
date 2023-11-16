package me.taubsie.dungeonhub.server.security.user.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.taubsie.dungeonhub.common.entity.model.InitializeModel;
import me.taubsie.dungeonhub.common.model.security.user.UserCreationModel;
import me.taubsie.dungeonhub.server.security.user.UserEntity;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserInitializeModel implements InitializeModel<UserEntity, UserCreationModel> {
    private String loginName;
    private String password;
    private Boolean enabled;

    @Override
    public UserEntity toEntity() {
        return new UserEntity(loginName, password, enabled);
    }

    @Override
    public UserInitializeModel fromCreationModel(UserCreationModel creationModel) {
        return new UserInitializeModel(creationModel.loginName(), creationModel.password(), creationModel.enabled());
    }
}
