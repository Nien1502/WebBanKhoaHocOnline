package SpringBootBE.BackEnd.controller;

import SpringBootBE.BackEnd.Service.PaymentService;
import SpringBootBE.BackEnd.config.JwtTokenService;
import SpringBootBE.BackEnd.dto.MomoCallbackResponse;
import SpringBootBE.BackEnd.dto.MomoPaymentRequest;
import SpringBootBE.BackEnd.dto.MomoPaymentResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MomoPaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private JwtTokenService jwtTokenService;

    @InjectMocks
    private MomoPaymentController momoPaymentController;

    @Test
    void createPayment_WhenValidRequest_ReturnsOk() {
        String authHeader = "Bearer valid-token";
        MomoPaymentRequest request = new MomoPaymentRequest();
        request.setUserId(1);
        request.setCourseIds(List.of(2, 3));
        JwtTokenService.AuthPrincipal authPrincipal = new JwtTokenService.AuthPrincipal(
                1,
                "student@mail.com",
                "Student",
                "student",
                9999999999L
        );
        when(jwtTokenService.parseAuthorizationHeader(authHeader)).thenReturn(authPrincipal);

        MomoPaymentResponse expected = new MomoPaymentResponse();
        expected.setMessage("Khởi tạo thanh toán thành công.");
        expected.setResultCode(0);
        when(paymentService.createMomoPayment(request)).thenReturn(expected);

        ResponseEntity<?> response = momoPaymentController.createPayment(authHeader, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
        assertEquals(1, request.getUserId());
        verify(jwtTokenService).parseAuthorizationHeader(authHeader);
        verify(paymentService).createMomoPayment(request);
    }

    @Test
    void createPayment_WhenMissingAuth_ReturnsUnauthorized() {
        MomoPaymentRequest request = new MomoPaymentRequest();
        when(jwtTokenService.parseAuthorizationHeader(null)).thenThrow(new IllegalArgumentException("Thiếu Authorization header."));

        ResponseEntity<?> response = momoPaymentController.createPayment(null, request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        Map<?, ?> body = assertInstanceOf(Map.class, response.getBody());
        assertEquals("Thiếu Authorization header.", body.get("message"));
        assertFalse((Boolean) body.get("success"));
    }

    @Test
    void createPayment_WhenRoleIsNotStudent_ReturnsForbidden() {
        String authHeader = "Bearer admin-token";
        MomoPaymentRequest request = new MomoPaymentRequest();
        JwtTokenService.AuthPrincipal authPrincipal = new JwtTokenService.AuthPrincipal(
                1,
                "admin@mail.com",
                "Admin",
                "admin",
                9999999999L
        );
        when(jwtTokenService.parseAuthorizationHeader(authHeader)).thenReturn(authPrincipal);

        ResponseEntity<?> response = momoPaymentController.createPayment(authHeader, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        Map<?, ?> body = assertInstanceOf(Map.class, response.getBody());
        assertEquals("Chỉ Student được phép khởi tạo thanh toán MoMo.", body.get("message"));
        assertFalse((Boolean) body.get("success"));
    }

    @Test
    void createPayment_WhenRequestUserMismatch_ReturnsForbidden() {
        String authHeader = "Bearer valid-token";
        MomoPaymentRequest request = new MomoPaymentRequest();
        request.setUserId(99);

        JwtTokenService.AuthPrincipal authPrincipal = new JwtTokenService.AuthPrincipal(
                1,
                "student@mail.com",
                "Student",
                "student",
                9999999999L
        );
        when(jwtTokenService.parseAuthorizationHeader(authHeader)).thenReturn(authPrincipal);

        ResponseEntity<?> response = momoPaymentController.createPayment(authHeader, request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        Map<?, ?> body = assertInstanceOf(Map.class, response.getBody());
        assertEquals("userId không khớp với người dùng trong token.", body.get("message"));
        assertFalse((Boolean) body.get("success"));
    }

    @Test
    void createPayment_WhenInvalidRequest_ReturnsBadRequestErrorBody() {
        String authHeader = "Bearer valid-token";
        MomoPaymentRequest request = new MomoPaymentRequest();
        request.setCourseIds(List.of());
        JwtTokenService.AuthPrincipal authPrincipal = new JwtTokenService.AuthPrincipal(
                1,
                "student@mail.com",
                "Student",
                "student",
                9999999999L
        );
        when(jwtTokenService.parseAuthorizationHeader(authHeader)).thenReturn(authPrincipal);
        when(paymentService.createMomoPayment(request)).thenThrow(new IllegalArgumentException("Danh sách khóa học không được để trống."));

        ResponseEntity<?> response = momoPaymentController.createPayment(authHeader, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<?, ?> body = assertInstanceOf(Map.class, response.getBody());
        assertEquals("Danh sách khóa học không được để trống.", body.get("message"));
        assertFalse((Boolean) body.get("success"));
        assertEquals(1, request.getUserId());
    }

    @Test
    void createPayment_WhenGatewayError_ReturnsBadGatewayErrorBody() {
        String authHeader = "Bearer valid-token";
        MomoPaymentRequest request = new MomoPaymentRequest();
        request.setCourseIds(List.of(1));
        JwtTokenService.AuthPrincipal authPrincipal = new JwtTokenService.AuthPrincipal(
                1,
                "student@mail.com",
                "Student",
                "student",
                9999999999L
        );
        when(jwtTokenService.parseAuthorizationHeader(authHeader)).thenReturn(authPrincipal);
        when(paymentService.createMomoPayment(request)).thenThrow(new IllegalStateException("MoMo API lỗi."));

        ResponseEntity<?> response = momoPaymentController.createPayment(authHeader, request);

        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        Map<?, ?> body = assertInstanceOf(Map.class, response.getBody());
        assertEquals("MoMo API lỗi.", body.get("message"));
        assertFalse((Boolean) body.get("success"));
    }

    @Test
    void handleReturn_DelegatesToService() {
        Map<String, String> callbackData = Map.of("orderId", "MOMO_1_ABC", "requestId", "req-1");
        MomoCallbackResponse expected = new MomoCallbackResponse();
        expected.setStatus("SUCCESS");
        when(paymentService.handleMomoReturn(callbackData)).thenReturn(expected);

        ResponseEntity<MomoCallbackResponse> response = momoPaymentController.handleReturn(callbackData);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
        verify(paymentService).handleMomoReturn(callbackData);
    }

    @Test
    void handleIpn_ConvertsBodyValuesToStringBeforeDelegating() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("orderId", "MOMO_1_ABC");
        body.put("resultCode", 0);
        body.put("transId", 123456789L);
        body.put("message", null);

        Map<String, Object> ipnResponse = Map.of("resultCode", 0, "message", "OK");
        when(paymentService.handleMomoIpn(org.mockito.ArgumentMatchers.anyMap())).thenReturn(ipnResponse);

        ResponseEntity<Map<String, Object>> response = momoPaymentController.handleIpn(body);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ipnResponse, response.getBody());

        verify(paymentService).handleMomoIpn(argThat(delegatedData -> {
            assertNotNull(delegatedData);
            boolean matched = "MOMO_1_ABC".equals(delegatedData.get("orderId"))
                    && "0".equals(delegatedData.get("resultCode"))
                    && "123456789".equals(delegatedData.get("transId"))
                    && "".equals(delegatedData.get("message"));
            assertTrue(matched);
            return matched;
        }));
    }
}
