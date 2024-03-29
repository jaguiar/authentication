package com.prez.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Let you load a fake private key and generate some tokens with it
 *
 * @author Jennifer Aguiar
 */
public class FakeTokenGenerator {

    public FakeTokenGenerator(String issuer) {
        this.rsaAlgorithm = getRsaAlgorithm();
        this.issuer = issuer;
    }

    private Algorithm rsaAlgorithm;
    private String issuer;

    public String generateSignedToken(String sub, Date expiresAt) {

        return JWT.create()
                .withSubject(sub)
                .withExpiresAt(expiresAt)
                .withIssuer(issuer)
                .withClaim("azp", "RANDOM_CLIENT_ID")
                .withClaim("tokenName", "id_token")
                .withClaim("realm", "/spring-security-oauth")
                .sign(rsaAlgorithm);
    }

    public String generateNotExpiredSignedToken(String sub, int nbSecondsValidity) {
        return generateSignedToken(sub, Date.from(Instant.now().plus(nbSecondsValidity, ChronoUnit.SECONDS)));
    }

    private Algorithm getRsaAlgorithm() {
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            RSAPrivateKey privateKey = readPrivateKey(kf);
            return Algorithm.RSA256(null, privateKey);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static RSAPrivateKey readPrivateKey(KeyFactory factory) throws InvalidKeySpecException, IOException {
        File file = ResourceUtils.getFile("classpath:private.der");
        byte[] content = Files.readAllBytes(file.toPath());
        PKCS8EncodedKeySpec priKeySpec = new PKCS8EncodedKeySpec(content);
        return (RSAPrivateKey) factory.generatePrivate(priKeySpec);
    }

    public static RSAPublicKey readPublicKey(KeyFactory factory, String keyValue) throws InvalidKeySpecException, IOException {
        try (PemReader pemReader = new PemReader(new StringReader(keyValue))) {
            byte[] content = pemReader.readPemObject().getContent();
            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(content);
            return (RSAPublicKey) factory.generatePublic(pubKeySpec);
        } //finally will close
    }

}
