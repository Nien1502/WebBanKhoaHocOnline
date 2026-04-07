package SpringBootBE.BackEnd.Service;

import SpringBootBE.BackEnd.dto.LoginResponse;
import SpringBootBE.BackEnd.dto.RegisterRequest;
import SpringBootBE.BackEnd.dto.RegisterResponse;

public interface AuthService {
    LoginResponse login(String email, String password);

    RegisterResponse register(RegisterRequest registerRequest);

    Integer getNonAdminUserCount();
}
