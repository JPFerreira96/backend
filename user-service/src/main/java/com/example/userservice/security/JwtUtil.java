package com.example.userservice.security;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    @Value("${JWT_SECRET:change-me}")
    private String secretKey;

    @Value("${JWT_EXPIRATION_MS:3600000}")
    private long expirationMs;

    public String generateToken(String username) {
        long nowSec = System.currentTimeMillis() / 1000L;
        long expSec = nowSec + (expirationMs / 1000L);

        String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payloadJson = "{\"sub\":\"" + escape(username) + "\",\"iat\":" + nowSec + ",\"exp\":" + expSec + "}";

        String headerB64 = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));
        String payloadB64 = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));
        String unsignedToken = headerB64 + "." + payloadB64;
        String signature = sign(unsignedToken, secretKey);
        return unsignedToken + "." + signature;
    }

    public boolean validateToken(String token, String expectedUsername) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return false;

            String unsigned = parts[0] + "." + parts[1];
            String signature = parts[2];

            // Verifica assinatura
            if (!constantTimeEquals(sign(unsigned, secretKey), signature)) return false;

            // Verifica expiração e, se informado, o username
            String payloadJson = new String(base64UrlDecode(parts[1]), StandardCharsets.UTF_8);
            Map<String, String> claims = parseJson(payloadJson);

            String expStr = claims.get("exp");
            if (expStr == null) return false;
            long expSec = Long.parseLong(expStr);
            long nowSec = System.currentTimeMillis() / 1000L;
            if (nowSec >= expSec) return false;

            if (expectedUsername != null) {
                String sub = stripQuotes(claims.getOrDefault("sub", ""));
                if (!expectedUsername.equals(sub)) return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;
            String payloadJson = new String(base64UrlDecode(parts[1]), StandardCharsets.UTF_8);
            Map<String, String> claims = parseJson(payloadJson);
            return stripQuotes(claims.get("sub"));
        } catch (Exception e) {
            return null;
        }
    }

    // Helpers

    private String sign(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] sig = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return base64UrlEncode(sig);
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao assinar JWT", e);
        }
    }

    private static String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static byte[] base64UrlDecode(String str) {
        return Base64.getUrlDecoder().decode(str);
    }

    // Parser JSON simples para payloads planos {"sub":"user","exp":12345}
    private Map<String, String> parseJson(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null) return map;
        String trimmed = json.trim();
        if (trimmed.startsWith("{")) trimmed = trimmed.substring(1);
        if (trimmed.endsWith("}")) trimmed = trimmed.substring(0, trimmed.length() - 1);
        if (trimmed.isEmpty()) return map;

        String[] pairs = trimmed.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":", 2);
            if (kv.length == 2) {
                String key = stripQuotes(kv[0].trim());
                String value = kv[1].trim();
                map.put(key, value);
            }
        }
        return map;
    }

    private String stripQuotes(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    public long getExpirationMs() { return expirationMs; }

    // Comparação em tempo constante
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        byte[] aa = a.getBytes(StandardCharsets.UTF_8);
        byte[] bb = b.getBytes(StandardCharsets.UTF_8);
        if (aa.length != bb.length) return false;
        int result = 0;
        for (int i = 0; i < aa.length; i++) {
            result |= aa[i] ^ bb[i];
        }
        return result == 0;
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}