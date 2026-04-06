package SpringBootBE.BackEnd.dto;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MomoUtil {

    private MomoUtil() {
    }

    public static String hmacSHA256(String data, String key) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(rawHmac);
        } catch (Exception exception) {
            throw new IllegalStateException("Không thể tạo chữ ký HMAC SHA256 cho MoMo.", exception);
        }
    }

    public static String buildRawSignature(Map<String, String> values) {
        StringJoiner joiner = new StringJoiner("&");
        values.forEach((key, value) -> joiner.add(key + "=" + nullToEmpty(value)));
        return joiner.toString();
    }

    public static String toJson(Map<String, ?> values) {
        StringJoiner joiner = new StringJoiner(",", "{", "}");
        values.forEach((key, value) -> joiner.add("\"" + escapeJson(key) + "\":" + toJsonValue(value)));
        return joiner.toString();
    }

    public static String extractJsonString(String json, String fieldName) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(fieldName) + "\"\\s*:\\s*\"(.*?)\"")
                .matcher(json);
        return matcher.find() ? matcher.group(1).replace("\\/", "/") : null;
    }

    public static Integer extractJsonInteger(String json, String fieldName) {
        Matcher matcher = Pattern.compile("\"" + Pattern.quote(fieldName) + "\"\\s*:\\s*(-?\\d+)")
                .matcher(json);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
    }

    public static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String toJsonValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        return "\"" + escapeJson(String.valueOf(value)) + "\"";
    }

    private static String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
