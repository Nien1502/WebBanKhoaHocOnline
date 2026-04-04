package SpringBootBE.BackEnd.Service;

import SpringBootBE.BackEnd.model.UserRole;
import SpringBootBE.BackEnd.repository.UserRoleRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserRoleServiceImpl implements UserRoleService {

    private final UserRoleRepository userRoleRepository;

    public UserRoleServiceImpl(UserRoleRepository userRoleRepository) {
        this.userRoleRepository = userRoleRepository;
    }

    @Override
    public List<UserRole> findAll() {
        return userRoleRepository.findAll();
    }

    @Override
    public UserRole findById(Integer id) {
        return userRoleRepository.findById(id).orElse(null);
    }

    @Override
    public List<UserRole> findByUserId(Integer userId) {
        return userRoleRepository.findByUserId(userId);
    }

    @Override
    public List<UserRole> findByRoleId(Integer roleId) {
        return userRoleRepository.findByRoleId(roleId);
    }

    @Override
    public UserRole save(UserRole userRole) {
        return userRoleRepository.save(userRole);
    }

    @Override
    public UserRole update(UserRole userRole) {
        return userRoleRepository.save(userRole);
    }

    @Override
    public void delete(Integer id) {
        userRoleRepository.deleteById(id);
    }
}

