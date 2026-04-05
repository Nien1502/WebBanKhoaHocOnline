package SpringBootBE.BackEnd.controller;

import SpringBootBE.BackEnd.Service.VideoAssetService;
import SpringBootBE.BackEnd.dto.VideoAssetResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VideoAssetControllerTest {

    @Mock
    private VideoAssetService videoAssetService;

    @InjectMocks
    private VideoAssetController videoAssetController;

    @Test
    void getAllVideos_ReturnsVideoList() {
        List<VideoAssetResponse> expected = List.of(
                new VideoAssetResponse("intro.mp4", "intro", "/assets/videos/intro.mp4"),
                new VideoAssetResponse("lesson1.mp4", "lesson1", "/assets/videos/lesson1.mp4")
        );
        when(videoAssetService.findAllVideos()).thenReturn(expected);

        ResponseEntity<List<VideoAssetResponse>> response = videoAssetController.getAllVideos();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
        verify(videoAssetService).findAllVideos();
    }

    @Test
    void getVideoByFileName_WhenExists_ReturnsOk() {
        VideoAssetResponse expected = new VideoAssetResponse("intro.mp4", "intro", "/assets/videos/intro.mp4");
        when(videoAssetService.findVideoByFileName("intro.mp4")).thenReturn(Optional.of(expected));

        ResponseEntity<VideoAssetResponse> response = videoAssetController.getVideoByFileName("intro.mp4");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
    }

    @Test
    void getVideoByFileName_WhenMissing_ReturnsNotFound() {
        when(videoAssetService.findVideoByFileName("missing.mp4")).thenReturn(Optional.empty());

        ResponseEntity<VideoAssetResponse> response = videoAssetController.getVideoByFileName("missing.mp4");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }
}

