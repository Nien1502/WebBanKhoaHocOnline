package SpringBootBE.BackEnd.Service;

import SpringBootBE.BackEnd.model.User;

import java.util.List;

public interface UserService {
    List<User> findAll();

    User findById(Integer id);

    User findByEmail(String email);

    User findByUsername(String username);

    User update(User user);

    void delete(Integer id);
}
