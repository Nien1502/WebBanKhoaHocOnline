package SpringBootBE.BackEnd.Service;

import SpringBootBE.BackEnd.model.Permission;
import java.util.List;

public interface PermissionService {
    List<Permission> findAll();
    Permission findById(Integer id);
    Permission findByPermissionName(String permissionName);
    Permission save(Permission permission);
    Permission update(Permission permission);
    void delete(Integer id);
}

