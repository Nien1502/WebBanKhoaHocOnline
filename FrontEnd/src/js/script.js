document.addEventListener("DOMContentLoaded", function () {

    const API_BASE = localStorage.getItem('apiBaseUrl') || window.API_BASE_URL || 'http://localhost:8080';
    const apiUrl = function (path) {
        return API_BASE + (path.startsWith('/') ? path : '/' + path);
    };

    function getAuthHeader() {
        const token = localStorage.getItem('authToken') || '';
        const tokenType = localStorage.getItem('authTokenType') || 'Bearer';
        const cachedHeader = localStorage.getItem('authHeader') || '';

        if (cachedHeader && cachedHeader.trim()) {
            return cachedHeader.trim();
        }

        if (token && token.trim()) {
            return (tokenType + ' ' + token).trim();
        }

        return '';
    }

    function withAuthHeaders(extraHeaders) {
        const headers = Object.assign({}, extraHeaders || {});
        const authHeader = getAuthHeader();
        if (authHeader) {
            headers.Authorization = authHeader;
        }
        return headers;
    }

    function isAuthExpired() {
        const expiresAt = Number(localStorage.getItem('authTokenExpiresAt') || 0);
        return expiresAt > 0 && Date.now() >= expiresAt;
    }

    function clearAuthSession() {
        ['authToken', 'authTokenType', 'authTokenExpiresAt', 'authHeader', 'loggedInUser'].forEach(function (key) {
            localStorage.removeItem(key);
        });
    }

    function normalizeLocalDateTime(value) {
        if (!value) {
            return null;
        }

        if (Array.isArray(value)) {
            const year = Number(value[0]);
            const month = Number(value[1]);
            const day = Number(value[2]);
            const hour = Number(value[3] ?? 0);
            const minute = Number(value[4] ?? 0);
            const second = Number(value[5] ?? 0);
            const nano = Number(value[6] ?? 0);
            if (!Number.isFinite(year) || !Number.isFinite(month) || !Number.isFinite(day)) {
                return null;
            }
            const ms = Number.isFinite(nano) ? Math.floor(nano / 1e6) : 0;
            return new Date(year, month - 1, day, hour, minute, second, ms);
        }

        const parsed = new Date(String(value));
        return Number.isNaN(parsed.getTime()) ? null : parsed;
    }

    function normalizePercentage(value) {
        const num = Number(value);
        if (!Number.isFinite(num)) {
            return 0;
        }
        return Math.max(0, Math.min(100, num));
    }

    function escapeHtml(value) {
        return String(value)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function getEnrollmentId(enrollment) {
        const id = enrollment?.id ?? enrollment?.EnrollmentID;
        const normalized = Number(id);
        return Number.isInteger(normalized) ? normalized : null;
    }

    function getCourseIdFromEnrollment(enrollment) {
        const id = enrollment?.course?.id ?? enrollment?.courseId ?? enrollment?.CourseID ?? enrollment?.courseID;
        const normalized = Number(id);
        return Number.isInteger(normalized) ? normalized : null;
    }

    function getCourseNameFromEnrollment(enrollment, courseId) {
        return enrollment?.course?.courseName ?? enrollment?.course?.CourseName ?? (courseId ? ('Khóa học #' + courseId) : 'Khóa học');
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

    async function fetchLessonProgressByEnrollmentId(enrollmentId) {
        const response = await fetch(apiUrl('/api/lesson-progress/enrollment/' + encodeURIComponent(enrollmentId)));
        if (!response.ok) {
            return [];
        }
        const rows = await response.json();
        return Array.isArray(rows) ? rows : [];
    }

    async function fetchLessonById(lessonId) {
        const response = await fetch(apiUrl('/api/lessons/' + encodeURIComponent(lessonId)));
        if (!response.ok) {
            throw new Error('Cannot fetch lesson');
        }
        return response.json();
    }

    function renderContinueLearning(container, model) {
        if (!model) {
            container.hidden = true;
            container.innerHTML = '';
            return;
        }

        const percentLabel = normalizePercentage(model.watchedPercentage).toFixed(0) + '%';
        const courseLabel = model.courseName || ('Khóa học #' + model.courseId);
        const lessonLabel = model.lessonTitle ? ('Bài gần nhất: ' + model.lessonTitle) : 'Bài gần nhất';
        const safeCourseLabel = escapeHtml(courseLabel);
        const safeLessonLabel = escapeHtml(lessonLabel);

        container.hidden = false;
        container.innerHTML = [
            '<div class="continue-learning-card">',
            '  <div class="continue-learning-main">',
            '    <div class="continue-learning-progress" style="--progress:' + String(normalizePercentage(model.watchedPercentage).toFixed(2)) + '">',
            '      <span>' + percentLabel + '</span>',
            '    </div>',
            '    <div class="continue-learning-text">',
            '      <div class="continue-learning-course" title="' + safeCourseLabel + '">' + safeCourseLabel + '</div>',
            '      <div class="continue-learning-lesson" title="' + safeLessonLabel + '">' + safeLessonLabel + '</div>',
            '    </div>',
            '  </div>',
            '  <div class="continue-learning-actions">',
            '    <a class="btn" href="lesson.html?courseId=' + encodeURIComponent(model.courseId) + '&lessonId=' + encodeURIComponent(model.lessonId) + '">Học ngay</a>',
            '  </div>',
            '</div>'
        ].join('');
    }

    async function initContinueLearning() {
        const container = document.getElementById('continue-learning');
        if (!container) {
            return;
        }

        const loggedInUser = JSON.parse(localStorage.getItem('loggedInUser'));
        if (!loggedInUser || !loggedInUser.id) {
            renderContinueLearning(container, null);
            return;
        }

        container.hidden = false;
        container.innerHTML = '<div class="continue-learning-card"><div class="continue-learning-main"><div class="continue-learning-progress" style="--progress:0"><span>...</span></div><div class="continue-learning-text"><div class="continue-learning-course">Đang tải khóa học gần nhất...</div><div class="continue-learning-lesson">Vui lòng chờ...</div></div></div></div>';

        try {
            const enrollmentsResponse = await fetch(apiUrl('/api/enrollments/user/' + encodeURIComponent(loggedInUser.id)));
            if (!enrollmentsResponse.ok) {
                renderContinueLearning(container, null);
                return;
            }

            const enrollments = await enrollmentsResponse.json();
            const list = Array.isArray(enrollments) ? enrollments : [];
            if (!list.length) {
                renderContinueLearning(container, null);
                return;
            }

            const progressByEnrollment = await Promise.all(list.map(async function (enrollment) {
                const enrollmentId = getEnrollmentId(enrollment);
                if (!enrollmentId) {
                    return { enrollment: enrollment, rows: [] };
                }
                const rows = await fetchLessonProgressByEnrollmentId(enrollmentId);
                return { enrollment: enrollment, rows: rows };
            }));

            let best = null;
            progressByEnrollment.forEach(function (entry) {
                const enrollment = entry.enrollment;
                const courseId = getCourseIdFromEnrollment(enrollment);
                const courseName = getCourseNameFromEnrollment(enrollment, courseId);

                (entry.rows || []).forEach(function (row) {
                    const lessonId = getProgressLessonId(row);
                    if (!lessonId) {
                        return;
                    }

                    const watchedPercentage = normalizePercentage(row?.watchedPercentage ?? row?.WatchedPercentage);
                    const lastWatchedAt = normalizeLocalDateTime(row?.lastWatchedAt ?? row?.LastWatchedAt);
                    const stamp = lastWatchedAt ? lastWatchedAt.getTime() : 0;

                    const candidate = {
                        courseId: courseId,
                        courseName: courseName,
                        lessonId: lessonId,
                        watchedPercentage: watchedPercentage,
                        lastWatchedStamp: stamp
                    };

                    if (!best) {
                        best = candidate;
                        return;
                    }

                    if (candidate.lastWatchedStamp > best.lastWatchedStamp) {
                        best = candidate;
                        return;
                    }

                    if (candidate.lastWatchedStamp === best.lastWatchedStamp && candidate.watchedPercentage > best.watchedPercentage) {
                        best = candidate;
                    }
                });
            });

            if (!best || !best.lessonId || !best.courseId) {
                renderContinueLearning(container, null);
                return;
            }

            try {
                const lesson = await fetchLessonById(best.lessonId);
                best.lessonTitle = lesson?.lessonTitle ?? lesson?.LessonTitle ?? '';
            } catch (error) {
                best.lessonTitle = '';
            }

            renderContinueLearning(container, best);
        } catch (error) {
            console.error('Continue learning load failed:', error);
            renderContinueLearning(container, null);
        }
    }

    function buildCategoryHref(categoryId, categoryName) {
        return 'category.html?categoryId=' + encodeURIComponent(categoryId) + '&name=' + encodeURIComponent(categoryName);
        
    }

    function getCategoryId(item) {
        const id = item?.id ?? item?.CategoryID ?? item?.categoryId ?? item?.categoryID ?? '';
        return id;
    }

    function getCategoryName(item) {
        const name = item?.categoryName ?? item?.CategoryName ?? item?.name ?? 'Danh mục';
        return name;
    }

    function getCategoryIcon(categoryName) {
        const name = String(categoryName || '').toLowerCase();
        if (name.includes('toán') || name.includes('toan')) return 'fa-square-root-alt';
        if (name.includes('anh') || name.includes('english')) return 'fa-language';
        if (name.includes('văn') || name.includes('van')) return 'fa-pen-nib';
        if (name.includes('miễn phí') || name.includes('mien phi') || name.includes('tất cả') || name.includes('tat ca')) return 'fa-layer-group';
        return 'fa-book';
    }

    function getCategoryDescription(categoryName) {
        const name = String(categoryName || '').toLowerCase();
        if (name.includes('toán') || name.includes('toan')) return 'Hệ thống bài học từ cơ bản đến nâng cao.';
        if (name.includes('anh') || name.includes('english')) return 'Lộ trình phát âm, từ vựng và giao tiếp.';
        if (name.includes('văn') || name.includes('van')) return 'Phân tích tác phẩm và kỹ năng viết bài thi.';
        if (name.includes('miễn phí') || name.includes('mien phi')) return 'Tổng hợp các khóa học miễn phí nổi bật.';
        if (name.includes('tất cả') || name.includes('tat ca')) return 'Khám phá đầy đủ tất cả khóa học hiện có.';
        return 'Khám phá các khóa học thuộc danh mục này.';
    }

    function fetchMenuCategories() {
        return fetch(apiUrl('/api/categories'))
            .then(function (response) {
                if (!response.ok) {
                    throw new Error('Cannot fetch categories');
                }
                return response.json();
            })
            .catch(function () {
                // Fallback data if API is not ready.
                return [
                    { CategoryID: 1, CategoryName: 'Toán học' },
                    { CategoryID: 2, CategoryName: 'Anh văn' },
                    { CategoryID: 3, CategoryName: 'Ngữ văn' },
                    { CategoryID: 4, CategoryName: 'Tất cả khóa học' }
                ];
            });
    }

    // Xử lý thanh tìm kiếm
    const searchIcon = document.querySelector('#search-icon');
    const searchForm = document.querySelector('#search-form');
    const closeIcon = document.querySelector('#close');

    if (searchIcon && searchForm && closeIcon) {
        searchIcon.onclick = () => {
            searchForm.classList.toggle('active');
        };

        closeIcon.onclick = () => {
            searchForm.classList.remove('active');
        };
    }

    const cachedUser = JSON.parse(localStorage.getItem('loggedInUser'));
    const userInfoEl = document.getElementById('userInfo');
    const searchInput = document.getElementById('menu-search-input');
    const clearBtn = document.querySelector('.clear-search');
    const openCourseLink = document.getElementById('open-course-link');

    if (cachedUser && userInfoEl) {
        userInfoEl.textContent = cachedUser.username;
    }

    if (searchInput && clearBtn) {
        clearBtn.addEventListener('click', function () {
            searchInput.value = '';
            searchInput.focus();
        });
    }

    if (openCourseLink) {
        openCourseLink.addEventListener('click', async function (event) {
            event.preventDefault();

            const currentUser = JSON.parse(localStorage.getItem('loggedInUser'));
            if (!currentUser || !currentUser.id) {
                alert('Vui lòng đăng nhập để vào khóa học.');
                window.location.href = 'login.html';
                return;
            }

            if (isAuthExpired()) {
                clearAuthSession();
                alert('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
                window.location.href = 'login.html';
                return;
            }

            if (!getAuthHeader()) {
                alert('Không tìm thấy phiên đăng nhập hợp lệ. Vui lòng đăng nhập lại.');
                window.location.href = 'login.html';
                return;
            }

            try {
                const ordersResponse = await fetch(apiUrl('/api/orders/user/' + encodeURIComponent(currentUser.id)), {
                    headers: withAuthHeaders({
                        'Accept': 'application/json'
                    })
                });

                if (!ordersResponse.ok) {
                    const errorPayload = await ordersResponse.json().catch(function () {
                        return null;
                    });
                    const message = errorPayload && errorPayload.message
                        ? errorPayload.message
                        : 'Không tải được danh sách đơn hàng.';
                    throw new Error(message);
                }

                const orders = await ordersResponse.json();
                const successOrders = (orders || []).filter(function (order) {
                    const status = String(order?.status ?? order?.Status ?? '').toUpperCase();
                    return status === 'SUCCESS';
                });

                for (const order of successOrders) {
                    const orderId = order?.id ?? order?.OrderID;
                    if (orderId == null) {
                        continue;
                    }

                    const detailsResponse = await fetch(apiUrl('/api/order-details/order/' + encodeURIComponent(orderId)), {
                        headers: withAuthHeaders({
                            'Accept': 'application/json'
                        })
                    });
                    if (!detailsResponse.ok) {
                        continue;
                    }

                    const details = await detailsResponse.json();
                    const firstCourseId = (details || []).map(function (detail) {
                        return detail?.course?.id ?? detail?.CourseID ?? detail?.courseId;
                    }).find(function (value) {
                        return Number.isInteger(Number(value));
                    });

                    if (firstCourseId != null) {
                        window.location.href = 'course.html?id=' + Number(firstCourseId);
                        return;
                    }
                }

                alert('Bạn chưa có khóa học đã kích hoạt. Hãy mua khóa học trước nhé.');
                window.location.href = 'cart.html';
            } catch (error) {
                console.error('Lỗi khi mở khóa học:', error);
                const message = String(error && error.message ? error.message : '');

                if (/token|authorization|đăng nhập|hết hạn/i.test(message)) {
                    clearAuthSession();
                    alert('Phiên đăng nhập không hợp lệ hoặc đã hết hạn. Vui lòng đăng nhập lại.');
                    window.location.href = 'login.html';
                    return;
                }

                if (/quyền|forbidden/i.test(message)) {
                    alert('Bạn không có quyền truy cập khóa học này.');
                    return;
                }

                alert('Không thể mở khóa học lúc này. Vui lòng thử lại sau.');
            }
        });
    }

    const categoryListEl = document.getElementById('menu-category-list');
    const categoryTrackEl = document.getElementById('menu-course-strip-track');
    const stripRootEl = document.querySelector('.menu-course-strip');
    const stripPrevBtn = stripRootEl ? stripRootEl.querySelector('.menu-strip-nav.prev') : null;
    const stripNextBtn = stripRootEl ? stripRootEl.querySelector('.menu-strip-nav.next') : null;

    initContinueLearning();

    if (categoryListEl || categoryTrackEl) {
        fetchMenuCategories()
            .then(function (categories) {
                if (!Array.isArray(categories) || categories.length === 0) {
                    if (categoryListEl) {
                        categoryListEl.innerHTML = '<a href="#" class="menu-category-item">Khong co danh muc</a>';
                    }
                    if (categoryTrackEl) {
                        categoryTrackEl.innerHTML = '<a href="#" class="menu-course-card"><i class="fas fa-book"></i><h3>Danh mục</h3><p>Không có dữ liệu danh mục.</p></a>';
                    }
                    return;
                }

                if (categoryListEl) {
                    categoryListEl.innerHTML = categories.map(function (item) {
                        const id = getCategoryId(item);
                        const name = getCategoryName(item);
                        return '<a class="menu-category-item" href="' + buildCategoryHref(id, name) + '">' + name + '</a>';
                    }).join('');
                }

                if (categoryTrackEl) {
                    categoryTrackEl.innerHTML = categories.map(function (item) {
                        const id = getCategoryId(item);
                        const name = getCategoryName(item);
                        const icon = getCategoryIcon(name);
                        const description = getCategoryDescription(name);
                        return [
                            '<a href="' + buildCategoryHref(id, name) + '" class="menu-course-card">',
                            '  <i class="fas ' + icon + '"></i>',
                            '  <h3>' + name + '</h3>',
                            '  <p>' + description + '</p>',
                            '</a>'
                        ].join('');
                    }).join('');
                }

                if (categoryTrackEl && stripPrevBtn && stripNextBtn) {
                    const scrollByAmount = function (dir) {
                        const amount = Math.max(240, Math.floor(categoryTrackEl.clientWidth * 0.9));
                        categoryTrackEl.scrollBy({ left: dir * amount, behavior: 'smooth' });
                    };

                    stripPrevBtn.addEventListener('click', function () {
                        scrollByAmount(-1);
                    });
                    stripNextBtn.addEventListener('click', function () {
                        scrollByAmount(1);
                    });
                }
            })
            .catch(function () {
                if (categoryListEl) {
                    categoryListEl.innerHTML = '<a href="#" class="menu-category-item">Tai danh muc that bai</a>';
                }
                if (categoryTrackEl) {
                    categoryTrackEl.innerHTML = '<a href="#" class="menu-course-card"><i class="fas fa-book"></i><h3>Lỗi tải dữ liệu</h3><p>Không thể tải danh mục từ API.</p></a>';
                }
            });
    }

    // Swiper slider
    const sliderRoot = document.querySelector('.home-slider');
    const sliderWrapper = document.querySelector('.home-slider .swiper-wrapper');

    if (sliderRoot && sliderWrapper) {
        const featuredSlides = [
            {
                label: 'Khoa hoc noi bat',
                title: 'Luyen thi IELTS',
                desc: 'Chinh phuc IELTS: Lo trinh hieu qua de dat band diem mo uoc!',
                href: buildCategoryHref(2, 'Anh van'),
                image: '../src/assets/img/IELTS.png',
                alt: 'Luyen thi IELTS'
            },
            {
                label: 'Khoa hoc noi bat',
                title: 'Viet van sang tao',
                desc: 'Hoc Ngu Van: Hieu y - Dien dat - Ghi diem!',
                href: buildCategoryHref(3, 'Ngữ văn'),
                image: '../src/assets/img/Van-sang-tao.png',
                alt: 'Viet van sang tao'
            },
            {
                label: 'Khoa hoc noi bat',
                title: 'Toan nang cao',
                desc: 'Hoc Toan: Tu duy sang tao - Giai bai hieu qua!',
                href: buildCategoryHref(1, 'Toan hoc'),
                image: '../src/assets/img/Toan-nang-cao.png',
                alt: 'Toan nang cao'
            }
        ];

        // Render slider data from an array so it can be replaced by API later.
        sliderWrapper.innerHTML = featuredSlides.map(function (item) {
            return [
                '<div class="swiper-slide slide">',
                '  <div class="content">',
                '      <span>' + item.label + '</span>',
                '      <h3>' + item.title + '</h3>',
                '      <p>' + item.desc + '</p>',
                '      <a href="' + item.href + '" class="btn">Xem ngay</a>',
                '  </div>',
                '  <div class="image">',
                '      <img src="' + item.image + '" alt="' + item.alt + '">',
                '  </div>',
                '</div>'
            ].join('');
        }).join('');

        const slider = new Swiper('.home-slider', {
            slidesPerView: 1,
            spaceBetween: 20,
            centeredSlides: false,
            loop: featuredSlides.length > 1,
            speed: 700,
            autoplay: {
                delay: 4500,
                disableOnInteraction: false,
                pauseOnMouseEnter: true,
            },
            pagination: {
                el: '.swiper-pagination',
                clickable: true,
            },
            navigation: {
                nextEl: '.home-slider-next',
                prevEl: '.home-slider-prev',
            },
        });

        const nextBtn = document.querySelector('.home-slider-next');
        const prevBtn = document.querySelector('.home-slider-prev');

        [nextBtn, prevBtn].forEach(function (btn) {
            if (btn) {
                btn.addEventListener('click', function () {
                    if (slider.autoplay) {
                        slider.autoplay.start();
                    }
                });
            }
        });
    }


});
