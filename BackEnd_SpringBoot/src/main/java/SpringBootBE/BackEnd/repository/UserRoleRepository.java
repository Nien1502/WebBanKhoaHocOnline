package SpringBootBE.BackEnd.repository;

import SpringBootBE.BackEnd.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Integer> {
    List<UserRole> findByUserId(Integer userId);

    List<UserRole> findByRoleId(Integer roleId);

    @Query("""
            select count(distinct ur.user.id)
            from UserRole ur
            join ur.user u
            join ur.role r
            where lower(r.roleName) <> 'admin'
            """)
    long countDistinctNonAdminUsers();
}
