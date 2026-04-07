package SpringBootBE.BackEnd.Service;

import SpringBootBE.BackEnd.model.Order;
import java.util.List;

public interface OrderService {
    List<Order> findAll();
    Order findById(Integer id);
    List<Order> findByUserId(Integer userId);
    List<Order> findByStatus(Order.OrderStatus status);
    Order save(Order order);
    Order update(Order order);
    void delete(Integer id);
}

