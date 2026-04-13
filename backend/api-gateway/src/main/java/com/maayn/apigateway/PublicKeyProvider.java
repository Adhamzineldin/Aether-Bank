package com.maayn.apigateway;

import lombok.extern.slf4j.Slf4j;
import maayn.veld.generated.sdk.iam.IamClient;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;


@Component
@Slf4j
public class PublicKeyProvider implements ApplicationRunner {

    private final IamClient iamClient;
    private RSAPublicKey publicKey;

    public PublicKeyProvider(IamClient iamClient) {
        this.iamClient = iamClient;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Fetching RSA public key from IAM service...");
        String pem = iamClient.shared.getPublicKey().getPublicKey();
        this.publicKey = parsePem(pem);
        log.info("RSA public key loaded successfully.");
    }

    public RSAPublicKey getPublicKey() {
        if (publicKey == null) {
            throw new IllegalStateException("RSA public key not loaded — IAM service may have been unreachable at startup.");
        }
        return publicKey;
    }

    private static RSAPublicKey parsePem(String pem) throws Exception {
        String base64 = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] der = Base64.getDecoder().decode(base64);
        return (RSAPublicKey) KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(der));
    }
}
