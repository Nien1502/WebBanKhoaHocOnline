document.addEventListener("DOMContentLoaded", () => {
    const API_BASE = localStorage.getItem("apiBaseUrl") || window.API_BASE_URL || "http://localhost:8080";
    const apiUrl = (path) => API_BASE + (path.startsWith("/") ? path : "/" + path);

    async function readBodySafe(response) {
        const raw = await response.text();
        if (!raw) {
            return { raw: "", data: null };
        }

        try {
            return { raw, data: JSON.parse(raw) };
        } catch {
            return { raw, data: null };
        }
    }

    const registerForm = document.getElementById("registerForm");

    registerForm.addEventListener("submit", async (event) => {
        event.preventDefault(); // Ngăn form reload trang

        // Lấy dữ liệu từ form
        const username = document.getElementById("username").value.trim();
        const email = document.getElementById("email").value.trim();
        const password = document.getElementById("password").value.trim();
        const confirmPassword = document.getElementById("confirmPassword").value.trim();

        // Kiểm tra tính hợp lệ
        if (!username || !email || !password || !confirmPassword) {
            alert("Vui lòng điền đầy đủ thông tin.");
            return;
        }

        if (password !== confirmPassword) {
            alert("Mật khẩu xác nhận không khớp.");
            return;
        }

        try {
            const registerResponse = await fetch(apiUrl("/api/auth/register"), {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    username: username,
                    email: email,
                    password: password,
                }),
            });

            const registerResult = await readBodySafe(registerResponse);
            const resultData = registerResult.data;
            const serverMessage = resultData?.message || registerResult.raw || "Đăng ký thất bại.";

            if (!registerResponse.ok) {
                console.error("Lỗi đăng ký:", registerResult.raw);
                alert(`Đăng ký thất bại: ${serverMessage}`);
                return;
            }

            alert(serverMessage || "Đăng ký thành công!");
            window.location.href = "/login.html";
        } catch (error) {
            console.error("Lỗi:", error);
            alert("Đã xảy ra lỗi. Vui lòng thử lại sau.");
        }
    });
});