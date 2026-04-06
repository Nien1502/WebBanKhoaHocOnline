package SpringBootBE.BackEnd.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoAssetResponse {
    private String fileName;
    private String displayName;
    private String url;
}

