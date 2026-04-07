package SpringBootBE.BackEnd.controller;

import SpringBootBE.BackEnd.Service.LessonService;
import SpringBootBE.BackEnd.config.JwtTokenService;
import SpringBootBE.BackEnd.model.Enrollment;
import SpringBootBE.BackEnd.model.Lesson;
import SpringBootBE.BackEnd.repository.EnrollmentRepository;
import jakarta.transaction.Transactional;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/lessons")
@CrossOrigin
@Transactional
public class LessonController {

    private static final List<String> SUPPORTED_VIDEO_EXTENSIONS = List.of(".mp4", ".webm", ".m4v", ".mov");
    private static final long VIDEO_CHUNK_SIZE = 1_024 * 1_024;

    private final LessonService lessonService;
    private final EnrollmentRepository enrollmentRepository;
    private final JwtTokenService jwtTokenService;

    public LessonController(LessonService lessonService,
                            EnrollmentRepository enrollmentRepository,
                            JwtTokenService jwtTokenService) {
        this.lessonService = lessonService;
        this.enrollmentRepository = enrollmentRepository;
        this.jwtTokenService = jwtTokenService;
    }

    @GetMapping
    public ResponseEntity<List<Lesson>> getAllLessons() {
        return ResponseEntity.ok(lessonService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Lesson> getLessonById(@PathVariable Integer id) {
        Lesson lesson = lessonService.findById(id);
        return lesson != null ? ResponseEntity.ok(lesson) : ResponseEntity.notFound().build();
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Lesson>> getLessonsByCourse(@PathVariable Integer courseId) {
        return ResponseEntity.ok(lessonService.findByCourseId(courseId));
    }

    @GetMapping("/course/{courseId}/ordered")
    public ResponseEntity<List<Lesson>> getLessonsByCourseOrdered(@PathVariable Integer courseId) {
        return ResponseEntity.ok(lessonService.findByCourseIdOrderByOrderIndex(courseId));
    }

    @GetMapping("/{id}/video")
    public ResponseEntity<?> streamLessonVideo(@PathVariable Integer id,
                                               @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
                                               @RequestParam(value = "token", required = false) String token,
                                               @RequestHeader HttpHeaders requestHeaders) {
        JwtTokenService.AuthPrincipal authPrincipal = requireAuthenticatedUser(authorizationHeader, token);
        if (authPrincipal == null) {
            return unauthorized("Token không hợp lệ hoặc đã hết hạn.");
        }

        Lesson lesson = lessonService.findById(id);
        if (lesson == null) {
            return ResponseEntity.notFound().build();
        }

        Integer courseId = lesson.getCourse() != null ? lesson.getCourse().getId() : null;
        if (courseId == null) {
            return ResponseEntity.badRequest().body(errorBody("Bài học không gắn với khóa học hợp lệ."));
        }

        if (!authPrincipal.isAdmin()) {
            Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(authPrincipal.userId(), courseId);
            if (enrollment == null) {
                return forbidden("Bạn chưa mua khóa học này.");
            }
        }

        String videoFileName = extractVideoFileName(lesson.getVideoURL());
        if (videoFileName == null) {
            return ResponseEntity.notFound().build();
        }

        Resource videoResource = new ClassPathResource("static/assets/videos/" + videoFileName);
        if (!videoResource.exists() || !videoResource.isReadable()) {
            return ResponseEntity.notFound().build();
        }

        try {
            long contentLength = videoResource.contentLength();
            if (contentLength <= 0) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(errorBody("Không thể đọc dữ liệu video."));
            }

            MediaType mediaType = MediaTypeFactory.getMediaType(videoFileName)
                    .orElse(MediaType.APPLICATION_OCTET_STREAM);

            ResourceRegion region;
            List<HttpRange> ranges = requestHeaders.getRange();
            if (ranges == null || ranges.isEmpty()) {
                region = new ResourceRegion(videoResource, 0, Math.min(VIDEO_CHUNK_SIZE, contentLength));
            } else {
                ResourceRegion requestedRegion = ranges.get(0).toResourceRegion(videoResource);
                long regionSize = Math.min(requestedRegion.getCount(), VIDEO_CHUNK_SIZE);
                region = new ResourceRegion(videoResource, requestedRegion.getPosition(), regionSize);
            }

            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set(HttpHeaders.ACCEPT_RANGES, "bytes");
            responseHeaders.setCacheControl("no-store, no-cache, must-revalidate, max-age=0");
            responseHeaders.setPragma("no-cache");

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .headers(responseHeaders)
                    .contentType(mediaType)
                    .body(region);
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorBody("Không thể phát video."));
        }
    }

    @PostMapping
    public ResponseEntity<Lesson> createLesson(@RequestBody Lesson lesson) {
        try {
            return ResponseEntity.ok(lessonService.save(lesson));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Lesson> updateLesson(@PathVariable Integer id, @RequestBody Lesson lesson) {
        Lesson existingLesson = lessonService.findById(id);
        if (existingLesson != null) {
            lesson.setId(id);
            try {
                return ResponseEntity.ok(lessonService.update(lesson));
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLesson(@PathVariable Integer id) {
        lessonService.delete(id);
        return ResponseEntity.ok().build();
    }

    private JwtTokenService.AuthPrincipal requireAuthenticatedUser(String authorizationHeader, String rawToken) {
        try {
            if (authorizationHeader != null && !authorizationHeader.isBlank()) {
                return jwtTokenService.parseAuthorizationHeader(authorizationHeader);
            }

            if (rawToken == null || rawToken.isBlank()) {
                return null;
            }

            String token = rawToken.trim();
            if (token.regionMatches(true, 0, "Bearer ", 0, 7)) {
                token = token.substring(7).trim();
            }

            if (token.isEmpty()) {
                return null;
            }

            return jwtTokenService.parseToken(token);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private ResponseEntity<Map<String, Object>> unauthorized(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBody(message));
    }

    private ResponseEntity<Map<String, Object>> forbidden(String message) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody(message));
    }

    private Map<String, Object> errorBody(String message) {
        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        body.put("message", message);
        body.put("success", false);
        return body;
    }

    private String extractVideoFileName(String rawVideoUrl) {
        if (rawVideoUrl == null || rawVideoUrl.isBlank()) {
            return null;
        }

        String normalized = rawVideoUrl.trim();
        int queryIndex = normalized.indexOf('?');
        if (queryIndex >= 0) {
            normalized = normalized.substring(0, queryIndex);
        }

        int hashIndex = normalized.indexOf('#');
        if (hashIndex >= 0) {
            normalized = normalized.substring(0, hashIndex);
        }

        normalized = normalized.replace('\\', '/');
        int lastSlash = normalized.lastIndexOf('/');
        String fileName = lastSlash >= 0 ? normalized.substring(lastSlash + 1).trim() : normalized.trim();
        if (fileName.isEmpty()) {
            return null;
        }

        try {
            fileName = URLDecoder.decode(fileName, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException exception) {
            return null;
        }

        String loweredName = fileName.toLowerCase(Locale.ROOT);
        boolean supportedExtension = SUPPORTED_VIDEO_EXTENSIONS.stream().anyMatch(loweredName::endsWith);
        if (!supportedExtension) {
            return null;
        }

        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            return null;
        }

        return fileName;
    }

}
