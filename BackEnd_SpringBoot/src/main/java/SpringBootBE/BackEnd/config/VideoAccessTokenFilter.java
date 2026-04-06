package SpringBootBE.BackEnd.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class VideoAccessTokenFilter extends OncePerRequestFilter {

    private static final String VIDEO_PATH_PREFIX = "/assets/videos/";
    private static final String VIDEO_TOKEN_PARAM = "vt";

    private final JwtTokenService jwtTokenService;

    public VideoAccessTokenFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = pathWithoutContext(request);
        return path == null || !path.startsWith(VIDEO_PATH_PREFIX);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String path = pathWithoutContext(request);
        String requestedFileName = extractRequestedFileName(path);
        if (requestedFileName == null) {
            writeUnauthorized(response, "Đường dẫn video không hợp lệ.");
            return;
        }

        String token = request.getParameter(VIDEO_TOKEN_PARAM);
        JwtTokenService.VideoAccessPrincipal videoAccessPrincipal;
        try {
            videoAccessPrincipal = jwtTokenService.parseVideoAccessToken(token);
        } catch (IllegalArgumentException exception) {
            writeUnauthorized(response, exception.getMessage());
            return;
        }

        if (!requestedFileName.equalsIgnoreCase(videoAccessPrincipal.fileName())) {
            writeUnauthorized(response, "Token video không khớp với tệp được yêu cầu.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String pathWithoutContext(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri == null) {
            return null;
        }

        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isBlank() && uri.startsWith(contextPath)) {
            return uri.substring(contextPath.length());
        }

        return uri;
    }

    private String extractRequestedFileName(String path) {
        if (path == null || !path.startsWith(VIDEO_PATH_PREFIX)) {
            return null;
        }

        String fileName = path.substring(VIDEO_PATH_PREFIX.length());
        if (fileName.isBlank() || fileName.contains("/") || fileName.contains("\\") || fileName.contains("..")) {
            return null;
        }

        return fileName;
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"message\":\"" + jsonEscape(message) + "\",\"success\":false}");
    }

    private String jsonEscape(String value) {
        if (value == null) {
            return "Token video không hợp lệ.";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
