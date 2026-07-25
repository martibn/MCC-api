package com.mcc.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AltchaService {

    @Value("${altcha.hmac-key:default-altcha-hmac-key-change-in-production}")
    private String hmacKey;

    public Map<String, Object> generateChallenge() {
        try {
            SecureRandom rng = new SecureRandom();

            byte[] nonceBytes = new byte[32];
            rng.nextBytes(nonceBytes);
            String nonce = HexFormat.of().formatHex(nonceBytes);

            byte[] keyPrefixBytes = new byte[2];
            rng.nextBytes(keyPrefixBytes);
            String keyPrefix = HexFormat.of().formatHex(keyPrefixBytes);

            LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
            parameters.put("algorithm", "SHA-256");
            parameters.put("cost", 1);
            parameters.put("keyLength", 32);
            parameters.put("nonce", nonce);
            parameters.put("salt", "");
            parameters.put("keyPrefix", keyPrefix);

            String signature = signParameters(parameters);

            return Map.of(
                "parameters", parameters,
                "signature", signature
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate ALTCHA challenge", e);
        }
    }

    public boolean verifyPayload(String altchaPayload) {
        try {
            byte[] decoded = Base64.getDecoder().decode(altchaPayload);
            String json = new String(decoded, "UTF-8");

            String[] parts = extractParts(json);
            String paramsJson = parts[0];
            String pSignature = parts[1];
            int pCounter = Integer.parseInt(parts[2]);

            String pAlgorithm = extractField(paramsJson, "algorithm");
            if (!"SHA-256".equals(pAlgorithm)) {
                return false;
            }
            String pNonce = extractField(paramsJson, "nonce");
            String pSalt = extractField(paramsJson, "salt");
            String pKeyPrefix = extractField(paramsJson, "keyPrefix");
            int pKeyLength = Integer.parseInt(extractFieldFromParams(paramsJson, "keyLength", "32"));
            int pCost = Integer.parseInt(extractFieldFromParams(paramsJson, "cost", "1"));

            String expectedSignature = hmacSign(paramsJson);
            if (!pSignature.equals(expectedSignature)) {
                return false;
            }

            byte[] nonceBuf = hexToBytes(pNonce);
            byte[] saltBuf = hexToBytes(pSalt);
            byte[] keyPrefixBuf = hexToBytes(pKeyPrefix);

            ByteBuffer bb = ByteBuffer.allocate(4);
            bb.putInt(pCounter);
            byte[] counterBuf = bb.array();

            byte[] password = concat(nonceBuf, counterBuf);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] derivedKey = null;

            for (int i = 0; i < pCost; i++) {
                byte[] input = (i == 0) ? concat(saltBuf, password) : derivedKey;
                byte[] fullHash = md.digest(input);
                int len = Math.min(pKeyLength, fullHash.length);
                derivedKey = new byte[len];
                System.arraycopy(fullHash, 0, derivedKey, 0, len);
            }

            if (derivedKey == null || derivedKey.length < keyPrefixBuf.length) {
                return false;
            }

            for (int i = 0; i < keyPrefixBuf.length; i++) {
                if (derivedKey[i] != keyPrefixBuf[i]) {
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String[] extractParts(String json) {
        int challengeStart = json.indexOf("\"challenge\":");
        int challengeEnd = findMatchingBrace(json, challengeStart + "\"challenge\":".length());
        String challengeSection = json.substring(challengeStart + "\"challenge\":".length(), challengeEnd + 1);

        int sigStart = challengeSection.indexOf("\"signature\":\"");
        sigStart += "\"signature\":\"".length();
        int sigEnd = challengeSection.indexOf("\"", sigStart);
        String signature = challengeSection.substring(sigStart, sigEnd);

        int paramsStart = challengeSection.indexOf("\"parameters\":");
        int paramsEnd = findMatchingBrace(challengeSection, paramsStart + "\"parameters\":".length());
        String paramsJson = challengeSection.substring(paramsStart + "\"parameters\":".length(), paramsEnd + 1);

        int solutionStart = json.indexOf("\"solution\":");
        int solutionEnd = findMatchingBrace(json, solutionStart + "\"solution\":".length());
        String solutionSection = json.substring(solutionStart + "\"solution\":".length(), solutionEnd + 1);

        int counterStart = solutionSection.indexOf("\"counter\":");
        counterStart += "\"counter\":".length();
        int counterEnd = findValueEnd(solutionSection, counterStart);
        String counter = solutionSection.substring(counterStart, counterEnd).trim();

        return new String[]{paramsJson, signature, counter};
    }

    private int findMatchingBrace(String s, int start) {
        char open = s.charAt(start);
        char close = (open == '{') ? '}' : '[';
        int depth = 1;
        for (int i = start + 1; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == open) depth++;
            else if (c == close) {
                depth--;
                if (depth == 0) return i;
            }
        }
        return s.length() - 1;
    }

    private int findValueEnd(String s, int start) {
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == ',' || c == '}' || c == ']') return i;
        }
        return s.length();
    }

    private String extractField(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start >= 0) {
            start += search.length();
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        }
        return null;
    }

    private String extractFieldFromParams(String json, String key, String defaultValue) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start >= 0) {
            start += search.length();
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        }
        search = "\"" + key + "\":";
        start = json.indexOf(search);
        if (start >= 0) {
            start += search.length();
            int end = findValueEnd(json, start);
            return json.substring(start, end).trim();
        }
        return defaultValue;
    }

    private String signParameters(LinkedHashMap<String, Object> parameters) throws Exception {
        String algorithm = (String) parameters.get("algorithm");
        int cost = (int) parameters.get("cost");
        int keyLength = (int) parameters.get("keyLength");
        String nonce = (String) parameters.get("nonce");
        String salt = (String) parameters.get("salt");
        String keyPrefix = (String) parameters.get("keyPrefix");

        StringBuilder sb = new StringBuilder();
        sb.append("{\"algorithm\":\"").append(algorithm);
        sb.append("\",\"cost\":").append(cost);
        sb.append(",\"keyLength\":").append(keyLength);
        sb.append(",\"nonce\":\"").append(nonce);
        sb.append("\",\"salt\":\"").append(salt);
        sb.append("\",\"keyPrefix\":\"").append(keyPrefix);
        sb.append("\"}");

        return hmacSign(sb.toString());
    }

    private String hmacSign(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(hmacKey.getBytes("UTF-8"), "HmacSHA256");
        mac.init(keySpec);
        byte[] sigBytes = mac.doFinal(data.getBytes("UTF-8"));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(sigBytes);
    }

    private byte[] hexToBytes(String hex) {
        if (hex == null || hex.isEmpty()) {
            return new byte[0];
        }
        return HexFormat.of().parseHex(hex);
    }

    private byte[] concat(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }
}
