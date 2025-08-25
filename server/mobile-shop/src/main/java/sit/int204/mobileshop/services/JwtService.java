package sit.int204.mobileshop.services;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;

@Slf4j
@Service
public class JwtService {

    @Value("${app.jwt.verification.expiration-hours:1}")
    private int verificationTokenExpirationHours;

    @Value("${app.jwt.issuer:itb-mshop}")
    private String issuer;

    private RSAKey rsaKey;
    private RSASSASigner signer;
    private RSASSAVerifier verifier;

    @PostConstruct
    public void init() throws Exception {
        // Generate RSA key pair
        this.rsaKey = new RSAKeyGenerator(2048)
                .keyID("verification-key-1")
                .generate();

        // Create signer and verifier
        this.signer = new RSASSASigner((RSAPrivateKey) rsaKey.toPrivateKey());
        this.verifier = new RSASSAVerifier((RSAPublicKey) rsaKey.toPublicKey());

        log.info("JWT RSA keys initialized successfully");
    }

    public String generateVerificationToken(Long userId, String email) {
        try {
            Instant now = Instant.now();
            Instant expiration = now.plusSeconds(verificationTokenExpirationHours * 3600L);

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(userId.toString())
                    .issuer(issuer)
                    .audience("email-verification")
                    .claim("email", email)
                    .claim("type", "email_verification")
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(expiration))
                    .jwtID(java.util.UUID.randomUUID().toString())
                    .build();

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256)
                            .keyID(rsaKey.getKeyID())
                            .build(),
                    claimsSet
            );

            signedJWT.sign(signer);
            return signedJWT.serialize();

        } catch (Exception e) {
            log.error("Failed to generate verification token", e);
            throw new RuntimeException("Failed to generate verification token", e);
        }
    }

    public JWTClaimsSet validateVerificationToken(String token) throws ParseException, JOSEException {
        SignedJWT signedJWT = SignedJWT.parse(token);

        // Verify signature
        if (!signedJWT.verify(verifier)) {
            throw new JOSEException("Invalid token signature");
        }

        JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();

        // Check expiration
        Date expirationTime = claimsSet.getExpirationTime();
        if (expirationTime == null || expirationTime.before(new Date())) {
            throw new JOSEException("Token has expired");
        }

        // Check token type
        String tokenType = claimsSet.getStringClaim("type");
        if (!"email_verification".equals(tokenType)) {
            throw new JOSEException("Invalid token type");
        }

        // Check issuer
        if (!issuer.equals(claimsSet.getIssuer())) {
            throw new JOSEException("Invalid token issuer");
        }

        return claimsSet;
    }

    public Long getUserIdFromToken(String token) throws ParseException, JOSEException {
        JWTClaimsSet claimsSet = validateVerificationToken(token);
        return Long.parseLong(claimsSet.getSubject());
    }

    public String getEmailFromToken(String token) throws ParseException, JOSEException {
        JWTClaimsSet claimsSet = validateVerificationToken(token);
        return claimsSet.getStringClaim("email");
    }
}