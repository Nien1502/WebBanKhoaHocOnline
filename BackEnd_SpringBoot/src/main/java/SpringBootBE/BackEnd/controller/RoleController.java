package SpringBootBE.BackEnd.controller;

import SpringBootBE.BackEnd.Service.RoleService;
import SpringBootBE.BackEnd.model.Role;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin
@Transactional
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Role> getRoleById(@PathVariable Integer id) {
        Role role = roleService.findById(id);
        return role != null ? ResponseEntity.ok(role) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Role> createRole(@RequestBody Role role) {
        return ResponseEntity.ok(roleService.save(role));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Role> updateRole(@PathVariable Integer id, @RequestBody Role role) {
        Role existingRole = roleService.findById(id);
        if (existingRole != null) {
            role.setId(id);
            return ResponseEntity.ok(roleService.update(role));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Integer id) {
        roleService.delete(id);
        return ResponseEntity.ok().build();
    }
}

