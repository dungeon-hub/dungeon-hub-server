package me.taubsie.dungeonhub.server.security.user;

import com.google.errorprone.annotations.DoNotCall;
import jakarta.persistence.*;
import lombok.*;
import me.taubsie.dungeonhub.common.entity.EntityModelRelation;
import me.taubsie.dungeonhub.common.model.security.user.SimpleUserModel;
import me.taubsie.dungeonhub.server.security.group.GroupEntity;
import me.taubsie.dungeonhub.server.security.group.model.SimpleUserGroupModel;
import me.taubsie.dungeonhub.server.security.privilege.PrivilegeEntity;
import me.taubsie.dungeonhub.server.security.user.model.UserModel;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Entity(name = "user")
@Table(name = "user", schema = "api", catalog = "api")
@Getter
@Setter
@NoArgsConstructor
public class UserEntity implements UserDetails, EntityModelRelation<UserModel> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    @Column(name = "id", nullable = false)
    private long id;
    @Column(name = "login_name", nullable = false)
    private String loginName;
    @Column(nullable = false)
    private String password;
    @Column
    private boolean enabled;

    @ManyToMany(fetch = FetchType.EAGER)
    @Setter(AccessLevel.PRIVATE)
    @JoinTable(name = "user_groups", schema = "api", catalog = "api", joinColumns = @JoinColumn(name = "user_id",
            referencedColumnName = "id", table = "user_groups"), inverseJoinColumns = @JoinColumn(name = "group_id",
            referencedColumnName = "id", table = "user_groups"))
    private Set<GroupEntity> groups = new HashSet<>();

    public UserEntity(String loginName, String password, boolean enabled) {
        this.loginName = loginName;
        this.password = password;
        this.enabled = enabled;
    }

    public SimpleUserModel toSimpleModel() {
        return new SimpleUserModel(id, loginName, enabled);
    }

    @Override
    @DoNotCall
    @SneakyThrows(IllegalAccessException.class)
    public UserEntity fromModel(UserModel model) {
        throw new IllegalAccessException();
    }

    @Override
    public UserModel toModel() {
        return new UserModel(id, loginName, enabled,
                getGroups().stream().map(groupEntity -> new SimpleUserGroupModel(groupEntity.getId(),
                        groupEntity.getName(),
                        groupEntity.getPrivileges().stream().map(PrivilegeEntity::toSimpleModel).collect(Collectors.toSet()))).collect(
                        Collectors.toSet()));
    }

    @Override
    @Transactional
    public Set<? extends GrantedAuthority> getAuthorities() {
        return groups.stream()
                .flatMap(groupEntity -> Stream.concat(
                        groupEntity.getPrivileges().stream()
                                .map(privilege -> new SimpleGrantedAuthority(privilege.getName())),
                        Stream.of(new SimpleGrantedAuthority("ROLE_" + groupEntity.getName()))
                ))
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return loginName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}