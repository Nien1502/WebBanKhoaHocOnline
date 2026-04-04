package SpringBootBE.BackEnd.controller;

import SpringBootBE.BackEnd.Service.RolePermissionService;
import SpringBootBE.BackEnd.model.RolePermission;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/role-permissions")
@CrossOrigin
@Transactional
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    public RolePermissionController(RolePermissionService rolePermissionService) {
        this.rolePermissionService = rolePermissionService;
    }

    @GetMapping
    public ResponseEntity<List<RolePermission>> getAllRolePermissions() {
        return ResponseEntity.ok(rolePermissionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RolePermission> getRolePermissionById(@PathVariable Integer id) {
        RolePermission rolePermission = rolePermissionService.findById(id);
        return rolePermission != null ? ResponseEntity.ok(rolePermission) : ResponseEntity.notFound().build();
    }

    @GetMapping("/role/{roleId}")
    public ResponseEntity<List<RolePermission>> getRolePermissionsByRole(@PathVariable Integer roleId) {
        return ResponseEntity.ok(rolePermissionService.findByRoleId(roleId));
    }

    @GetMapping("/permission/{permissionId}")
    public ResponseEntity<List<RolePermission>> getRolePermissionsByPermission(@PathVariable Integer permissionId) {
        return ResponseEntity.ok(rolePermissionService.findByPermissionId(permissionId));
    }

    @PostMapping
    public ResponseEntity<RolePermission> createRolePermission(@RequestBody RolePermission rolePermission) {
        return ResponseEntity.ok(rolePermissionService.save(rolePermission));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RolePermission> updateRolePermission(@PathVariable Integer id, @RequestBody RolePermission rolePermission) {
        RolePermission existingRolePermission = rolePermissionService.findById(id);
        if (existingRolePermission != null) {
            rolePermission.setId(id);
            return ResponseEntity.ok(rolePermissionService.update(rolePermission));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRolePermission(@PathVariable Integer id) {
        rolePermissionService.delete(id);
        return ResponseEntity.ok().build();
    }
}

