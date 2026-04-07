package SpringBootBE.BackEnd.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class JwtTokenService {

    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();

    private final String jwtSecret;
    private final long expirationMs;

    public JwtTokenService(
            @Value("${app.jwt.secret:change-me-to-a-secure-jwt-secret}") String jwtSecret,
            @Value("${app.jwt.expiration-ms:86400000}") long expirationMs) {
        this.jwtSecret = jwtSecret;
        this.expirationMs = expirationMs;
    }

    public TokenData generateToken(Integer userId, String email, String role, String access) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusMillis(Math.max(expirationMs, 60_000L));

        String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payloadJson = "{" +
                "\"sub\":\"" + escape(email) + "\"," +
                "\"userId\":" + userId + "," +
                "\"role\":\"" + escape(role) + "\"," +
                "\"access\":\"" + escape(access) + "\"," +
                "\"iat\":" + issuedAt.getEpochSecond() + "," +
                "\"exp\":" + expiresAt.getEpochSecond() +
                "}";

        String unsignedToken = encode(headerJson) + "." + encode(payloadJson);
        String signature = sign(unsignedToken);

        return new TokenData(unsignedToken + "." + signature, expiresAt.toEpochMilli());
    }

    private String encode(String value) {
        return BASE64_URL_ENCODER.encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String value) {
        return new String(BASE64_URL_DECODER.decode(value), StandardCharsets.UTF_8);
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return BASE64_URL_ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Không thể ký JWT token.", exception);
        }
    }

    public AuthPrincipal parseAuthorizationHeader(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new IllegalArgumentException("Thiếu Authorization header.");
        }

        if (!authorizationHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw new IllegalArgumentException("Authorization header phải dùng Bearer token.");
        }

        String token = authorizationHeader.substring(7).trim();
        if (token.isEmpty()) {
            throw new IllegalArgumentException("Thiếu Bearer token.");
        }

        return parseToken(token);
    }

    public AuthPrincipal parseToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Token không đúng định dạng JWT.");
            }

            String unsignedToken = parts[0] + "." + parts[1];
            String expectedSignature = sign(unsignedToken);
            if (!MessageDigest.isEqual(
                    expectedSignature.getBytes(StandardCharsets.UTF_8),
                    parts[2].getBytes(StandardCharsets.UTF_8))) {
                throw new IllegalArgumentException("Chữ ký token không hợp lệ.");
            }

            String payloadJson = decode(parts[1]);

            Integer userId = readIntegerClaim(payloadJson, "userId");
            String role = readStringClaim(payloadJson, "role");
            String access = readStringClaim(payloadJson, "access");
            String email = readStringClaim(payloadJson, "sub");
            Long exp = readLongClaim(payloadJson, "exp");

            if (userId == null) {
                throw new IllegalArgumentException("Token không chứa userId hợp lệ.");
            }
            if (role == null || role.isBlank()) {
                throw new IllegalArgumentException("Token không chứa role hợp lệ.");
            }
            if (exp == null) {
                throw new IllegalArgumentException("Token không chứa thời hạn hợp lệ.");
            }
            if (Instant.now().getEpochSecond() >= exp) {
                throw new IllegalArgumentException("Token đã hết hạn.");
            }

            return new AuthPrincipal(userId, email, role, access, exp);
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new IllegalArgumentException("Token không hợp lệ.", exception);
        }
    }

    private String readStringClaim(String json, String claimName) {
        String rawValue = extractJsonString(json, claimName);
        if (rawValue == null) {
            return null;
        }
        String stringValue = unescapeJson(rawValue).trim();
        return stringValue.isEmpty() ? null : stringValue;
    }

    private Integer readIntegerClaim(String json, String claimName) {
        Long longValue = readLongClaim(json, claimName);
        if (longValue == null) {
            return null;
        }
        if (longValue > Integer.MAX_VALUE || longValue < Integer.MIN_VALUE) {
            return null;
        }
        return longValue.intValue();
    }

    private Long readLongClaim(String json, String claimName) {
        String rawValue = extractJsonNumber(json, claimName);
        if (rawValue == null) {
            return null;
        }
        try {
            return Long.parseLong(rawValue.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String extractJsonString(String json, String claimName) {
        if (json == null || claimName == null) {
            return null;
        }
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(claimName) + "\"\\s*:\\s*\"((?:\\\\.|[^\"])*)\"");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String extractJsonNumber(String json, String claimName) {
        if (json == null || claimName == null) {
            return null;
        }
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(claimName) + "\"\\s*:\\s*(-?\\d+)");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String unescapeJson(String value) {
        if (value == null) {
            return null;
        }
        return value
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    public record AuthPrincipal(Integer userId, String email, String role, String access, long expiresAt) {
        public boolean isAdmin() {
            return "Admin".equalsIgnoreCase(role) || "admin".equalsIgnoreCase(access);
        }

        public boolean isStudent() {
            return "Student".equalsIgnoreCase(role) || "student".equalsIgnoreCase(access);
        }
    }

    public record TokenData(String token, long expiresAt) {
    }
}
