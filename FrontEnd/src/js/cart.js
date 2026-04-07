document.addEventListener("DOMContentLoaded", function () {
  const API_BASE = localStorage.getItem("apiBaseUrl") || window.API_BASE_URL || "http://localhost:8080";
  const apiUrl = function (path) {
    return API_BASE + (path.startsWith("/") ? path : "/" + path);
  };

  function getAuthHeader() {
    const token = localStorage.getItem("authToken") || "";
    const tokenType = localStorage.getItem("authTokenType") || "Bearer";
    const cachedHeader = localStorage.getItem("authHeader") || "";

    if (cachedHeader && cachedHeader.trim()) {
      return cachedHeader.trim();
    }

    if (token && token.trim()) {
      return `${tokenType} ${token}`.trim();
    }

    return "";
  }

  function withAuthHeaders(extraHeaders) {
    const headers = Object.assign({}, extraHeaders || {});
    const authHeader = getAuthHeader();
    if (authHeader) {
      headers.Authorization = authHeader;
    }
    return headers;
  }

  // Lấy thông tin user (có thể null nếu chưa đăng nhập)
  const loggedInUser = JSON.parse(localStorage.getItem("loggedInUser"));
  // Giỏ hàng tách theo user; nếu chưa đăng nhập thì dùng giỏ tạm guest.
  const cartKey = loggedInUser && loggedInUser.username ? "cart_" + loggedInUser.username : "cart_guest";
  
  // Lấy giỏ hàng từ localStorage (nếu có) hoặc tạo mảng rỗng
  let cartItems = JSON.parse(localStorage.getItem(cartKey)) || [];

  function parsePrice(value) {
    if (typeof value === "number") return value;
    if (!value) return 0;
    const normalized = String(value).replace(/[^\d.-]/g, "");
    const parsed = Number(normalized);
    return Number.isFinite(parsed) ? parsed : 0;
  }

  function isCourseInCart(courseId) {
    const normalizedId = String(courseId || "");
    return cartItems.some(item => String(item.courseId) === normalizedId);
  }

  function updateAddToCartButtonState(buttonEl) {
    if (!buttonEl) return;
    const courseId = buttonEl.getAttribute("data-id");
    const inCart = isCourseInCart(courseId);
    buttonEl.textContent = inCart ? "Đã thêm vào giỏ hàng" : "Thêm vào giỏ hàng";
    buttonEl.disabled = inCart;
    buttonEl.classList.toggle("is-added", inCart);
  }

  function syncAddToCartButtons() {
    document.querySelectorAll(".add-to-cart-btn").forEach(function (btn) {
      updateAddToCartButtonState(btn);
    });
  }

  // Hàm hiển thị giỏ hàng
  function displayCart() {
    const cartTable = document.querySelector(".cart tbody");
    if (!cartTable) return;
    cartTable.innerHTML = "";
    cartItems.forEach(item => {
      const addtr = document.createElement("tr");
      const trcontent = `
        <td style="display: flex; align-items: center;">
          <img style="width: 70px" src="${item.img}" alt="" />
          ${item.name}
        </td>
        <td>
          <p><span>${parsePrice(item.price).toLocaleString('vi-VN')}</span><sup>đ</sup></p>
        </td>
        <td style="cursor: pointer;" class="remove-btn" data-id="${item.courseId}">Xóa</td>
      `;
      addtr.innerHTML = trcontent;
      cartTable.append(addtr);
    });
    cartTotal();
  }

  // Hàm tính tổng tiền của giỏ hàng
  function cartTotal() {
    let totalC = 0;
    cartItems.forEach(item => {
      totalC += parsePrice(item.price);
    });
    const cartTotalA = document.querySelector(".price-total span");
    if (cartTotalA) {
      cartTotalA.innerHTML = totalC.toLocaleString('vi-VN');
    }
  }

  // Hàm thêm sản phẩm vào giỏ hàng
  function addCart(productId, productPrice, productImg, productName) {
    const courseId = String(productId);
    let productExists = cartItems.some(item => String(item.courseId) === courseId);
    if (productExists) {
      alert("Khóa học này đã có trong giỏ hàng!");
      return false;
    }
    const newItem = {
      courseId: courseId,
      name: productName,
      price: parsePrice(productPrice),
      img: productImg
    };
    cartItems.push(newItem);
    localStorage.setItem(cartKey, JSON.stringify(cartItems));
    displayCart();
    syncAddToCartButtons();
    return true;
  }

  // Gắn sự kiện vào các nút "Thêm vào giỏ hàng"
  const coursesContainer = document.getElementById('coursesContainer');
  if (coursesContainer) {
    coursesContainer.addEventListener('click', function(e) {
      if (e.target && e.target.classList.contains('add-to-cart-btn')) {
        const btn = e.target;
        const productId = btn.getAttribute('data-id');
        const productPrice = btn.getAttribute('data-price');
        const productImg = btn.getAttribute('data-image');
        const productName = btn.getAttribute('data-name');
        const added = addCart(productId, productPrice, productImg, productName);
        if (added) {
          updateAddToCartButtonState(btn);
        }
      }
    });

    const observer = new MutationObserver(function () {
      syncAddToCartButtons();
    });
    observer.observe(coursesContainer, { childList: true });
  }

  // Gắn sự kiện "Xóa" trong giỏ hàng
  const cartTable = document.querySelector(".cart tbody");
  if (cartTable) {
    cartTable.addEventListener("click", function(e) {
      if (e.target && e.target.classList.contains('remove-btn')) {
        const courseId = e.target.getAttribute("data-id");
        cartItems = cartItems.filter(item => item.courseId !== courseId);
        localStorage.setItem(cartKey, JSON.stringify(cartItems));
        displayCart();
      }
    });
  }

  // Gắn sự kiện cho nút "Thanh toán"
  const payButton = document.getElementById("btn-pay");
  if (payButton) {
    payButton.addEventListener("click", async function () {
      if (cartItems.length === 0) {
        alert("Giỏ hàng của bạn đang trống!");
        return;
      }

      const loggedInUser = JSON.parse(localStorage.getItem("loggedInUser"));
      if (!loggedInUser || !loggedInUser.id) {
        alert("Vui lòng đăng nhập để thanh toán!");
        console.error("User chưa đăng nhập hoặc thiếu ID.");
        return;
      }

      const authHeader = getAuthHeader();
      if (!authHeader) {
        alert("Phiên đăng nhập không hợp lệ. Vui lòng đăng nhập lại để thanh toán.");
        return;
      }

      const courseIds = cartItems
        .map(item => Number.parseInt(item.courseId, 10))
        .filter(Number.isInteger);

      if (courseIds.length === 0) {
        alert("Giỏ hàng không hợp lệ, vui lòng thêm lại khóa học.");
        return;
      }

      try {
        payButton.disabled = true;
        payButton.textContent = "Đang chuyển MoMo...";

        const totalAmount = cartItems.reduce((total, item) => total + parsePrice(item.price), 0);
        const payload = {
          userId: Number(loggedInUser.id),
          courseIds: courseIds,
          amount: totalAmount,
          orderInfo: "Thanh toán khóa học từ giỏ hàng",
          extraData: "",
        };

        const paymentResponse = await fetch(apiUrl("/api/payment/momo/create"), {
          method: "POST",
          headers: withAuthHeaders({
            "Content-Type": "application/json",
          }),
          body: JSON.stringify(payload),
        });

        if (!paymentResponse.ok) {
          const errorText = await paymentResponse.text();
          let backendMessage = "Không thể khởi tạo thanh toán MoMo!";
          try {
            const parsedError = JSON.parse(errorText);
            if (parsedError && typeof parsedError.message === "string" && parsedError.message.trim()) {
              backendMessage = parsedError.message.trim();
            }
          } catch {
            if (errorText && errorText.trim()) {
              backendMessage = errorText.trim();
            }
          }
          console.error("MoMo create error:", backendMessage);
          throw new Error(backendMessage);
        }

        const paymentData = await paymentResponse.json();
        const redirectUrl = paymentData.payUrl || paymentData.deeplink || paymentData.qrCodeUrl;

        if (!redirectUrl) {
          throw new Error(paymentData.message || "MoMo chưa trả về link thanh toán.");
        }

        // Lưu thông tin đơn chờ thanh toán để xử lý ở trang callback nếu cần.
        localStorage.setItem("pendingMomoOrder", JSON.stringify({
          orderId: paymentData.orderId,
          momoOrderId: paymentData.momoOrderId,
          requestId: paymentData.requestId,
          amount: paymentData.amount,
          courseIds: courseIds,
          createdAt: Date.now(),
        }));

        window.location.href = redirectUrl;
      } catch (error) {
        console.error("Lỗi khi thanh toán:", error);
        alert(error && error.message ? error.message : "Đã xảy ra lỗi khi thanh toán. Vui lòng thử lại!");
        payButton.disabled = false;
        payButton.textContent = "Thanh Toán";
      }
    });
  }

  // Gọi API lấy danh sách các khóa học đã được xử lý
  async function updateActivatedCourses() {
    if (loggedInUser && loggedInUser.id) {
      try {
        // Gọi API để lấy danh sách đơn hàng đã xử lý của người dùng hiện tại
        const response = await fetch(apiUrl(`/api/orders/user/${loggedInUser.id}`), {
          headers: withAuthHeaders(),
        });
        if (!response.ok) {
          throw new Error("Không thể tải danh sách đơn hàng của người dùng.");
        }
        const confirmedOrders = await response.json();

        console.log("Danh sách đơn hàng đã xử lý:", confirmedOrders);

        // Tạo một mảng để lưu tất cả CourseID từ các đơn hàng đã xử lý
        const processedCourseIds = [];

        // Lấy chi tiết từng đơn hàng
        for (const order of confirmedOrders) {
          const orderStatus = String(order?.status ?? order?.Status ?? "").toUpperCase();
          const orderId = order?.id ?? order?.OrderID;

          if (orderStatus === "SUCCESS" && orderId != null) {
            const orderDetailResponse = await fetch(apiUrl(`/api/order-details/order/${orderId}`));
            if (!orderDetailResponse.ok) {
              continue;
            }
            const orderDetails = await orderDetailResponse.json();

            // Lấy tất cả CourseID từ chi tiết đơn hàng
            orderDetails.forEach(detail => {
              const processedId = detail?.course?.id ?? detail?.CourseID ?? detail?.courseId;
              if (processedId != null) {
                processedCourseIds.push(Number(processedId));
              }
            });
          }
        }

        console.log("Danh sách CourseID đã xử lý:", processedCourseIds);

        // Duyệt qua tất cả nút "Thêm vào giỏ hàng"
        document.querySelectorAll(".add-to-cart-btn").forEach(btn => {
          const courseId = parseInt(btn.getAttribute("data-id"), 10);
          console.log("Course ID từ nút:", courseId);

          // Kiểm tra xem khóa học có trong danh sách đã xử lý hay không
          const isProcessed = Number.isInteger(courseId) && processedCourseIds.includes(courseId);
          console.log(`isProcessed cho Course ID ${courseId}:`, isProcessed);

          if (isProcessed) {
            const anchor = document.createElement("a");
            anchor.href = `course.html?id=${courseId}`; 
            anchor.className = "learn-now-link";
            anchor.textContent = "Học ngay";
            btn.parentElement.replaceChild(anchor, btn);
          }
        });
      } catch (error) {
        console.error("Lỗi khi lấy danh sách khóa học đã xử lý:", error);
      }
    }
  }

  updateActivatedCourses();
  displayCart();
  syncAddToCartButtons();

  // Export hàm addCart nếu cần dùng ở nơi khác
  window.addToCart = addCart;
});