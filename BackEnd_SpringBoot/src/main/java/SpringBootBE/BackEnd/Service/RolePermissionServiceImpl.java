package SpringBootBE.BackEnd.Service;

import SpringBootBE.BackEnd.model.RolePermission;
import SpringBootBE.BackEnd.repository.RolePermissionRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RolePermissionServiceImpl implements RolePermissionService {

    private final RolePermissionRepository rolePermissionRepository;

    public RolePermissionServiceImpl(RolePermissionRepository rolePermissionRepository) {
        this.rolePermissionRepository = rolePermissionRepository;
    }

    @Override
    public List<RolePermission> findAll() {
        return rolePermissionRepository.findAll();
    }

    @Override
    public RolePermission findById(Integer id) {
        return rolePermissionRepository.findById(id).orElse(null);
    }

    @Override
    public List<RolePermission> findByRoleId(Integer roleId) {
        return rolePermissionRepository.findByRoleId(roleId);
    }

    @Override
    public List<RolePermission> findByPermissionId(Integer permissionId) {
        return rolePermissionRepository.findByPermissionId(permissionId);
    }

    @Override
    public RolePermission save(RolePermission rolePermission) {
        return rolePermissionRepository.save(rolePermission);
    }

    @Override
    public RolePermission update(RolePermission rolePermission) {
        return rolePermissionRepository.save(rolePermission);
    }

    @Override
    public void delete(Integer id) {
        rolePermissionRepository.deleteById(id);
    }
}

