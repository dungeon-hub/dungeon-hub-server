package me.taubsie.carrylogs.server.config;


import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public record RsaKeyProperties(RSAPublicKey publicKey, RSAPrivateKey privateKey)
{
}