package com.mazadak.gateway.config;

import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Configuration
public class JwtConfig {
    @Bean
    public JwtDecoder jwtDecoder(@Value("${security.jwt.secret}") String rawSecret) {
        String secret = rawSecret.trim();

        log.info("Gateway raw secret: {}", secret);
        log.info("Gateway secret length: {}", secret.length());

        byte[] keyBytes = Base64.getDecoder().decode(secret);

        log.info("Gateway decoded key length: {} bytes", keyBytes.length);
        log.info("Gateway decoded key (first 16 bytes): {}",
                java.util.Arrays.toString(java.util.Arrays.copyOf(keyBytes, Math.min(16, keyBytes.length))));

        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        return NimbusJwtDecoder
                .withSecretKey(key)
                .macAlgorithm(MacAlgorithm.HS384)
                .build();
    }
}