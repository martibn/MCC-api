package com.mcc.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class AltchaService {

    private static final int MAX_NUMBER = 50000;
    private static final int ZERO_BITS = 12;

    @Value("${altcha.hmac-key:default-altcha-hmac-key-change-in-production}")
    private String hmacKey;

    public String generateChallenge() {
        try {
            SecureRandom rng = new SecureRandom();
            byte[] saltBytes = new byte[16];
            rng.nextBytes(saltBytes);
            String salt = HexFormat.of().formatHex(saltBytes);
            String challenge = salt;

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(hmacKey.getBytes("UTF-8"), "HmacSHA256");
            mac.init(keySpec);
            byte[] sigBytes = mac.doFinal(challenge.getBytes("UTF-8"));
            String signature = Base64.getUrlEncoder().withoutPadding().encodeToString(sigBytes);

            return "{\"algorithm\":\"SHA-256\",\"challenge\":\"" + challenge + "\",\"salt\":\"" + salt + "\",\"maxnumber\":" + MAX_NUMBER + ",\"signature\":\"" + signature + "\"}";
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate ALTCHA challenge", e);
        }
    }

    public boolean verifyPayload(String algorithm, String challenge, String salt, int number, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(hmacKey.getBytes("UTF-8"), "HmacSHA256");
            mac.init(keySpec);
            byte[] expectedSig = mac.doFinal(challenge.getBytes("UTF-8"));
            String expectedSigB64 = Base64.getUrlEncoder().withoutPadding().encodeToString(expectedSig);

            if (!signature.equals(expectedSigB64)) return false;

            String hashInput = challenge + number;
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(hashInput.getBytes("UTF-8"));

            int leadingZeros = 0;
            for (byte b : hash) {
                if (b == 0) {
                    leadingZeros += 8;
                } else {
                    leadingZeros += Integer.numberOfLeadingZeros(b & 0xFF) - 24;
                    break;
                }
            }

            return leadingZeros >= ZERO_BITS;
        } catch (Exception e) {
            return false;
        }
    }
}
