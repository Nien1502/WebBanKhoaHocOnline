package SpringBootBE.BackEnd.controller;

import SpringBootBE.BackEnd.Service.PaymentService;
import SpringBootBE.BackEnd.config.JwtTokenService;
import SpringBootBE.BackEnd.dto.MomoCallbackResponse;
import SpringBootBE.BackEnd.dto.MomoPaymentRequest;
import SpringBootBE.BackEnd.dto.MomoPaymentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment/momo")
@CrossOrigin
public class MomoPaymentController {

    private final PaymentService paymentService;
    private final JwtTokenService jwtTokenService;

    public MomoPaymentController(PaymentService paymentService,
                                 JwtTokenService jwtTokenService) {
        this.paymentService = paymentService;
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createPayment(@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
                                           @RequestBody MomoPaymentRequest request) {
        JwtTokenService.AuthPrincipal authPrincipal;
        try {
            authPrincipal = jwtTokenService.parseAuthorizationHeader(authorizationHeader);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBody(exception.getMessage()));
        }

        if (!authPrincipal.isStudent()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(errorBody("Chỉ Student được phép khởi tạo thanh toán MoMo."));
        }

        if (request != null && request.getUserId() != null && !request.getUserId().equals(authPrincipal.userId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(errorBody("userId không khớp với người dùng trong token."));
        }

        if (request != null) {
            request.setUserId(authPrincipal.userId());
        }

        try {
            MomoPaymentResponse response = paymentService.createMomoPayment(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(errorBody(exception.getMessage()));
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorBody(exception.getMessage()));
        }
    }

    @GetMapping("/callback")
    public ResponseEntity<MomoCallbackResponse> handleReturn(@RequestParam Map<String, String> callbackData) {
        return ResponseEntity.ok(paymentService.handleMomoReturn(callbackData));
    }

    @PostMapping({"/ipn", "/callback"})
    public ResponseEntity<Map<String, Object>> handleIpn(@RequestBody Map<String, Object> callbackData) {
        return ResponseEntity.ok(paymentService.handleMomoIpn(toStringMap(callbackData)));
    }

    private Map<String, Object> errorBody(String message) {
        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        body.put("message", message);
        body.put("success", false);
        return body;
    }

    private Map<String, String> toStringMap(Map<String, Object> body) {
        LinkedHashMap<String, String> values = new LinkedHashMap<>();
        if (body != null) {
            body.forEach((key, value) -> values.put(key, value == null ? "" : String.valueOf(value)));
        }
        return values;
    }
}

