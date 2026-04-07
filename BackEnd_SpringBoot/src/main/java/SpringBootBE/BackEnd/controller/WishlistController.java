package SpringBootBE.BackEnd.controller;

import SpringBootBE.BackEnd.Service.WishlistService;
import SpringBootBE.BackEnd.model.Wishlist;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlists")
@CrossOrigin
@Transactional
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public ResponseEntity<List<Wishlist>> getAllWishlists() {
        return ResponseEntity.ok(wishlistService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Wishlist> getWishlistById(@PathVariable Integer id) {
        Wishlist wishlist = wishlistService.findById(id);
        return wishlist != null ? ResponseEntity.ok(wishlist) : ResponseEntity.notFound().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Wishlist>> getWishlistsByUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(wishlistService.findByUserId(userId));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Wishlist>> getWishlistsByCourse(@PathVariable Integer courseId) {
        return ResponseEntity.ok(wishlistService.findByCourseId(courseId));
    }

    @GetMapping("/user/{userId}/course/{courseId}")
    public ResponseEntity<Wishlist> getWishlistByUserAndCourse(@PathVariable Integer userId, @PathVariable Integer courseId) {
        Wishlist wishlist = wishlistService.findByUserIdAndCourseId(userId, courseId);
        return wishlist != null ? ResponseEntity.ok(wishlist) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Wishlist> createWishlist(@RequestBody Wishlist wishlist) {
        return ResponseEntity.ok(wishlistService.save(wishlist));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Wishlist> updateWishlist(@PathVariable Integer id, @RequestBody Wishlist wishlist) {
        Wishlist existingWishlist = wishlistService.findById(id);
        if (existingWishlist != null) {
            wishlist.setId(id);
            return ResponseEntity.ok(wishlistService.update(wishlist));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWishlist(@PathVariable Integer id) {
        wishlistService.delete(id);
        return ResponseEntity.ok().build();
    }
}

