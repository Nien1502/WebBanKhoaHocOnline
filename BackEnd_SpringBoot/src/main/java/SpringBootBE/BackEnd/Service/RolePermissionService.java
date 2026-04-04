package SpringBootBE.BackEnd.Service;

import SpringBootBE.BackEnd.model.RolePermission;
import java.util.List;

public interface RolePermissionService {
    List<RolePermission> findAll();
    RolePermission findById(Integer id);
    List<RolePermission> findByRoleId(Integer roleId);
    List<RolePermission> findByPermissionId(Integer permissionId);
    RolePermission save(RolePermission rolePermission);
    RolePermission update(RolePermission rolePermission);
    void delete(Integer id);
}

