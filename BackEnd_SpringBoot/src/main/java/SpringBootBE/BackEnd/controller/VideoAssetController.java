package SpringBootBE.BackEnd.controller;

import SpringBootBE.BackEnd.Service.VideoAssetService;
import SpringBootBE.BackEnd.config.JwtTokenService;
import SpringBootBE.BackEnd.dto.VideoAssetResponse;
import SpringBootBE.BackEnd.dto.VideoSignedUrlResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/videos")
@CrossOrigin
public class VideoAssetController {

    private final VideoAssetService videoAssetService;
    private final JwtTokenService jwtTokenService;

    public VideoAssetController(VideoAssetService videoAssetService,
                                JwtTokenService jwtTokenService) {
        this.videoAssetService = videoAssetService;
        this.jwtTokenService = jwtTokenService;
    }

    @GetMapping
    public ResponseEntity<List<VideoAssetResponse>> getAllVideos() {
        return ResponseEntity.ok(videoAssetService.findAllVideos());
    }

    @GetMapping("/{fileName:.+}")
    public ResponseEntity<VideoAssetResponse> getVideoByFileName(@PathVariable String fileName) {
        return videoAssetService.findVideoByFileName(fileName)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/lessons/{lessonId}/signed-url")
    public ResponseEntity<?> getSignedUrlByLesson(@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
                                                  @PathVariable Integer lessonId) {
        JwtTokenService.AuthPrincipal authPrincipal;
        try {
            authPrincipal = jwtTokenService.parseAuthorizationHeader(authorizationHeader);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBody(exception.getMessage()));
        }

        try {
            VideoSignedUrlResponse response = videoAssetService.generateSignedVideoUrl(lessonId, authPrincipal);
            return ResponseEntity.ok(response);
        } catch (SecurityException exception) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody(exception.getMessage()));
        } catch (NoSuchElementException exception) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody(exception.getMessage()));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(errorBody(exception.getMessage()));
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorBody(exception.getMessage()));
        }
    }

    private Map<String, Object> errorBody(String message) {
        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        body.put("message", message);
        body.put("success", false);
        return body;
    }
}

