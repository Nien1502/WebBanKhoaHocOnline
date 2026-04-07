package SpringBootBE.BackEnd.controller;

import SpringBootBE.BackEnd.Service.UserRoleService;
import SpringBootBE.BackEnd.model.UserRole;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-roles")
@CrossOrigin
@Transactional
public class UserRoleController {

    private final UserRoleService userRoleService;

    public UserRoleController(UserRoleService userRoleService) {
        this.userRoleService = userRoleService;
    }

    @GetMapping
    public ResponseEntity<List<UserRole>> getAllUserRoles() {
        return ResponseEntity.ok(userRoleService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserRole> getUserRoleById(@PathVariable Integer id) {
        UserRole userRole = userRoleService.findById(id);
        return userRole != null ? ResponseEntity.ok(userRole) : ResponseEntity.notFound().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserRole>> getUserRolesByUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(userRoleService.findByUserId(userId));
    }

    @GetMapping("/role/{roleId}")
    public ResponseEntity<List<UserRole>> getUserRolesByRole(@PathVariable Integer roleId) {
        return ResponseEntity.ok(userRoleService.findByRoleId(roleId));
    }

    @PostMapping
    public ResponseEntity<UserRole> createUserRole(@RequestBody UserRole userRole) {
        return ResponseEntity.ok(userRoleService.save(userRole));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserRole> updateUserRole(@PathVariable Integer id, @RequestBody UserRole userRole) {
        UserRole existingUserRole = userRoleService.findById(id);
        if (existingUserRole != null) {
            userRole.setId(id);
            return ResponseEntity.ok(userRoleService.update(userRole));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserRole(@PathVariable Integer id) {
        userRoleService.delete(id);
        return ResponseEntity.ok().build();
    }
}

