package SpringBootBE.BackEnd.Service;

import SpringBootBE.BackEnd.model.Wishlist;
import SpringBootBE.BackEnd.repository.WishlistRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;

    public WishlistServiceImpl(WishlistRepository wishlistRepository) {
        this.wishlistRepository = wishlistRepository;
    }

    @Override
    public List<Wishlist> findAll() {
        return wishlistRepository.findAll();
    }

    @Override
    public Wishlist findById(Integer id) {
        return wishlistRepository.findById(id).orElse(null);
    }

    @Override
    public List<Wishlist> findByUserId(Integer userId) {
        return wishlistRepository.findByUserId(userId);
    }

    @Override
    public List<Wishlist> findByCourseId(Integer courseId) {
        return wishlistRepository.findByCourseId(courseId);
    }

    @Override
    public Wishlist findByUserIdAndCourseId(Integer userId, Integer courseId) {
        return wishlistRepository.findByUserIdAndCourseId(userId, courseId);
    }

    @Override
    public Wishlist save(Wishlist wishlist) {
        return wishlistRepository.save(wishlist);
    }

    @Override
    public Wishlist update(Wishlist wishlist) {
        return wishlistRepository.save(wishlist);
    }

    @Override
    public void delete(Integer id) {
        wishlistRepository.deleteById(id);
    }
}

