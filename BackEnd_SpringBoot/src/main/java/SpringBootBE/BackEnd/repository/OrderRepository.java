package SpringBootBE.BackEnd.repository;

import SpringBootBE.BackEnd.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByUserId(Integer userId);
    List<Order> findByStatus(Order.OrderStatus status);
    List<Order> findByStatusAndOrderDateBefore(Order.OrderStatus status, LocalDateTime orderDate);
}

