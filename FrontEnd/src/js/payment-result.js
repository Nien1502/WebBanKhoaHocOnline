document.addEventListener("DOMContentLoaded", function () {
  const API_BASE = localStorage.getItem("apiBaseUrl") || window.API_BASE_URL || "http://localhost:8080";
  const apiUrl = function (path) {
    return API_BASE + (path.startsWith("/") ? path : "/" + path);
  };

  const statusEl = document.getElementById("status");
  const messageEl = document.getElementById("message");
  const metaEl = document.getElementById("meta");

  function setStatus(ok, text) {
    if (!statusEl) return;
    statusEl.textContent = text;
    statusEl.classList.remove("ok", "error");
    statusEl.classList.add(ok ? "ok" : "error");
  }

  function setMessage(text) {
    if (!messageEl) return;
    messageEl.textContent = text || "";
  }

  function showMeta(values) {
    if (!metaEl) return;
    metaEl.hidden = false;
    const safe = values || {};
    metaEl.innerHTML = ""
      + "<div><strong>Mã đơn hàng:</strong> " + (safe.orderId || "-") + "</div>"
      + "<div><strong>Mã yêu cầu:</strong> " + (safe.requestId || "-") + "</div>"
      + "<div><strong>Mã kết quả:</strong> " + (safe.resultCode ?? "-") + "</div>"
      + "<div><strong>Trạng thái:</strong> " + (safe.status || "-") + "</div>";
  }

  function clearCartAfterSuccess() {
    const pendingRaw = localStorage.getItem("pendingMomoOrder");
    if (!pendingRaw) return;

    let pending;
    try {
      pending = JSON.parse(pendingRaw);
    } catch {
      return;
    }

    const loggedInUser = JSON.parse(localStorage.getItem("loggedInUser"));
    const cartKey = loggedInUser && loggedInUser.username ? "cart_" + loggedInUser.username : "cart_guest";

    let cartItems = [];
    try {
      cartItems = JSON.parse(localStorage.getItem(cartKey)) || [];
    } catch {
      cartItems = [];
    }

    const paidCourseIds = Array.isArray(pending.courseIds) ? pending.courseIds.map(function (id) {
      return String(id);
    }) : [];

    if (paidCourseIds.length > 0) {
      const filtered = cartItems.filter(function (item) {
        return !paidCourseIds.includes(String(item.courseId));
      });
      localStorage.setItem(cartKey, JSON.stringify(filtered));
    }

    localStorage.removeItem("pendingMomoOrder");
  }

  async function processCallback() {
    const query = window.location.search;
    if (!query || query.length <= 1) {
      setStatus(false, "Không tìm thấy dữ liệu callback");
      setMessage("Bạn đang vào trang kết quả nhưng không có tham số trả về từ cổng thanh toán.");
      return;
    }

    try {
      const response = await fetch(apiUrl("/api/payment/momo/callback" + query), {
        method: "GET",
      });

      if (!response.ok) {
        const text = await response.text();
        throw new Error(text || "Không thể xử lý callback thanh toán.");
      }

      const data = await response.json();
      showMeta(data);

      const isSuccess = String(data.status || "").toUpperCase() === "SUCCESS";
      if (isSuccess) {
        setStatus(true, "Thanh toán thành công");
        setMessage(data.message || "Đơn hàng đã được xác nhận thành công.");
        clearCartAfterSuccess();
      } else {
        setStatus(false, "Thanh toán chưa thành công");
        setMessage(data.message || "Thanh toán thất bại hoặc callback không hợp lệ.");
      }
    } catch (error) {
      setStatus(false, "Xử lý callback thất bại");
      setMessage(error && error.message ? error.message : "Đã xảy ra lỗi khi xử lý callback.");
    }
  }

  processCallback();
});
