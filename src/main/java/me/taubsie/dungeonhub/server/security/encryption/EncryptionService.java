package me.taubsie.dungeonhub.server.security.encryption;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import me.taubsie.dungeonhub.common.exceptions.ProgramStartException;
import me.taubsie.dungeonhub.common.model.security.user.JwtTokenModel;
import me.taubsie.dungeonhub.server.config.RsaKeyProperties;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

@Service
public class EncryptionService {
    private static final Logger logger = LoggerFactory.getLogger(EncryptionService.class);

    private final RsaKeyProperties rsaKeys;

    public EncryptionService() {
        try (InputStream publicKeyStream = getClass().getClassLoader().getResourceAsStream("certs/public.pem");
             InputStream privateKeyStream = getClass().getClassLoader().getResourceAsStream("certs/private.pem")) {
            if (publicKeyStream == null || privateKeyStream == null) {
                throw new ProgramStartException("Key files are missing!");
            }

            String publicKey = new String(publicKeyStream.readAllBytes())
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replaceAll(System.lineSeparator(), "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .strip();
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.decodeBase64(publicKey));

            String privateKey = new String(privateKeyStream.readAllBytes())
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replaceAll(System.lineSeparator(), "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .strip();
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKey));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            this.rsaKeys = new RsaKeyProperties((RSAPublicKey) keyFactory.generatePublic(publicKeySpec),
                    (RSAPrivateKey) keyFactory.generatePrivate(spec));
        }
        catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException exception) {
            throw new ProgramStartException(exception);
        }
    }

    public JwtTokenModel generateKey(String subject) {
        Instant issued = Instant.now();

        String token = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(Date.from(issued))
                .setIssuer("api.dungeon-hub.net")
                .signWith(rsaKeys.privateKey())
                .setExpiration(Date.from(issued.plus(1, ChronoUnit.HOURS)))
                .compact();

        return new JwtTokenModel(token, issued.minusSeconds(30));
    }

    public Optional<UsernamePasswordAuthenticationToken> validate(String token, AuthorityFactory authorityFactory) {
        try {
            String subject = Jwts.parserBuilder()
                    .setSigningKey(rsaKeys.privateKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();

            long userId = Long.parseLong(subject);

            return Optional.of(new UsernamePasswordAuthenticationToken(userId, null, authorityFactory.get(userId)));
        }
        catch (ExpiredJwtException expiredJwtException) {
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
        }
        catch (SignatureException signatureException) {
            logger.warn("Someone tried to authorize with a forget JWT.", signatureException);
            throw new HttpClientErrorException(HttpStatus.I_AM_A_TEAPOT);
        }
    }
}