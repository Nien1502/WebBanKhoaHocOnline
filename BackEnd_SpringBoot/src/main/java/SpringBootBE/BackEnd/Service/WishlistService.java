package SpringBootBE.BackEnd.Service;

import SpringBootBE.BackEnd.model.Wishlist;
import java.util.List;

public interface WishlistService {
    List<Wishlist> findAll();
    Wishlist findById(Integer id);
    List<Wishlist> findByUserId(Integer userId);
    List<Wishlist> findByCourseId(Integer courseId);
    Wishlist findByUserIdAndCourseId(Integer userId, Integer courseId);
    Wishlist save(Wishlist wishlist);
    Wishlist update(Wishlist wishlist);
    void delete(Integer id);
}

