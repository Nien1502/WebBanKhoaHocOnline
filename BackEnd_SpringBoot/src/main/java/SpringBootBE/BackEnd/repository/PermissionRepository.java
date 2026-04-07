package SpringBootBE.BackEnd.repository;

import SpringBootBE.BackEnd.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Integer> {
    Permission findByPermissionName(String permissionName);
}

