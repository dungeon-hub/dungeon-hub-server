package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.taubsie.dungeonhub.server.security.user.UserEntity;
import org.hibernate.annotations.OnDelete;

import java.time.Instant;

@Getter
@Entity(name = "refresh_token")
@Table(name = "refresh_token", schema = "api", catalog = "api")
@NoArgsConstructor
public class RefreshToken {
    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    @JoinColumn(name = "user", nullable = false)
    private UserEntity user;

    @Column(name = "token", nullable = false)
    private String token;

    @Column(name = "valid_until", nullable = false)
    private Instant validUntil;
}