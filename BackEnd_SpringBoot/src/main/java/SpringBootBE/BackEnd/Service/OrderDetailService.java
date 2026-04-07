package SpringBootBE.BackEnd.Service;

import SpringBootBE.BackEnd.model.OrderDetail;
import java.util.List;

public interface OrderDetailService {
    List<OrderDetail> findAll();
    OrderDetail findById(Integer id);
    List<OrderDetail> findByOrderId(Integer orderId);
    List<OrderDetail> findByCourseId(Integer courseId);
    OrderDetail save(OrderDetail orderDetail);
    OrderDetail update(OrderDetail orderDetail);
    void delete(Integer id);
}

