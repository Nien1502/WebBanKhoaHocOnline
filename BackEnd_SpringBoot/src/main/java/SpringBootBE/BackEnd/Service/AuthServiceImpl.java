package SpringBootBE.BackEnd.Service;

import SpringBootBE.BackEnd.config.JwtTokenService;
import SpringBootBE.BackEnd.dto.LoginResponse;
import SpringBootBE.BackEnd.dto.RegisterRequest;
import SpringBootBE.BackEnd.dto.RegisterResponse;
import SpringBootBE.BackEnd.model.Role;
import SpringBootBE.BackEnd.model.User;
import SpringBootBE.BackEnd.model.UserRole;
import SpringBootBE.BackEnd.repository.RoleRepository;
import SpringBootBE.BackEnd.repository.UserRepository;
import SpringBootBE.BackEnd.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    private static final String DEFAULT_ROLE_NAME = "Student";

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final JwtTokenService jwtTokenService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository,
                           UserRoleRepository userRoleRepository,
                           RoleRepository roleRepository,
                           JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public LoginResponse login(String email, String password) {
        LoginResponse response = new LoginResponse();

        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            response.setMessage("Email và mật khẩu là bắt buộc.");
            return response;
        }

        User user = userRepository.findByEmail(email);
        if (user == null) {
            response.setMessage("Email hoặc mật khẩu không đúng.");
            return response;
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            response.setMessage("Email hoặc mật khẩu không đúng.");
            return response;
        }

        List<UserRole> userRoles = userRoleRepository.findByUserId(user.getId());
        if (userRoles.isEmpty()) {
            response.setMessage("Bạn không có quyền truy cập.");
            return response;
        }

        UserRole userRole = userRoles.get(0);
        Role role = userRole.getRole();
        String roleName = role.getRoleName();
        String access;

        if ("Admin".equalsIgnoreCase(roleName)) {
            access = "admin";
        } else if ("Student".equalsIgnoreCase(roleName)) {
            access = "student";
        } else if ("Teacher".equalsIgnoreCase(roleName)) {
            access = "teacher";
        } else {
            response.setMessage("Bạn không có quyền truy cập.");
            return response;
        }

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setEmail(user.getEmail());
        userInfo.setUsername(user.getUsername());
        userInfo.setRole(roleName);
        userInfo.setAccess(access);

        JwtTokenService.TokenData tokenData = jwtTokenService.generateToken(
                user.getId(),
                user.getEmail(),
                roleName,
                access
        );

        response.setMessage("Đăng nhập thành công!");
        response.setUser(userInfo);
        response.setToken(tokenData.token());
        response.setTokenType("Bearer");
        response.setExpiresAt(tokenData.expiresAt());
        return response;
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest registerRequest) {
        RegisterResponse response = new RegisterResponse();
        if (registerRequest == null) {
            response.setMessage("Dữ liệu đăng ký không hợp lệ.");
            return response;
        }

        String username = normalize(registerRequest.getUsername());
        String email = normalize(registerRequest.getEmail());
        String password = registerRequest.getPassword();

        if (username == null || email == null || password == null || password.trim().isEmpty()) {
            response.setMessage("Username, email và mật khẩu là bắt buộc.");
            return response;
        }

        email = email.toLowerCase();

        if (userRepository.findByEmail(email) != null) {
            response.setMessage("Email đã tồn tại.");
            return response;
        }

        if (userRepository.findByUsername(username) != null) {
            response.setMessage("Username đã tồn tại.");
            return response;
        }

        Role studentRole = roleRepository.findByRoleNameIgnoreCase(DEFAULT_ROLE_NAME);
        if (studentRole == null) {
            response.setMessage("Hệ thống chưa cấu hình role Student.");
            return response;
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setStatus("Hoạt động");
        user.setCreateTime(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        UserRole userRole = new UserRole();
        userRole.setUser(savedUser);
        userRole.setRole(studentRole);
        userRoleRepository.save(userRole);

        response.setMessage("Đăng ký thành công!");
        response.setUser(new RegisterResponse.UserInfo(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getUsername(),
                studentRole.getRoleName()
        ));
        return response;
    }

    @Override
    public Integer getNonAdminUserCount() {
        return (int) userRoleRepository.countDistinctNonAdminUsers();
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
