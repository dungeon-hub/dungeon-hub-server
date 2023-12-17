package me.taubsie.dungeonhub.server.security.user;

import lombok.AllArgsConstructor;
import me.taubsie.dungeonhub.common.entity.EntityService;
import me.taubsie.dungeonhub.common.exceptions.EntityUnknownException;
import me.taubsie.dungeonhub.common.model.security.user.UserCreationModel;
import me.taubsie.dungeonhub.common.model.security.user.UserLoginModel;
import me.taubsie.dungeonhub.common.model.security.user.UserLoginVerificationModel;
import me.taubsie.dungeonhub.common.model.security.user.UserTokenRefreshModel;
import me.taubsie.dungeonhub.server.exception.CreationException;
import me.taubsie.dungeonhub.server.repositories.RefreshTokenRepository;
import me.taubsie.dungeonhub.server.security.encryption.EncryptionService;
import me.taubsie.dungeonhub.server.security.exception.LoginNameOccupiedException;
import me.taubsie.dungeonhub.server.security.user.model.UserInitializeModel;
import me.taubsie.dungeonhub.server.security.user.model.UserModel;
import me.taubsie.dungeonhub.server.security.user.model.UserUpdateModel;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
@AllArgsConstructor
public class UserService implements EntityService<UserEntity, UserModel, UserCreationModel, UserInitializeModel,
        UserUpdateModel>, UserDetailsService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EncryptionService encryptionService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Optional<UserEntity> loadEntityById(long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<UserEntity> loadEntityByName(String name) {
        return userRepository.findByLoginName(name);
    }

    @Override
    public List<UserEntity> findAllEntities() {
        return userRepository.findAll();
    }

    @Override
    public UserEntity createEntity(UserInitializeModel model) throws CreationException {
        userRepository.findByLoginName(model.getLoginName())
                .ifPresent(e -> {
                    throw new LoginNameOccupiedException();
                });

        String password = model.getPassword();

        String hashedPassword = passwordEncoder.encode(password);
        return saveEntity(model.toEntity((entity ->
        {
            entity.setPassword(hashedPassword);
            return entity;
        })));
    }

    public UserEntity saveEntity(UserEntity entity) {
        return userRepository.save(entity);
    }

    @Override
    public Function<UserModel, UserEntity> toEntity() {
        return userModel -> loadEntityById(userModel.getId()).orElseThrow(() -> new EntityUnknownException(userModel.getId()));
    }

    @Override
    public Function<UserEntity, UserModel> toModel() {
        return UserEntity::toModel;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return loadEntityByName(username).orElseThrow(() -> new UsernameNotFoundException(username));
    }

    @Override
    public boolean delete(long id) {
        return userRepository.findById(id).map(userEntity ->
        {
            userRepository.deleteById(id);
            return true;
        }).orElse(false);
    }

    @Transactional
    public Optional<UserLoginVerificationModel> login(UserLoginModel userLoginModel) {
        return loadEntityByName(userLoginModel.loginName()).map(user ->
        {
            if (passwordEncoder.matches(userLoginModel.password(), user.getPassword())) {
                return new UserLoginVerificationModel(user.getId(),
                        encryptionService.generateKey(String.valueOf(user.getId())),
                        encryptionService.generateRefreshToken(user),
                        Instant.now());
            }
            return null;
        });
    }

    @Transactional
    public Optional<UserLoginVerificationModel> refresh(UserTokenRefreshModel userTokenRefreshModel) {
        return refreshTokenRepository.findRefreshTokenByRefreshTokenIdToken(userTokenRefreshModel.refreshToken())
                .map(refreshToken -> {
                    refreshTokenRepository.delete(refreshToken);
                    return refreshToken.getUser();
                })
                .map(userEntity -> new UserLoginVerificationModel(userEntity.getId(),
                        encryptionService.generateKey(String.valueOf(userEntity.getId())),
                        encryptionService.generateRefreshToken(userEntity),
                        Instant.now()));
    }

    @Transactional
    public Optional<UsernamePasswordAuthenticationToken> validate(String token) {
        return encryptionService.validate(token,
                id -> loadEntityById(id).map(UserEntity::getAuthorities).orElse(new HashSet<>()));
    }
}