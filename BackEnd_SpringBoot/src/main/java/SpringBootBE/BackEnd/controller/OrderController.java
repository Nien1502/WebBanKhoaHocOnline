package SpringBootBE.BackEnd.controller;

import SpringBootBE.BackEnd.Service.OrderService;
import SpringBootBE.BackEnd.config.JwtTokenService;
import SpringBootBE.BackEnd.model.Order;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin
@Transactional
public class OrderController {

    private final OrderService orderService;
    private final JwtTokenService jwtTokenService;

    public OrderController(OrderService orderService,
                           JwtTokenService jwtTokenService) {
        this.orderService = orderService;
        this.jwtTokenService = jwtTokenService;
    }

    @GetMapping
    public ResponseEntity<?> getAllOrders(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        JwtTokenService.AuthPrincipal authPrincipal = requireAuthenticatedUser(authorizationHeader);
        if (authPrincipal == null) {
            return unauthorized("Token không hợp lệ hoặc đã hết hạn.");
        }
        if (!authPrincipal.isAdmin()) {
            return forbidden("Chỉ Admin được xem toàn bộ đơn hàng.");
        }
        return ResponseEntity.ok(orderService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
                                          @PathVariable Integer id) {
        JwtTokenService.AuthPrincipal authPrincipal = requireAuthenticatedUser(authorizationHeader);
        if (authPrincipal == null) {
            return unauthorized("Token không hợp lệ hoặc đã hết hạn.");
        }

        Order order = orderService.findById(id);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        Integer ownerId = order.getUser() != null ? order.getUser().getId() : null;
        if (!authPrincipal.isAdmin() && !Objects.equals(authPrincipal.userId(), ownerId)) {
            return forbidden("Bạn không có quyền xem đơn hàng này.");
        }

        return ResponseEntity.ok(order);
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyOrders(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        JwtTokenService.AuthPrincipal authPrincipal = requireAuthenticatedUser(authorizationHeader);
        if (authPrincipal == null) {
            return unauthorized("Token không hợp lệ hoặc đã hết hạn.");
        }
        return ResponseEntity.ok(orderService.findByUserId(authPrincipal.userId()));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getOrdersByUser(@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
                                             @PathVariable Integer userId) {
        JwtTokenService.AuthPrincipal authPrincipal = requireAuthenticatedUser(authorizationHeader);
        if (authPrincipal == null) {
            return unauthorized("Token không hợp lệ hoặc đã hết hạn.");
        }

        if (!authPrincipal.isAdmin() && !Objects.equals(authPrincipal.userId(), userId)) {
            return forbidden("Bạn chỉ được xem đơn hàng của chính mình.");
        }

        return ResponseEntity.ok(orderService.findByUserId(userId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<?> getOrdersByStatus(@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
                                               @PathVariable String status) {
        JwtTokenService.AuthPrincipal authPrincipal = requireAuthenticatedUser(authorizationHeader);
        if (authPrincipal == null) {
            return unauthorized("Token không hợp lệ hoặc đã hết hạn.");
        }
        if (!authPrincipal.isAdmin()) {
            return forbidden("Chỉ Admin được lọc đơn theo trạng thái.");
        }

        try {
            Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            return ResponseEntity.ok(orderService.findByStatus(orderStatus));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
                                         @RequestBody Order order) {
        JwtTokenService.AuthPrincipal authPrincipal = requireAuthenticatedUser(authorizationHeader);
        if (authPrincipal == null) {
            return unauthorized("Token không hợp lệ hoặc đã hết hạn.");
        }
        if (!authPrincipal.isAdmin()) {
            return forbidden("Không cho phép Student tạo order trực tiếp. Hãy dùng API thanh toán.");
        }

        return ResponseEntity.ok(orderService.save(order));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrder(@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
                                         @PathVariable Integer id,
                                         @RequestBody Order order) {
        JwtTokenService.AuthPrincipal authPrincipal = requireAuthenticatedUser(authorizationHeader);
        if (authPrincipal == null) {
            return unauthorized("Token không hợp lệ hoặc đã hết hạn.");
        }
        if (!authPrincipal.isAdmin()) {
            return forbidden("Chỉ Admin được cập nhật đơn hàng.");
        }

        Order existingOrder = orderService.findById(id);
        if (existingOrder != null) {
            order.setId(id);
            return ResponseEntity.ok(orderService.update(order));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
                                         @PathVariable Integer id) {
        JwtTokenService.AuthPrincipal authPrincipal = requireAuthenticatedUser(authorizationHeader);
        if (authPrincipal == null) {
            return unauthorized("Token không hợp lệ hoặc đã hết hạn.");
        }
        if (!authPrincipal.isAdmin()) {
            return forbidden("Chỉ Admin được xóa đơn hàng.");
        }

        orderService.delete(id);
        return ResponseEntity.ok().build();
    }

    private JwtTokenService.AuthPrincipal requireAuthenticatedUser(String authorizationHeader) {
        try {
            return jwtTokenService.parseAuthorizationHeader(authorizationHeader);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private ResponseEntity<Map<String, Object>> unauthorized(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBody(message));
    }

    private ResponseEntity<Map<String, Object>> forbidden(String message) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody(message));
    }

    private Map<String, Object> errorBody(String message) {
        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        body.put("message", message);
        body.put("success", false);
        return body;
    }
}

