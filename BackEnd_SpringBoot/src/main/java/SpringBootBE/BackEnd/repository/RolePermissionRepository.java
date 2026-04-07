package SpringBootBE.BackEnd.repository;

import SpringBootBE.BackEnd.model.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Integer> {
    List<RolePermission> findByRoleId(Integer roleId);
    List<RolePermission> findByPermissionId(Integer permissionId);
}

