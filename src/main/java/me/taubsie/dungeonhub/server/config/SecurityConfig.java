package me.taubsie.dungeonhub.server.config;

import lombok.Getter;
import me.taubsie.dungeonhub.common.exceptions.ProgramStartException;
import me.taubsie.dungeonhub.server.security.encryption.JwtAuthorizationFilter;
import me.taubsie.dungeonhub.server.security.user.UserService;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Getter
    private final RsaKeyProperties rsaKeys;
    private final UserService userService;

    @Autowired
    public SecurityConfig(UserService userService) {
        this.userService = userService;

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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(new JwtAuthorizationFilter(userService), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/v1/user/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/user/refresh").permitAll()
                        .requestMatchers(HttpMethod.GET, "cdn/**").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(Customizer.withDefaults())
                .build();
    }
}