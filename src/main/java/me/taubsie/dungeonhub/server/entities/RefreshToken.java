package me.taubsie.dungeonhub.server.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.taubsie.dungeonhub.server.security.user.UserEntity;
import org.hibernate.annotations.OnDelete;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity(name = "refresh_token")
@Table(name = "refresh_token", schema = "api", catalog = "api")
@NoArgsConstructor
public class RefreshToken {
    @EmbeddedId
    private RefreshTokenId refreshTokenId;

    @MapsId("user")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    @JoinColumn(name = "user", nullable = false)
    private UserEntity user;

    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "token", nullable = false)
    private UUID token;

    @Column(name = "valid_until", nullable = false)
    private Instant validUntil;

    public RefreshToken(UserEntity user, Instant validUntil) {
        this.refreshTokenId = new RefreshTokenId(user.getId());
        this.user = user;
        this.validUntil = validUntil;
        this.token = UUID.randomUUID();
    }
}