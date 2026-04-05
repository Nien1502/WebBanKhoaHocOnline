package SpringBootBE.BackEnd.Service;

import SpringBootBE.BackEnd.dto.VideoAssetResponse;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class VideoAssetService {

    private static final String VIDEO_RESOURCE_PATTERN = "classpath:/static/assets/videos/*";
    private static final String VIDEO_PUBLIC_PATH = "/assets/videos/";
    private static final List<String> SUPPORTED_EXTENSIONS = List.of(".mp4", ".webm", ".m4v", ".mov");

    private final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

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

    private String stripExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }
}

