package SpringBootBE.BackEnd.controller;

import SpringBootBE.BackEnd.Service.OrderDetailService;
import SpringBootBE.BackEnd.model.OrderDetail;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order-details")
@CrossOrigin
@Transactional
public class OrderDetailController {

    private final OrderDetailService orderDetailService;

    public OrderDetailController(OrderDetailService orderDetailService) {
        this.orderDetailService = orderDetailService;
    }

    @GetMapping
    public ResponseEntity<List<OrderDetail>> getAllOrderDetails() {
        return ResponseEntity.ok(orderDetailService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDetail> getOrderDetailById(@PathVariable Integer id) {
        OrderDetail orderDetail = orderDetailService.findById(id);
        return orderDetail != null ? ResponseEntity.ok(orderDetail) : ResponseEntity.notFound().build();
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderDetail>> getOrderDetailsByOrder(@PathVariable Integer orderId) {
        return ResponseEntity.ok(orderDetailService.findByOrderId(orderId));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<OrderDetail>> getOrderDetailsByCourse(@PathVariable Integer courseId) {
        return ResponseEntity.ok(orderDetailService.findByCourseId(courseId));
    }

    @PostMapping
    public ResponseEntity<OrderDetail> createOrderDetail(@RequestBody OrderDetail orderDetail) {
        return ResponseEntity.ok(orderDetailService.save(orderDetail));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderDetail> updateOrderDetail(@PathVariable Integer id, @RequestBody OrderDetail orderDetail) {
        OrderDetail existingOrderDetail = orderDetailService.findById(id);
        if (existingOrderDetail != null) {
            orderDetail.setId(id);
            return ResponseEntity.ok(orderDetailService.update(orderDetail));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrderDetail(@PathVariable Integer id) {
        orderDetailService.delete(id);
        return ResponseEntity.ok().build();
    }
}

