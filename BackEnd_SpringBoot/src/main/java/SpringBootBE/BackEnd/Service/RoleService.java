package SpringBootBE.BackEnd.Service;

import SpringBootBE.BackEnd.model.Role;
import java.util.List;

public interface RoleService {
    List<Role> findAll();
    Role findById(Integer id);
    Role findByRoleName(String roleName);
    Role save(Role role);
    Role update(Role role);
    void delete(Integer id);
}

