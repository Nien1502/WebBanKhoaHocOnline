package SpringBootBE.BackEnd.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String message;
    private UserInfo user;
    private String token;
    private String tokenType;
    private Long expiresAt;

    public LoginResponse(String message, UserInfo user) {
        this.message = message;
        this.user = user;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Integer id;
        private String email;
        private String username;
        private String role;
        private String access;
    }
}
