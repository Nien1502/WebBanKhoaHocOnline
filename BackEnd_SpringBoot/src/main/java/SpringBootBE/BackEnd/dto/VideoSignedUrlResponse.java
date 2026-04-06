package SpringBootBE.BackEnd.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoSignedUrlResponse {
    private Integer lessonId;
    private Integer courseId;
    private String fileName;
    private String displayName;
    private String url;
    private long expiresAt;
}
