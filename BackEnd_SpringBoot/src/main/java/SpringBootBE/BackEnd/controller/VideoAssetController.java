package SpringBootBE.BackEnd.controller;

import SpringBootBE.BackEnd.Service.VideoAssetService;
import SpringBootBE.BackEnd.dto.VideoAssetResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/videos")
@CrossOrigin
public class VideoAssetController {

    private final VideoAssetService videoAssetService;

    public VideoAssetController(VideoAssetService videoAssetService) {
        this.videoAssetService = videoAssetService;
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
}

