package SpringBootBE.BackEnd.controller;

import SpringBootBE.BackEnd.Service.PermissionService;
import SpringBootBE.BackEnd.model.Permission;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@CrossOrigin
@Transactional
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    public ResponseEntity<List<Permission>> getAllPermissions() {
        return ResponseEntity.ok(permissionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Permission> getPermissionById(@PathVariable Integer id) {
        Permission permission = permissionService.findById(id);
        return permission != null ? ResponseEntity.ok(permission) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Permission> createPermission(@RequestBody Permission permission) {
        return ResponseEntity.ok(permissionService.save(permission));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Permission> updatePermission(@PathVariable Integer id, @RequestBody Permission permission) {
        Permission existingPermission = permissionService.findById(id);
        if (existingPermission != null) {
            permission.setId(id);
            return ResponseEntity.ok(permissionService.update(permission));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePermission(@PathVariable Integer id) {
        permissionService.delete(id);
        return ResponseEntity.ok().build();
    }
}

