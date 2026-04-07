package SpringBootBE.BackEnd.Service;

import SpringBootBE.BackEnd.model.Permission;
import SpringBootBE.BackEnd.repository.PermissionRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionServiceImpl(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @Override
    public List<Permission> findAll() {
        return permissionRepository.findAll();
    }

    @Override
    public Permission findById(Integer id) {
        return permissionRepository.findById(id).orElse(null);
    }

    @Override
    public Permission findByPermissionName(String permissionName) {
        return permissionRepository.findByPermissionName(permissionName);
    }

    @Override
    public Permission save(Permission permission) {
        return permissionRepository.save(permission);
    }

    @Override
    public Permission update(Permission permission) {
        return permissionRepository.save(permission);
    }

    @Override
    public void delete(Integer id) {
        permissionRepository.deleteById(id);
    }
}

