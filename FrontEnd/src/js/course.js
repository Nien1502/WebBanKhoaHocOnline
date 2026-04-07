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

  const purchasedCourseList = document.getElementById("purchasedCourseList");
  const courseCountEl = document.getElementById("courseCount");

  const loggedInUser = JSON.parse(localStorage.getItem("loggedInUser"));
  const userInfo = document.getElementById("userInfo");
  if (loggedInUser && loggedInUser.username && userInfo) {
    userInfo.textContent = loggedInUser.username;
  }

  if (!loggedInUser || !loggedInUser.id) {
    alert("Vui lòng đăng nhập để xem khóa học đã mua.");
    window.location.href = "login.html";
    return;
  }

  if (!purchasedCourseList) {
    console.error("Không tìm thấy thành phần hiển thị khóa học.");
    return;
  }

  async function fetchPurchasedCourseIds(userId) {
    const ordersResponse = await fetch(apiUrl(`/api/orders/user/${userId}`), {
      headers: withAuthHeaders(),
    });
    if (!ordersResponse.ok) {
      throw new Error("Không thể tải danh sách đơn hàng của bạn.");
    }

    const orders = await ordersResponse.json();
    const successOrders = (orders || []).filter(function (order) {
      const status = String(order?.status ?? order?.Status ?? "").toUpperCase();
      return status === "SUCCESS";
    });

    const purchasedIds = new Set();
    for (const order of successOrders) {
      const orderId = order?.id ?? order?.OrderID;
      if (orderId == null) {
        continue;
      }

      const detailsResponse = await fetch(apiUrl(`/api/order-details/order/${orderId}`));
      if (!detailsResponse.ok) {
        continue;
      }

      const details = await detailsResponse.json();
      (details || []).forEach(function (detail) {
        const courseId = detail?.course?.id ?? detail?.CourseID ?? detail?.courseId;
        const normalized = Number(courseId);
        if (Number.isInteger(normalized)) {
          purchasedIds.add(normalized);
        }
      });
    }

    return Array.from(purchasedIds);
  }

  async function fetchCourseById(courseId) {
    const response = await fetch(apiUrl("/api/courses/") + encodeURIComponent(courseId));
    if (!response.ok) {
      throw new Error("Không thể tải khóa học (ID: " + courseId + ").");
    }
    return response.json();
  }

  async function fetchLessonsByCourseId(courseId) {
    const response = await fetch(apiUrl(`/api/lessons/course/${courseId}`));
    if (!response.ok) {
      return [];
    }

    const lessons = await response.json();
    return Array.isArray(lessons) ? lessons : [];
  }

  async function fetchEnrollmentByUserAndCourse(userId, courseId) {
    const response = await fetch(apiUrl(`/api/enrollments/user/${userId}/course/${courseId}`));
    if (response.status === 404) {
      return null;
    }
    if (!response.ok) {
      throw new Error("Không thể tải thông tin tiến độ của khóa học (ID: " + courseId + ").");
    }
    return response.json();
  }

  async function fetchLessonProgressByEnrollmentId(enrollmentId) {
    const response = await fetch(apiUrl(`/api/lesson-progress/enrollment/${enrollmentId}`));
    if (!response.ok) {
      return [];
    }
    const progresses = await response.json();
    return Array.isArray(progresses) ? progresses : [];
  }

  function normalizePercentage(value) {
    const num = Number(value);
    if (!Number.isFinite(num)) {
      return 0;
    }
    return Math.max(0, Math.min(100, num));
  }

  function normalizeDuration(value) {
    const num = Number(value);
    if (!Number.isFinite(num) || num < 0) {
      return 0;
    }
    return num;
  }

  function formatDuration(secondsValue) {
    const totalSeconds = Math.max(0, Math.round(Number(secondsValue) || 0));
    const hours = Math.floor(totalSeconds / 3600);
    const minutes = Math.floor((totalSeconds % 3600) / 60);
    const seconds = totalSeconds % 60;
    const pad = function (value) {
      return String(value).padStart(2, "0");
    };

    return pad(hours) + ":" + pad(minutes) + ":" + pad(seconds);
  }

  function getProgressLessonId(progress) {
    const directId = progress?.lessonId ?? progress?.LessonID;
    if (directId != null) {
      const normalized = Number(directId);
      return Number.isInteger(normalized) ? normalized : null;
    }

    const nestedId = progress?.lesson?.id ?? progress?.lesson?.LessonID;
    if (nestedId != null) {
      const normalized = Number(nestedId);
      return Number.isInteger(normalized) ? normalized : null;
    }

    return null;
  }

  function toCourseViewModel(rawCourse, fallbackId) {
    const id = Number(rawCourse?.id ?? rawCourse?.CourseID ?? fallbackId);
    return {
      id: id,
      name: rawCourse?.courseName ?? rawCourse?.CourseName ?? ("Khóa học #" + id),
      description: rawCourse?.description ?? rawCourse?.Description ?? "",
      price: rawCourse?.price ?? rawCourse?.Price,
      lessonCount: 0,
      completionPercent: 0,
      isCourseCompleted: false,
      completionStatus: "Chưa hoàn thành",
      totalDuration: 0,
      watchedDuration: 0,
      videos: []
    };
  }

  async function enrichCourseProgress(course, userId) {
    const lessons = await fetchLessonsByCourseId(course.id);
    const enrollment = await fetchEnrollmentByUserAndCourse(userId, course.id);
    const progressRows = enrollment?.id ? await fetchLessonProgressByEnrollmentId(enrollment.id) : [];

    const progressByLessonId = new Map();
    progressRows.forEach(function (row) {
      const lessonId = getProgressLessonId(row);
      if (lessonId != null) {
        progressByLessonId.set(lessonId, row);
      }
    });

    let totalDuration = 0;
    let watchedDuration = 0;
    let completedVideos = 0;

    const videos = lessons.map(function (lesson, index) {
      const lessonId = Number(lesson?.id ?? lesson?.LessonID ?? index);
      const duration = normalizeDuration(lesson?.duration ?? lesson?.Duration);
      const progress = progressByLessonId.get(lessonId);
      const watchedPercent = normalizePercentage(progress?.watchedPercentage ?? progress?.WatchedPercentage);
      const isCompleted = Boolean(progress?.isCompleted ?? progress?.IsCompleted) || watchedPercent >= 100;
      const watched = duration * (watchedPercent / 100);

      totalDuration += duration;
      watchedDuration += watched;
      if (isCompleted) {
        completedVideos += 1;
      }

      return {
        id: lessonId,
        title: lesson?.lessonTitle ?? lesson?.LessonTitle ?? ("Video #" + (index + 1)),
        duration: duration,
        watchedDuration: watched,
        watchedPercent: watchedPercent,
        isCompleted: isCompleted
      };
    });

    const completionFromDuration = totalDuration > 0 ? (watchedDuration / totalDuration) * 100 : 0;
    const completionFromCount = lessons.length > 0 ? (completedVideos / lessons.length) * 100 : 0;
    const completionPercent = totalDuration > 0 ? completionFromDuration : completionFromCount;
    const enrollmentStatus = String(enrollment?.completionStatus ?? enrollment?.CompletionStatus ?? "").trim();
    const completedByStatus = /^completed$/i.test(enrollmentStatus);
    const completedByLessons = lessons.length > 0 && completedVideos === lessons.length;
    const isCourseCompleted = completedByStatus || completedByLessons;

    course.lessonCount = lessons.length;
    course.totalDuration = totalDuration;
    course.watchedDuration = watchedDuration;
    course.completionPercent = normalizePercentage(completionPercent);
    course.isCourseCompleted = isCourseCompleted;
    course.completionStatus = isCourseCompleted ? "Đã hoàn thành" : "Chưa hoàn thành";
    course.videos = videos;

    return course;
  }

  function renderPurchasedCourseVideoCounts(courses) {
    const safeCourses = Array.isArray(courses) ? courses : [];
    const totalVideos = safeCourses.reduce(function (sum, course) {
      return sum + (Number.isInteger(course.lessonCount) ? course.lessonCount : 0);
    }, 0);
    const totalDuration = safeCourses.reduce(function (sum, course) {
      return sum + normalizeDuration(course.totalDuration);
    }, 0);
    const totalWatched = safeCourses.reduce(function (sum, course) {
      return sum + normalizeDuration(course.watchedDuration);
    }, 0);
    const overallPercent = totalDuration > 0 ? normalizePercentage((totalWatched / totalDuration) * 100) : 0;

    if (courseCountEl) {
      courseCountEl.textContent =
        "Tổng số video: " +
        totalVideos +
        " | Tiến độ học: " +
        overallPercent.toFixed(1) +
        "% (" +
        formatDuration(totalWatched) +
        " / " +
        formatDuration(totalDuration) +
        ")";
    }

    purchasedCourseList.innerHTML = "";

    if (!safeCourses.length) {
      purchasedCourseList.innerHTML = '<div class="course-item">Bạn chưa có khóa học nào đã mua thành công.</div>';
      return;
    }

    safeCourses.forEach(function (course) {
      const item = document.createElement("article");
      item.className = "course-item";

      const title = document.createElement("h3");
      title.className = "course-item-title";
      title.textContent = course.name;

      const status = document.createElement("p");
      status.className = "course-item-status " + (course.isCourseCompleted ? "done" : "pending");
      status.textContent = "Trạng thái khóa học: " + course.completionStatus;

      const count = document.createElement("p");
      count.className = "course-item-meta";
      count.textContent =
        "Số video: " +
        (Number.isInteger(course.lessonCount) ? course.lessonCount : 0) +
        " | Hoàn thành: " +
        normalizePercentage(course.completionPercent).toFixed(1) +
        "%";

      const progressWrap = document.createElement("div");
      progressWrap.className = "course-progress";

      const progressBar = document.createElement("div");
      progressBar.className = "course-progress-bar";

      const progressFill = document.createElement("span");
      progressFill.className = "course-progress-fill";
      progressFill.style.width = normalizePercentage(course.completionPercent).toFixed(1) + "%";

      progressBar.appendChild(progressFill);
      progressWrap.appendChild(progressBar);

      const progressText = document.createElement("p");
      progressText.className = "course-item-meta";
      progressText.textContent =
        "Thời lượng đã xem: " +
        formatDuration(course.watchedDuration) +
        " / " +
        formatDuration(course.totalDuration);

      const videoList = document.createElement("div");
      videoList.className = "course-video-list";

      course.videos.forEach(function (video) {
        const row = document.createElement("div");
        row.className = "course-video-item";

        const left = document.createElement("span");
        left.className = "course-video-title";
        left.textContent = video.title;

        const right = document.createElement("span");
        right.className = "course-video-progress";
        right.textContent =
          normalizePercentage(video.watchedPercent).toFixed(1) +
          "% (" +
          formatDuration(video.watchedDuration) +
          " / " +
          formatDuration(video.duration) +
          ")" +
          (video.isCompleted ? " - Đã xong" : " - Chưa xong");

        row.appendChild(left);
        row.appendChild(right);
        videoList.appendChild(row);
      });

      const learnNowBtn = document.createElement("button");
      learnNowBtn.type = "button";
      learnNowBtn.className = "course-item-btn";
      learnNowBtn.textContent = "Học ngay";
      learnNowBtn.addEventListener("click", function () {
        window.location.href = "lesson.html?courseId=" + encodeURIComponent(course.id);
      });

      item.appendChild(title);
      item.appendChild(status);
      item.appendChild(count);
      item.appendChild(progressWrap);
      item.appendChild(progressText);
      item.appendChild(videoList);
      item.appendChild(learnNowBtn);
      purchasedCourseList.appendChild(item);
    });
  }

  async function initPurchasedCoursesPage() {
    try {
      const purchasedIds = await fetchPurchasedCourseIds(loggedInUser.id);
      if (!purchasedIds.length) {
        renderPurchasedCourseVideoCounts([]);
        return;
      }

      const loadedCourses = await Promise.all(
        purchasedIds.map(async function (id) {
          const raw = await fetchCourseById(id);
          const course = toCourseViewModel(raw, id);
          return enrichCourseProgress(course, loggedInUser.id);
        })
      );

      const validCourses = loadedCourses.filter(function (course) {
        return Number.isInteger(course.id);
      });

      if (!validCourses.length) {
        throw new Error("Không có dữ liệu khóa học hợp lệ để hiển thị.");
      }

      renderPurchasedCourseVideoCounts(validCourses);
    } catch (error) {
      console.error("Lỗi khi tải khóa học đã mua:", error);
      alert(error && error.message ? error.message : "Đã xảy ra lỗi khi tải danh sách khóa học.");
    }
  }

  initPurchasedCoursesPage();
});
