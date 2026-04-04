package SpringBootBE.BackEnd.controller;

import SpringBootBE.BackEnd.Service.AuthService;
import SpringBootBE.BackEnd.dto.LoginRequest;
import SpringBootBE.BackEnd.dto.LoginResponse;
import SpringBootBE.BackEnd.dto.RegisterRequest;
import SpringBootBE.BackEnd.dto.RegisterResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void login_MissingCredentials_ReturnsBadRequest() {
        LoginRequest request = new LoginRequest("", "secret");

        ResponseEntity<?> response = authController.login(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        LoginResponse body = assertInstanceOf(LoginResponse.class, response.getBody());
        assertEquals("Email và mật khẩu là bắt buộc.", body.getMessage());
        verifyNoInteractions(authService);
    }

    @Test
    void login_InvalidCredentials_ReturnsUnauthorized() {
        LoginRequest request = new LoginRequest("user@mail.com", "wrong");
        LoginResponse serviceResponse = new LoginResponse("Sai tài khoản hoặc mật khẩu.", null);
        when(authService.login("user@mail.com", "wrong")).thenReturn(serviceResponse);

        ResponseEntity<?> response = authController.login(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(serviceResponse, response.getBody());
        verify(authService).login("user@mail.com", "wrong");
    }

    @Test
    void login_ValidCredentials_ReturnsOk() {
        LoginRequest request = new LoginRequest("user@mail.com", "correct");
        LoginResponse.UserInfo user = new LoginResponse.UserInfo(1, "user@mail.com", "user1", "Student", "ACTIVE");
        LoginResponse serviceResponse = new LoginResponse("Đăng nhập thành công.", user, "jwt-token", "Bearer", 99999L);
        when(authService.login("user@mail.com", "correct")).thenReturn(serviceResponse);

        ResponseEntity<?> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(serviceResponse, response.getBody());
    }

    @Test
    void register_Success_ReturnsCreated() {
        RegisterRequest request = new RegisterRequest("newuser", "new@mail.com", "123456");
        RegisterResponse.UserInfo user = new RegisterResponse.UserInfo(2, "new@mail.com", "newuser", "Student");
        RegisterResponse serviceResponse = new RegisterResponse("Đăng ký thành công.", user);
        when(authService.register(request)).thenReturn(serviceResponse);

        ResponseEntity<RegisterResponse> response = authController.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Đăng ký thành công.", response.getBody().getMessage());
    }

    @Test
    void register_EmailExists_ReturnsConflict() {
        RegisterRequest request = new RegisterRequest("newuser", "existing@mail.com", "123456");
        RegisterResponse serviceResponse = new RegisterResponse("Email đã tồn tại.", null);
        when(authService.register(request)).thenReturn(serviceResponse);

        ResponseEntity<RegisterResponse> response = authController.register(request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(serviceResponse, response.getBody());
    }

    @Test
    void register_SystemMissingStudentRole_ReturnsInternalServerError() {
        RegisterRequest request = new RegisterRequest("newuser", "new@mail.com", "123456");
        RegisterResponse serviceResponse = new RegisterResponse("Hệ thống chưa cấu hình role Student.", null);
        when(authService.register(request)).thenReturn(serviceResponse);

        ResponseEntity<RegisterResponse> response = authController.register(request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(serviceResponse, response.getBody());
    }
}

