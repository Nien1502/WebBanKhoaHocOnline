package SpringBootBE.BackEnd.repository;

import SpringBootBE.BackEnd.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Integer> {
    List<Wishlist> findByUserId(Integer userId);
    List<Wishlist> findByCourseId(Integer courseId);
    Wishlist findByUserIdAndCourseId(Integer userId, Integer courseId);
}

