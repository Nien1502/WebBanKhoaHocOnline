package SpringBootBE.BackEnd.controller;

import SpringBootBE.BackEnd.Service.AuthService;
import SpringBootBE.BackEnd.dto.LoginRequest;
import SpringBootBE.BackEnd.dto.LoginResponse;
import SpringBootBE.BackEnd.dto.RegisterRequest;
import SpringBootBE.BackEnd.dto.RegisterResponse;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
@Transactional
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        if (loginRequest.getEmail() == null || loginRequest.getEmail().isEmpty() ||
            loginRequest.getPassword() == null || loginRequest.getPassword().isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new LoginResponse("Email và mật khẩu là bắt buộc.", null));
        }

        LoginResponse response = authService.login(loginRequest.getEmail(), loginRequest.getPassword());

        if (response.getUser() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest registerRequest) {
        RegisterResponse response = authService.register(registerRequest);
        if (response.getUser() != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        String message = response.getMessage() == null ? "Đăng ký thất bại." : response.getMessage();

        if ("Email đã tồn tại.".equals(message) || "Username đã tồn tại.".equals(message)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        if ("Hệ thống chưa cấu hình role Student.".equals(message)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @GetMapping("/users/non-admins/count")
    public ResponseEntity<Map<String, Object>> getNonAdminUserCount() {
        Integer count = authService.getNonAdminUserCount();
        LinkedHashMap<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Số lượng user không có quyền admin.");
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/checklogin")
    public ResponseEntity<?> checkLogin(@RequestBody LoginRequest loginRequest) {
        if (loginRequest.getEmail() == null || loginRequest.getEmail().isEmpty() ||
            loginRequest.getPassword() == null || loginRequest.getPassword().isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new LoginResponse("Email và mật khẩu là bắt buộc.", null));
        }

        LoginResponse response = authService.login(loginRequest.getEmail(), loginRequest.getPassword());

        if (response.getUser() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        return ResponseEntity.ok(response);
    }
}
