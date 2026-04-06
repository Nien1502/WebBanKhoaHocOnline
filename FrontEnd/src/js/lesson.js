document.addEventListener("DOMContentLoaded", function () {
  const API_BASE = localStorage.getItem("apiBaseUrl") || window.API_BASE_URL || "http://localhost:8080";
  const apiUrl = function (path) {
    return API_BASE + (path.startsWith("/") ? path : "/" + path);
  };
  const API_ORIGIN = API_BASE.replace(/\/+$/, "");

  const loggedInUser = JSON.parse(localStorage.getItem("loggedInUser"));
  const userInfo = document.getElementById("userInfo");
  if (loggedInUser && loggedInUser.username && userInfo) {
    userInfo.textContent = loggedInUser.username;
  }

  if (!loggedInUser || !loggedInUser.id) {
    alert("Vui lòng đăng nhập để học khóa học.");
    window.location.href = "login.html";
    return;
  }

  const params = new URLSearchParams(window.location.search);
  let courseId = Number(params.get("courseId"));
  const preferredLessonId = Number(params.get("lessonId"));
  let redirecting = false;

  const courseTitleEl = document.getElementById("courseTitle");
  const lessonSummaryEl = document.getElementById("lessonSummary");
  const playerTitleEl = document.getElementById("playerTitle");
  const playerMetaEl = document.getElementById("playerMeta");
  const playerEl = document.getElementById("mainPlayer");
  const playerEmptyEl = document.getElementById("playerEmpty");
  const lessonListEl = document.getElementById("lessonList");

  let enrollmentId = null;
  let activeLessonIndex = -1;
  let activeLessons = [];
  let autoSaveTimer = null;
  let saveInFlight = false;
  const progressByLessonId = new Map();

  async function fetchLessonById(id) {
    const response = await fetch(apiUrl("/api/lessons/") + encodeURIComponent(id));
    if (!response.ok) {
      throw new Error("Không thể tải thông tin bài học.");
    }
    return response.json();
  }

  async function ensureCourseId() {
    if (Number.isInteger(courseId) && courseId > 0) {
      return;
    }

    if (Number.isInteger(preferredLessonId) && preferredLessonId > 0) {
      const lesson = await fetchLessonById(preferredLessonId);
      const derivedCourseId = Number(lesson?.course?.id ?? lesson?.CourseID ?? lesson?.courseId);
      if (Number.isInteger(derivedCourseId) && derivedCourseId > 0) {
        courseId = derivedCourseId;
        return;
      }
    }

    alert("Thiếu courseId hợp lệ để mở bài học.");
    window.location.href = "course.html";
    redirecting = true;
  }

  async function fetchCourseById(id) {
    const response = await fetch(apiUrl("/api/courses/") + encodeURIComponent(id));
    if (!response.ok) {
      throw new Error("Không thể tải thông tin khóa học.");
    }
    return response.json();
  }

  async function fetchLessonsByCourseId(id) {
    const orderedResponse = await fetch(apiUrl(`/api/lessons/course/${id}/ordered`));
    if (orderedResponse.ok) {
      const orderedLessons = await orderedResponse.json();
      return Array.isArray(orderedLessons) ? orderedLessons : [];
    }

    const fallbackResponse = await fetch(apiUrl(`/api/lessons/course/${id}`));
    if (!fallbackResponse.ok) {
      throw new Error("Không thể tải danh sách bài học của khóa học này.");
    }
    const lessons = await fallbackResponse.json();
    return Array.isArray(lessons) ? lessons : [];
  }

  async function fetchEnrollmentByUserAndCourse(userId, cId) {
    const response = await fetch(apiUrl(`/api/enrollments/user/${userId}/course/${cId}`));
    if (response.status === 404) {
      return null;
    }
    if (!response.ok) {
      throw new Error("Không thể tải thông tin enrollment để lưu tiến độ học.");
    }
    return response.json();
  }

  async function fetchLessonProgressByEnrollmentId(eId) {
    const response = await fetch(apiUrl(`/api/lesson-progress/enrollment/${eId}`));
    if (!response.ok) {
      return [];
    }
    const rows = await response.json();
    return Array.isArray(rows) ? rows : [];
  }

  function formatDurationSeconds(rawValue) {
    const seconds = Number(rawValue);
    if (!Number.isFinite(seconds) || seconds <= 0) {
      return "Chưa có thời lượng";
    }

    if (seconds < 60) {
      return Math.round(seconds) + " giây";
    }

    const minutes = Math.floor(seconds / 60);
    const remainSeconds = Math.round(seconds % 60);

    if (minutes < 60) {
      return remainSeconds > 0 ? minutes + " phút " + remainSeconds + " giây" : minutes + " phút";
    }

    const hours = Math.floor(minutes / 60);
    const remainMinutes = minutes % 60;
    const hourPart = hours + " giờ";
    const minutePart = remainMinutes > 0 ? " " + remainMinutes + " phút" : "";
    return hourPart + minutePart;
  }

  function getLessonTitle(lesson, index) {
    return lesson?.lessonTitle ?? lesson?.LessonTitle ?? ("Bài học #" + (index + 1));
  }

  function getLessonDuration(lesson) {
    return lesson?.duration ?? lesson?.Duration;
  }

  function getLessonVideoUrl(lesson) {
    const raw = String(lesson?.videoURL ?? lesson?.videourl ?? lesson?.VideoURL ?? "").trim();
    if (!raw) {
      return "";
    }

    // Already a full URL.
    if (/^https?:\/\//i.test(raw)) {
      return raw;
    }

    // Stored as backend static path.
    if (raw.startsWith("/assets/videos/")) {
      return API_ORIGIN + raw;
    }

    if (raw.startsWith("assets/videos/")) {
      return API_ORIGIN + "/" + raw;
    }

    // Stored as filename only (the current admin flow saves this format).
    const fileName = raw.split(/[\\/]/).pop();
    if (!fileName) {
      return "";
    }

    return API_ORIGIN + "/assets/videos/" + encodeURIComponent(fileName);
  }

  function getProgressLessonId(progressRow) {
    const directId = progressRow?.lessonId ?? progressRow?.LessonID;
    if (directId != null) {
      const normalized = Number(directId);
      if (Number.isInteger(normalized)) {
        return normalized;
      }
    }

    const nestedId = progressRow?.lesson?.id ?? progressRow?.lesson?.LessonID;
    if (nestedId != null) {
      const normalized = Number(nestedId);
      if (Number.isInteger(normalized)) {
        return normalized;
      }
    }

    return null;
  }

  function getLessonId(lesson) {
    const normalized = Number(lesson?.id ?? lesson?.LessonID);
    return Number.isInteger(normalized) ? normalized : null;
  }

  function normalizePercentage(value) {
    const num = Number(value);
    if (!Number.isFinite(num)) {
      return 0;
    }
    return Math.max(0, Math.min(100, num));
  }

  function stopAutoSave() {
    if (autoSaveTimer) {
      clearInterval(autoSaveTimer);
      autoSaveTimer = null;
    }
  }

  function startAutoSave() {
    stopAutoSave();
    autoSaveTimer = setInterval(function () {
      persistCurrentLessonProgress(false);
    }, 10000);
  }

  function setSaveMeta(text) {
    if (!playerMetaEl) {
      return;
    }

    const current = playerMetaEl.textContent || "";
    const clean = current.split(" | Đồng bộ:")[0];
    playerMetaEl.textContent = clean + " | Đồng bộ: " + text;
  }

  function buildProgressPayload(lessonId, watchedPercentage, isCompleted, existingId) {
    const payload = {
      enrollment: { id: enrollmentId },
      lesson: { id: lessonId },
      watchedPercentage: Number(normalizePercentage(watchedPercentage).toFixed(2)),
      isCompleted: Boolean(isCompleted),
      lastWatchedAt: new Date().toISOString()
    };

    if (existingId != null) {
      payload.id = existingId;
    }

    return payload;
  }

  async function upsertLessonProgress(lessonId, watchedPercentage, isCompleted) {
    if (!enrollmentId) {
      return;
    }

    const existing = progressByLessonId.get(lessonId);
    const existingId = existing?.id ?? existing?.ProgressID ?? null;
    const previousPercent = normalizePercentage(existing?.watchedPercentage ?? existing?.WatchedPercentage);
    const mergedPercent = Math.max(previousPercent, normalizePercentage(watchedPercentage));
    const mergedCompleted = Boolean(existing?.isCompleted ?? existing?.IsCompleted) || Boolean(isCompleted);

    const payload = buildProgressPayload(lessonId, mergedPercent, mergedCompleted, existingId);
    const endpoint = existingId != null ? `/api/lesson-progress/${existingId}` : "/api/lesson-progress";
    const method = existingId != null ? "PUT" : "POST";

    const response = await fetch(apiUrl(endpoint), {
      method: method,
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    });

    if (!response.ok) {
      throw new Error("Không thể lưu tiến độ học vào lesson_progress.");
    }

    const saved = await response.json();
    progressByLessonId.set(lessonId, {
      ...saved,
      watchedPercentage: mergedPercent,
      isCompleted: mergedCompleted
    });
  }

  async function persistCurrentLessonProgress(forceCompleted) {
    if (saveInFlight || !enrollmentId || activeLessonIndex < 0 || !activeLessons.length || playerEl.style.display === "none") {
      return;
    }

    const lesson = activeLessons[activeLessonIndex];
    const lessonId = getLessonId(lesson);
    if (!lessonId) {
      return;
    }

    const duration = Number(playerEl.duration);
    if (!Number.isFinite(duration) || duration <= 0) {
      return;
    }

    const currentTime = Math.max(0, Number(playerEl.currentTime) || 0);
    const watchedPercentage = normalizePercentage((currentTime / duration) * 100);
    const isCompleted = Boolean(forceCompleted) || watchedPercentage >= 99.5;

    try {
      saveInFlight = true;
      await upsertLessonProgress(lessonId, watchedPercentage, isCompleted);
      setSaveMeta("Đã lưu " + new Date().toLocaleTimeString("vi-VN"));
    } catch (error) {
      console.error("Lỗi lưu lesson_progress:", error);
      setSaveMeta("Lỗi lưu");
    } finally {
      saveInFlight = false;
    }
  }

  function restoreProgressForLesson(lesson) {
    const lessonId = getLessonId(lesson);
    if (!lessonId || !enrollmentId) {
      return;
    }

    const row = progressByLessonId.get(lessonId);
    const watchedPercentage = normalizePercentage(row?.watchedPercentage ?? row?.WatchedPercentage);
    if (watchedPercentage <= 0 || watchedPercentage >= 99.5) {
      return;
    }

    const onLoaded = function () {
      const duration = Number(playerEl.duration);
      if (!Number.isFinite(duration) || duration <= 0) {
        return;
      }

      const targetTime = (watchedPercentage / 100) * duration;
      if (targetTime > 1 && targetTime < duration - 1) {
        playerEl.currentTime = targetTime;
      }
    };

    playerEl.addEventListener("loadedmetadata", onLoaded, { once: true });
  }

  function setActiveLesson(lesson, index, lessons) {
    persistCurrentLessonProgress(false);

    const title = getLessonTitle(lesson, index);
    const durationLabel = formatDurationSeconds(getLessonDuration(lesson));
    const videoUrl = getLessonVideoUrl(lesson);

    activeLessonIndex = index;
    activeLessons = lessons;

    playerTitleEl.textContent = title;
    playerMetaEl.textContent = "Bài " + (index + 1) + "/" + lessons.length + " | Thời lượng: " + durationLabel;

    if (videoUrl) {
      playerEl.src = videoUrl;
      playerEl.style.display = "block";
      playerEmptyEl.classList.remove("visible");
      restoreProgressForLesson(lesson);
    } else {
      playerEl.removeAttribute("src");
      playerEl.load();
      playerEl.style.display = "none";
      playerEmptyEl.classList.add("visible");
    }

    const allItems = lessonListEl.querySelectorAll(".lesson-item[data-index]");
    allItems.forEach(function (node) {
      node.classList.remove("active");
    });
    const activeNode = lessonListEl.querySelector('.lesson-item[data-index="' + index + '"]');
    if (activeNode) {
      activeNode.classList.add("active");
    }
  }

  function renderLessons(lessons) {
    lessonListEl.innerHTML = "";

    if (!Array.isArray(lessons) || lessons.length === 0) {
      lessonSummaryEl.textContent = "Khóa học này chưa có bài học nào.";
      lessonListEl.innerHTML = '<div class="lesson-item">Chưa có bài học để hiển thị.</div>';
      playerTitleEl.textContent = "Chưa có bài học";
      playerMetaEl.textContent = "Vui lòng quay lại sau.";
      playerEl.style.display = "none";
      playerEmptyEl.classList.add("visible");
      stopAutoSave();
      return;
    }

    lessonSummaryEl.textContent = "Tổng số bài học: " + lessons.length;

    lessons.forEach(function (lesson, index) {
      const title = getLessonTitle(lesson, index);
      const duration = getLessonDuration(lesson);
      const videoUrl = getLessonVideoUrl(lesson);

      const item = document.createElement("button");
      item.type = "button";
      item.className = "lesson-item";
      item.setAttribute("data-index", String(index));

      const badge = document.createElement("span");
      badge.className = "lesson-item-index";
      badge.textContent = String(index + 1);

      const heading = document.createElement("h3");
      heading.className = "lesson-item-title";
      heading.textContent = title;

      const meta = document.createElement("p");
      meta.className = "lesson-item-meta";
      meta.textContent = "Thời lượng: " + formatDurationSeconds(duration) + (videoUrl ? "" : " | Chưa có video");

      item.appendChild(badge);
      item.appendChild(heading);
      item.appendChild(meta);

      item.addEventListener("click", function () {
        setActiveLesson(lesson, index, lessons);
      });

      lessonListEl.appendChild(item);
    });

    let startIndex = 0;
    if (Number.isInteger(preferredLessonId) && preferredLessonId > 0) {
      const idx = lessons.findIndex(function (l) {
        return getLessonId(l) === preferredLessonId;
      });
      if (idx >= 0) {
        startIndex = idx;
      }
    }

    setActiveLesson(lessons[startIndex], startIndex, lessons);
  }

  playerEl.addEventListener("play", function () {
    startAutoSave();
  });

  playerEl.addEventListener("pause", function () {
    persistCurrentLessonProgress(false);
    stopAutoSave();
  });

  playerEl.addEventListener("ended", function () {
    persistCurrentLessonProgress(true);
    stopAutoSave();
  });

  window.addEventListener("beforeunload", function () {
    persistCurrentLessonProgress(false);
  });

  document.addEventListener("visibilitychange", function () {
    if (document.hidden) {
      persistCurrentLessonProgress(false);
      stopAutoSave();
    }
  });

  async function initLessonPage() {
    try {
      await ensureCourseId();

      if (redirecting) {
        return;
      }

      const [course, lessons, enrollment] = await Promise.all([
        fetchCourseById(courseId),
        fetchLessonsByCourseId(courseId),
        fetchEnrollmentByUserAndCourse(loggedInUser.id, courseId)
      ]);

      enrollmentId = Number(enrollment?.id ?? enrollment?.EnrollmentID ?? 0) || null;
      if (enrollmentId) {
        const progressRows = await fetchLessonProgressByEnrollmentId(enrollmentId);
        progressRows.forEach(function (row) {
          const lessonId = getProgressLessonId(row);
          if (lessonId != null) {
            progressByLessonId.set(lessonId, row);
          }
        });
      } else {
        console.warn("Không tìm thấy enrollment, tiến độ học sẽ không lưu vào lesson_progress.");
      }

      const courseName = course?.courseName ?? course?.CourseName ?? ("Khóa học #" + courseId);
      courseTitleEl.textContent = "Bài học: " + courseName;

      renderLessons(lessons);
    } catch (error) {
      console.error("Lỗi khi tải trang bài học:", error);
      lessonSummaryEl.textContent = "Không thể tải danh sách bài học.";
      lessonListEl.innerHTML = '<div class="lesson-item">Đã xảy ra lỗi khi tải dữ liệu bài học.</div>';
      playerTitleEl.textContent = "Không tải được bài học";
      playerMetaEl.textContent = "Hãy thử tải lại trang.";
      playerEl.style.display = "none";
      playerEmptyEl.classList.add("visible");
      alert(error && error.message ? error.message : "Đã có lỗi xảy ra khi tải trang bài học.");
    }
  }

  initLessonPage();
});
