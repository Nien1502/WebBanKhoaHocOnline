package SpringBootBE.BackEnd.Service;

import SpringBootBE.BackEnd.model.Role;
import SpringBootBE.BackEnd.repository.RoleRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    @Override
    public Role findById(Integer id) {
        return roleRepository.findById(id).orElse(null);
    }

    @Override
    public Role findByRoleName(String roleName) {
        return roleRepository.findByRoleName(roleName);
    }

    @Override
    public Role save(Role role) {
        return roleRepository.save(role);
    }

    @Override
    public Role update(Role role) {
        return roleRepository.save(role);
    }

    @Override
    public void delete(Integer id) {
        roleRepository.deleteById(id);
    }
}

