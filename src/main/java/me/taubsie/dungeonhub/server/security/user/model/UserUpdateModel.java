package me.taubsie.dungeonhub.server.security.user.model;

import me.taubsie.dungeonhub.common.entity.model.UpdateModel;

public record UserUpdateModel(String loginName, Boolean enabled) implements UpdateModel<UserModel> {
    @Override
    public UserModel apply(UserModel model) {
        if (enabled != null) {
            model.setEnabled(enabled);
        }

        if (loginName != null) {
            model.setLoginName(loginName);
        }

        return model;
    }
}