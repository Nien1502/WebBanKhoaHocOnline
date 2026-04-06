package SpringBootBE.BackEnd.Service;

import SpringBootBE.BackEnd.config.JwtTokenService;
import SpringBootBE.BackEnd.dto.VideoAssetResponse;
import SpringBootBE.BackEnd.dto.VideoSignedUrlResponse;
import SpringBootBE.BackEnd.model.Lesson;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class VideoAssetService {

    private static final String VIDEO_RESOURCE_PATTERN = "classpath:/static/assets/videos/*";
    private static final String VIDEO_PUBLIC_PATH = "/assets/videos/";
    private static final List<String> SUPPORTED_EXTENSIONS = List.of(".mp4", ".webm", ".m4v", ".mov");

    private final JwtTokenService jwtTokenService;
    private final LessonService lessonService;
    private final EnrollmentService enrollmentService;
    private final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    public VideoAssetService(JwtTokenService jwtTokenService,
                             LessonService lessonService,
                             EnrollmentService enrollmentService) {
        this.jwtTokenService = jwtTokenService;
        this.lessonService = lessonService;
        this.enrollmentService = enrollmentService;
    }

    public List<VideoAssetResponse> findAllVideos() {
        try {
            return Arrays.stream(resourcePatternResolver.getResources(VIDEO_RESOURCE_PATTERN))
                    .filter(Resource::isReadable)
                    .map(this::toResponse)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .sorted(Comparator.comparing(VideoAssetResponse::getFileName, String.CASE_INSENSITIVE_ORDER))
                    .toList();
        } catch (IOException ex) {
            throw new IllegalStateException("Không thể đọc danh sách video trong static/assets/videos", ex);
        }
    }

    public Optional<VideoAssetResponse> findVideoByFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return Optional.empty();
        }

        return findAllVideos().stream()
                .filter(video -> fileName.equalsIgnoreCase(video.getFileName()))
                .findFirst();
    }

    public VideoSignedUrlResponse generateSignedVideoUrl(Integer lessonId, JwtTokenService.AuthPrincipal authPrincipal) {
        if (authPrincipal == null) {
            throw new IllegalArgumentException("Bạn chưa đăng nhập.");
        }
        if (lessonId == null || lessonId <= 0) {
            throw new IllegalArgumentException("lessonId không hợp lệ.");
        }

        Lesson lesson = lessonService.findById(lessonId);
        if (lesson == null) {
            throw new NoSuchElementException("Không tìm thấy bài học.");
        }

        Integer courseId = lesson.getCourse() != null ? lesson.getCourse().getId() : null;
        if (courseId == null) {
            throw new IllegalStateException("Bài học chưa được gán khóa học.");
        }

        if (!authPrincipal.isAdmin()
                && enrollmentService.findByUserIdAndCourseId(authPrincipal.userId(), courseId) == null) {
            throw new SecurityException("Bạn chưa đăng ký khóa học chứa video này.");
        }

        String fileName = extractFileNameFromVideoValue(lesson.getVideoURL());
        if (fileName == null || !isSupportedVideoFile(fileName)) {
            throw new IllegalStateException("Bài học chưa cấu hình video hợp lệ.");
        }

        VideoAssetResponse videoAsset = findVideoByFileName(fileName)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy tệp video trên hệ thống."));

        JwtTokenService.TokenData tokenData = jwtTokenService.generateVideoAccessToken(
                authPrincipal.userId(),
                courseId,
                videoAsset.getFileName()
        );

        String signedUrl = VIDEO_PUBLIC_PATH + videoAsset.getFileName()
                + "?vt=" + URLEncoder.encode(tokenData.token(), StandardCharsets.UTF_8);

        return new VideoSignedUrlResponse(
                lessonId,
                courseId,
                videoAsset.getFileName(),
                videoAsset.getDisplayName(),
                signedUrl,
                tokenData.expiresAt()
        );
    }

    private Optional<VideoAssetResponse> toResponse(Resource resource) {
        String fileName = resource.getFilename();
        if (fileName == null || !isSupportedVideoFile(fileName)) {
            return Optional.empty();
        }

        String displayName = stripExtension(fileName).replace('_', ' ');
        String url = VIDEO_PUBLIC_PATH + fileName;
        return Optional.of(new VideoAssetResponse(fileName, displayName, url));
    }

    private boolean isSupportedVideoFile(String fileName) {
        String lowerFileName = fileName.toLowerCase(Locale.ROOT);
        return SUPPORTED_EXTENSIONS.stream().anyMatch(lowerFileName::endsWith);
    }

    private String extractFileNameFromVideoValue(String videoValue) {
        if (videoValue == null || videoValue.isBlank()) {
            return null;
        }

        String normalized = videoValue.trim();

        int queryIndex = normalized.indexOf('?');
        if (queryIndex >= 0) {
            normalized = normalized.substring(0, queryIndex);
        }

        int fragmentIndex = normalized.indexOf('#');
        if (fragmentIndex >= 0) {
            normalized = normalized.substring(0, fragmentIndex);
        }

        normalized = normalized.replace('\\', '/');
        int lastSlashIndex = normalized.lastIndexOf('/');
        String fileName = lastSlashIndex >= 0 ? normalized.substring(lastSlashIndex + 1) : normalized;

        if (fileName.isBlank() || fileName.contains("..")) {
            return null;
        }

        return fileName;
    }

    private String stripExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }
}

