package SpringBootBE.BackEnd.Service;

import SpringBootBE.BackEnd.model.UserRole;
import java.util.List;

public interface UserRoleService {
    List<UserRole> findAll();
    UserRole findById(Integer id);
    List<UserRole> findByUserId(Integer userId);
    List<UserRole> findByRoleId(Integer roleId);
    UserRole save(UserRole userRole);
    UserRole update(UserRole userRole);
    void delete(Integer id);
}

