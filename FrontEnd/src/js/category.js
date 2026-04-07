document.addEventListener("DOMContentLoaded", function () {
  const API_BASE = localStorage.getItem("apiBaseUrl") || window.API_BASE_URL || "http://localhost:8080";
  const apiUrl = function (path) {
    return API_BASE + (path.startsWith("/") ? path : "/" + path);
  };

  function getCategoryId(item) {
    return item?.id ?? item?.CategoryID ?? item?.categoryId ?? item?.categoryID ?? "";
  }

  function getCategoryName(item) {
    return item?.categoryName ?? item?.CategoryName ?? item?.name ?? "Danh mục";
  }

  function getCourseId(course) {
    return course?.id ?? course?.CourseID ?? "";
  }

  function getCourseName(course) {
    return course?.courseName ?? course?.CourseName ?? "Khóa học";
  }

  function getCoursePrice(course) {
    return Number(course?.price ?? course?.Price ?? 0);
  }

  function getCourseImageValue(course) {
    return String(course?.image ?? course?.Imag ?? "").trim();
  }

  
  function resolveCourseImageUrl(course) {
    const imageValue = getCourseImageValue(course);
    if (!imageValue) return "../src/assets/img/blank-image.png";

    if (/^(https?:)?\/\//i.test(imageValue) || imageValue.startsWith("data:")) {
      return imageValue;
    }

    if (imageValue.includes("/")) {
      if (imageValue.startsWith("/")) {
        return API_BASE + imageValue;
      }
      return imageValue;
    }

    // DB chi luu ten file: uu tien lay anh tu static backend.
    return apiUrl("/assets/images/" + encodeURIComponent(imageValue));
  }

  const params = new URLSearchParams(window.location.search);
  const categoryId = params.get("categoryId");
  const categoryName = params.get("name") || "Danh mục";

  const title = document.getElementById("categoryTitle");
  const desc = document.getElementById("categoryDescription");
  const container = document.getElementById("coursesContainer");
  const dropdown = document.getElementById("categoryDropdownList");

  if (title) {
    title.textContent = categoryName;
  }

  if (desc) {
    desc.textContent = "Danh sách khóa học thuộc " + categoryName + ".";
  }

  if (!categoryId) {
    if (container) {
      container.innerHTML = "<p>Thiếu categoryId trên URL.</p>";
    }
    return;
  }

  fetch(apiUrl("/api/categories"))
    .then(function (response) {
      if (!response.ok) {
        throw new Error("Không thể tải danh mục");
      }
      return response.json();
    })
    .then(function (categories) {
      if (!Array.isArray(categories) || !dropdown) return;
      dropdown.innerHTML = categories.map(function (item) {
        const id = getCategoryId(item);
        const name = getCategoryName(item);
        return '<a href="category.html?categoryId=' + encodeURIComponent(id) + '&name=' + encodeURIComponent(name) + '">' + name + "</a>";
      }).join("");
    })
    .catch(function () {
      // Keep default dropdown links if API is unavailable.
    });

  fetch(apiUrl("/api/courses/category/") + encodeURIComponent(categoryId))
    .then(function (response) {
      if (!response.ok) {
        throw new Error("HTTP error! status: " + response.status);
      }
      return response.json();
    })
    .then(function (courses) {
      if (!container) return;

      if (!Array.isArray(courses) || courses.length === 0) {
        container.innerHTML = "<p>Không có khóa học nào thuộc danh mục này.</p>";
        return;
      }

      container.innerHTML = courses.map(function (course) {
        const imageUrl = resolveCourseImageUrl(course);
        const courseName = getCourseName(course);
        const coursePrice = getCoursePrice(course);
        const courseId = getCourseId(course);
        return [
          '<div class="product-item">',
          '  <img src="' + imageUrl + '" alt="' + courseName + '" onerror="this.onerror=null;this.src=\'../src/assets/img/blank-image.png\';" />',
          '  <div class="product-item-text">',
          '    <p><span>' + coursePrice.toLocaleString("vi-VN") + '</span><sup>đ</sup></p>',
          '    <h1>' + courseName + "</h1>",
          '  </div>',
          '  <button class="add-to-cart-btn"',
          '          data-id="' + courseId + '"',
          '          data-name="' + courseName + '"',
          '          data-price="' + coursePrice + '"',
          '          data-image="' + imageUrl + '">',
          '    Thêm vào giỏ hàng',
          '  </button>',
          '</div>'
        ].join("");
      }).join("");
    })
    .catch(function (err) {
      console.error(err);
      if (container) {
        container.innerHTML = "<p>Lỗi: " + err.message + "</p>";
      }
    });

  const loggedInUser = JSON.parse(localStorage.getItem("loggedInUser"));
  const userInfo = document.getElementById("userInfo");
  if (loggedInUser && loggedInUser.username && userInfo) {
    userInfo.textContent = loggedInUser.username;
  }
});
