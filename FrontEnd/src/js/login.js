document.addEventListener('DOMContentLoaded', () => {
    const API_BASE = localStorage.getItem('apiBaseUrl') || window.API_BASE_URL || 'http://localhost:8080';
    const apiUrl = (path) => API_BASE + (path.startsWith('/') ? path : '/' + path);

    const loginForm = document.getElementById('loginForm');
    const passwordInput = document.getElementById('password');
    const togglePasswordButton = document.getElementById('togglePassword');

    if (passwordInput && togglePasswordButton) {
        togglePasswordButton.addEventListener('click', () => {
            const isHidden = passwordInput.type === 'password';
            passwordInput.type = isHidden ? 'text' : 'password';

            const icon = togglePasswordButton.querySelector('i');
            if (icon) {
                icon.classList.toggle('fa-eye', !isHidden);
                icon.classList.toggle('fa-eye-slash', isHidden);
            }

            togglePasswordButton.setAttribute('aria-pressed', String(isHidden));
            togglePasswordButton.setAttribute('aria-label', isHidden ? 'Ẩn mật khẩu' : 'Hiện mật khẩu');
        });
    }

    // Hàm kiểm tra email hợp lệ
    function validateEmail(email) {
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return emailRegex.test(email);
    }

    loginForm.addEventListener('submit', async (event) => {
        event.preventDefault(); // Ngăn form reload trang

        const email = document.getElementById('email').value.trim();
        const password = document.getElementById('password').value.trim();

        if (!email || !password) {
            alert('Vui lòng điền đầy đủ thông tin.');
            return;
        }

        if (!validateEmail(email)) {
            alert('Email không hợp lệ.');
            return;
        }

        try {
            const response = await fetch(apiUrl('/api/auth/users/checklogin'), {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ email: email, password: password }),
            });

            if (response.ok) {
                const data = await response.json();
                
                const token = data.token || '';
                const tokenType = data.tokenType || 'Bearer';
                const expiresAt = Number(data.expiresAt || 0);

                if (!token) {
                    alert('Đăng nhập thất bại: Server không trả về token.');
                    return;
                }

                if (expiresAt && Date.now() >= expiresAt) {
                    alert('Đăng nhập thất bại: Token đã hết hạn. Vui lòng thử lại.');
                    return;
                }

                localStorage.setItem('authToken', token);
                localStorage.setItem('authTokenType', tokenType);
                localStorage.setItem('authTokenExpiresAt', String(expiresAt));

                // Lưu sẵn header để tái sử dụng cho các request cần Authorization.
                localStorage.setItem('authHeader', `${tokenType} ${token}`);

                localStorage.setItem('loggedInUser', JSON.stringify({
                    id: data.user.id,            // ID người dùng
                    username: data.user.username, // Tên người dùng
                    email: data.user.email,       // Email người dùng
                    role: data.user.role,         // Vai trò (Admin/Student)
                }));            

                if (data.user.role === 'Admin') {
                    alert('Đăng nhập thành công với quyền Admin!');
                    window.location.href = '/admin.html'; // Điều hướng đến trang admin
                } else if (data.user.role === 'Student') {
                    alert('Đăng nhập thành công với quyền Student!');
                    window.location.href = '/menu.html'; // Điều hướng đến trang student
                } else {
                    alert('Quyền không hợp lệ.');
                }
            } else {
                const errorText = await response.text();
                let errorMessage = 'Đăng nhập thất bại.';
                try {
                    const errorJson = JSON.parse(errorText);
                    errorMessage = errorJson.message || errorMessage;
                    console.error('Server Error:', errorJson);
                } catch {
                    if (errorText) {
                        errorMessage = errorText;
                    }
                    console.error('Server Error (raw):', errorText);
                }
                alert(`Đăng nhập thất bại: ${errorMessage}`);
            }
        } catch (error) {
            console.error('Lỗi:', error);
            alert('Đã xảy ra lỗi. Vui lòng thử lại sau.');
        }
    });
});

