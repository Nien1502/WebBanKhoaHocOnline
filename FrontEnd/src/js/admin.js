const API_BASE = window.API_BASE_URL || localStorage.getItem('apiBaseUrl') || 'http://localhost:8080';
function apiUrl(path) {
    return `${API_BASE}${path}`;
}

function getAuthHeader() {
    const token = localStorage.getItem('authToken') || '';
    const tokenType = localStorage.getItem('authTokenType') || 'Bearer';
    const cachedHeader = localStorage.getItem('authHeader') || '';

    if (cachedHeader && cachedHeader.trim()) {
        return cachedHeader.trim();
    }

    if (token && token.trim()) {
        return `${tokenType} ${token}`.trim();
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

function getCategoryId(category) {
    return category?.id ?? category?.CategoryID ?? null;
}

function getCategoryName(category) {
    return category?.categoryName ?? category?.CategoryName ?? 'Khong xac dinh';
}

function getCourseId(course) {
    return course?.id ?? course?.CourseID ?? null;
}

function getCourseName(course) {
    return course?.courseName ?? course?.CourseName ?? 'Khong co ten';
}

function getCourseDescription(course) {
    return course?.description ?? course?.Description ?? 'Khong co mo ta';
}

function getCoursePrice(course) {
    return course?.price ?? course?.Price ?? 0;
}

function getCourseImage(course) {
    return String(course?.image ?? course?.Imag ?? "").trim();
}

function getCourseCategoryId(course) {
    return course?.category?.id ?? course?.CategoryID ?? null;
}

function getUserId(user) {
    return user?.id ?? user?.UserID ?? null;
}

function getUserName(user) {
    return user?.username ?? user?.UserName ?? 'Khong xac dinh';
}

function getUserEmail(user) {
    return user?.email ?? user?.Email ?? '';
}

function getUserStatus(user) {
    return user?.status ?? user?.Status ?? 'Bi khoa';
}

function isActiveUserStatus(status) {
    const normalized = String(status || '').trim().toLowerCase();
    return normalized === 'hoat dong' || normalized === 'hoạt động';
}

function toUserStatusLabel(status) {
    return isActiveUserStatus(status) ? 'Hoạt động' : 'Bị khóa';
}

function getUserCreateTime(user) {
    return user?.createTime ?? user?.CreateTime ?? null;
}

function getOrderId(order) {
    return order?.id ?? order?.OrderID ?? null;
}

function getOrderDate(order) {
    return order?.orderDate ?? order?.OrderDate ?? null;
}

function getOrderTotal(order) {
    return order?.totalAmount ?? order?.TotalAmount ?? 0;
}

function getOrderStatus(order) {
    return String(order?.status ?? order?.Status ?? '').toUpperCase();
}

function getOrderUserName(order) {
    return order?.user?.username ?? order?.UserName ?? 'Khong xac dinh';
}

function statusLabelFromEnum(status) {
    return status === 'SUCCESS' ? 'Da xu ly' : status === 'PENDING' ? 'Chua xu ly' : 'That bai';
}

  function resolveCourseImageUrl(course) {
    const imageValue = getCourseImage(course);
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

function normalizeOrderStatusFilterValue(rawValue) {
    const value = String(rawValue || '').trim().toUpperCase();
    if (value === '1') return 'SUCCESS';
    if (value === '0') return 'PENDING';
    if (value === '3') return 'FAILED';
    if (value === 'SUCCESS' || value === 'PENDING' || value === 'FAILED') {
        return value;
    }
    return 'ALL';
}

function getOrderStatusBadge(statusEnum) {
    if (statusEnum === 'SUCCESS') {
        return '<span class="status-complete">Da xu ly</span>';
    }
    if (statusEnum === 'FAILED') {
        return '<span class="status-failed">Thanh toan that bai</span>';
    }
    return '<span class="status-no-complete">Chua xu ly</span>';
}

async function fetchOrdersByStatusFilter(statusFilter) {
    const normalizedStatus = normalizeOrderStatusFilterValue(statusFilter);
    const endpoint = normalizedStatus === 'ALL'
        ? '/api/orders'
        : `/api/orders/status/${encodeURIComponent(normalizedStatus)}`;

    const response = await fetch(apiUrl(endpoint), {
        headers: withAuthHeaders(),
    });

    if (!response.ok) {
        throw new Error(await response.text());
    }

    const orders = await response.json();
    return Array.isArray(orders) ? orders : [];
}

//do sidebar open and close
const menuIconButton = document.querySelector(".menu-icon-btn");
const sidebar = document.querySelector(".sidebar");
menuIconButton.addEventListener("click", () => {
    sidebar.classList.toggle("open");
});

// tab for section
const sidebars = document.querySelectorAll(".sidebar-list-item.tab-content");
const sections = document.querySelectorAll(".section");

for (let i = 0; i < sidebars.length; i++) {
    sidebars[i].onclick = function () {
        document.querySelector(".sidebar-list-item.active").classList.remove("active");
        document.querySelector(".section.active").classList.remove("active");
        sidebars[i].classList.add("active");
        sections[i].classList.add("active");
    };
}

const closeBtn = document.querySelectorAll('.section');
console.log(closeBtn[0])
for (let i = 0; i < closeBtn.length; i++) {
    closeBtn[i].addEventListener('click', (e) => {
        sidebar.classList.add("open");
    })
}


// Get amount categorie
async function getAmountCourses() {
    try {
        const response = await fetch(apiUrl('/api/courses'), {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });

        console.log('Phan hoi tu API:', response);

        if (response.ok) {
            const courses = await response.json();
            return courses.length;
        } else {
            console.error('Loi khi lay danh sach khoa hoc:', await response.text());
            return 0;
        }
    } catch (error) {
        console.error('Loi khi lay danh sach khoa hoc:', error);
        return 0;
    }
}

async function getAmountUser() {
    try {
        const response = await fetch(apiUrl('/api/auth/users/non-admins/count'), {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' }
        });

        if (!response.ok) {
            console.error('Loi khi goi API:', await response.text());
            return 0;
        }

        const data = await response.json();
        return data.count;
    } catch (error) {
        console.error('Loi khi lay so luong nguoi dung:', error);
        return 0;
    }
}

// Get amount user
async function getMoney() {
    try {
        const response = await fetch(apiUrl('/api/orders'), {
            method: 'GET',
            headers: withAuthHeaders({
                'Content-Type': 'application/json',
            }),
        });

        if (response.ok) {
            const orders = await response.json();
            return orders
                .filter(order => getOrderStatus(order) === 'SUCCESS')
                .reduce((total, order) => total + Number(getOrderTotal(order) || 0), 0);
        } else {
            console.error('Loi khi lay danh sach don hang:', await response.text());
            return 0;
        }
    } catch (error) {
        console.error('Loi khi lay danh sach don hang:', error);
        return 0;
    }
}


// Dinh nghia ham formatDate
function formatDate(date) {
    if (!date) return "Khong xac dinh"; // Tra ve gia tri mac dinh neu khong co ngay

    const formattedDate = date.replace(" ", "T"); // Chuyen doi dinh dang
    const fm = new Date(formattedDate);
    if (isNaN(fm)) return "Khong xac dinh"; // Tra ve gia tri mac dinh neu ngay khong hop le

    const yyyy = fm.getFullYear();
    const mm = fm.getMonth() + 1;
    const dd = fm.getDate();
    return `${dd < 10 ? "0" + dd : dd}/${mm < 10 ? "0" + mm : mm}/${yyyy}`;
}

function vnd(price) {
    if (price == null || price === undefined) {
        return "Khong xac dinh"; // Gia tri mac dinh neu `price` khong hop le
    }
    return parseFloat(price).toLocaleString('vi-VN', { style: 'currency', currency: 'VND' });
}

// Cap nhat bang dieu khien
async function updateDashboard() {
    try {
        const amountUser = await getAmountUser();
        console.log('So luong nguoi dung khong phai Admin:', amountUser);

        // Kiem tra neu phan tu ton tai truoc khi cap nhat
        const amountUserElement = document.getElementById("amount-user");
        if (amountUserElement) {
            amountUserElement.innerHTML = amountUser;
        } else {
            console.error('Khong tim thay phan tu voi ID "amount-user".');
        }
    } catch (error) {
        console.error('Loi khi cap nhat bang dieu khien:', error);
    }

    try {
        const amountCourses = await getAmountCourses();
        console.log('So luong khoa hoc:', amountCourses);
        const amountCoursesElement = document.getElementById("amount-categorie");
        if (amountCoursesElement) {
            amountCoursesElement.innerHTML = amountCourses;
        } else {
            console.error('Khong tim thay phan tu voi ID "amount-categorie".');
        }

    } catch (error) {
        console.error('Loi khi cap nhat bang dieu khien:', error);
    }

    try {
        const amountMoney = await getMoney();
        console.log('Tong tien:', amountMoney);
        const amountMoneyElement = document.getElementById("amount-money");
        if (amountMoneyElement) {
            amountMoneyElement.innerHTML = vnd(amountMoney);
        } else {
            console.error('Khong tim thay phan tu voi ID "amount-money".');
        }
    } catch (error) {
        console.error('Loi khi cap nhat bang dieu khien:', error);
    }
}
// Phan trang 
let perPage = 12;
let currentPage = 1;
let totalPage = 0;
let percategories = [];

function displayList(coursesAll, perPage, currentPage) {
    let start = (currentPage - 1) * perPage;
    let end = (currentPage - 1) * perPage + perPage;
    let coursesShow = coursesAll.slice(start, end);
    showcoursesArr(coursesShow);
}

function setupPagination(coursesAll, perPage) {
    document.querySelector('.page-nav-list').innerHTML = '';
    let page_count = Math.ceil(coursesAll.length / perPage);
    for (let i = 1; i <= page_count; i++) {
        let li = paginationChange(i, coursesAll, currentPage);
        document.querySelector('.page-nav-list').appendChild(li);
    }
}

function paginationChange(page, coursesAll, currentPage) {
    let node = document.createElement(`li`);
    node.classList.add('page-nav-item');
    node.innerHTML = `<a href="#">${page}</a>`;
    if (currentPage == page) node.classList.add('active');
    node.addEventListener('click', function () {
        currentPage = page;
        displayList(coursesAll, perPage, currentPage);
        let t = document.querySelectorAll('.page-nav-item.active');
        for (let i = 0; i < t.length; i++) {
            t[i].classList.remove('active');
        }
        node.classList.add('active');
    });
    return node;
}

// Hien thi danh sach san pham 
async function showcoursesArr(courses) {
    // Lay danh sach danh muc tu API
    let categoriesMap = {};
    try {
        const response = await fetch(apiUrl('/api/categories'));
        if (response.ok) {
            const categories = await response.json();
            // Tao anh xa CategoryID -> CategoryName
            categories.forEach(category => {
                categoriesMap[getCategoryId(category)] = getCategoryName(category);
            });
        } else {
            console.error('Loi khi lay danh muc:', await response.text());
        }
    } catch (error) {
        console.error('Loi khi goi API:', error);
    }

    // Hien thi danh sach khoa hoc
    let courseHtml = "";
    if (courses.length === 0) {
        courseHtml = `<div class="no-result"><div class="no-result-i"><i class="fa-solid fa-face-sad-cry"></i></div><div class="no-result-h">Khong co khoa hoc de hien thi</div></div>`;
    } else {
        courses.forEach(course => {
            const courseId = getCourseId(course);
            const price = Number(getCoursePrice(course) || 0).toLocaleString();
            const image = resolveCourseImageUrl(course);
            const name = getCourseName(course);
            const description = getCourseDescription(course);
            const categoryName = categoriesMap[getCourseCategoryId(course)] || "Khong co danh muc";

            courseHtml += `
            <div class="list">
                <div class="list-left">
                    <img src="${image}" alt="">
                    <div class="list-info">
                        <h4>${name}</h4>
                        <p class="list-note">${description}</p>
                        <span class="list-category">${categoryName}</span>
                    </div>
                </div>
                <div class="list-right">
                    <div class="list-price">
                        <span class="list-current-price">${price} VND</span>
                    </div>
                    <div class="list-control">
                        <div class="list-tool">
                            <button class="btn-edit" onclick="editcategorie(${courseId})"><i class="fa-solid fa-pen-to-square"></i></button>
                            <button class="btn-delete" onclick="deletecategorie(${courseId})"><i class="fa-solid fa-trash"></i></button>
                        </div>
                    </div>
                </div>
            </div>`;
        });
    }
    document.getElementById("show-categorie").innerHTML = courseHtml;
}

async function showcourses() {
    try {
        const response = await fetch(apiUrl('/api/courses'));
        if (response.ok) {

            const courses = await response.json();
            showcoursesArr(courses); // Goi ham de hien thi danh sach khoa hoc
        } else {
            console.error('Loi khi lay danh sach khoa hoc:', await response.text());
        }
    } catch (error) {
        console.error('Loi khi goi API:', error);
    }
}

async function showcategorie() {
    try {
        const response = await fetch(apiUrl('/api/categories'));
        if (response.ok) {
            const categories = await response.json();
            let options = '<option value="">Tat ca</option>';
            categories.forEach(category => {
                options += `<option value="${getCategoryId(category)}">${getCategoryName(category)}</option>`;
            });
            const courseSelect = document.getElementById("chon-mon-select");
            const statisticSelect = document.getElementById("chon-mon-thong-ke");
            if (courseSelect) {
                courseSelect.innerHTML = options;
            }
            if (statisticSelect) {
                statisticSelect.innerHTML = options;
            }
        } else {
            console.error('Loi khi lay danh muc:', await response.text());
        }
    } catch (error) {
        console.error('Loi khi goi API:', error);
    }
}

async function showcategorieaddedit() {
    const selectedValue = document.getElementById("chon-mon-them-moi").value; // Luu gia tri truoc khi lam moi
    try {
        const response = await fetch(apiUrl('/api/categories'));
        if (response.ok) {
            const categories = await response.json();
            let options = '<option value="">Chon danh muc</option>';
            categories.forEach(category => {
                options += `<option value="${getCategoryId(category)}">${getCategoryName(category)}</option>`;
            });
            document.getElementById("chon-mon-them-moi").innerHTML = options;
            document.getElementById("chon-mon-them-moi").value = selectedValue; // Gan lai gia tri da chon
        }
    } catch (error) {
        console.error('Loi khi goi API:', error);
    }
}



async function filterCoursesByCategory() {
    const selectedCategoryID = document.getElementById("chon-mon-select").value; // Lay CategoryID tu dropdown
    const searchKeyword = document.getElementById("chon-mon-input").value.toLowerCase(); // Lay tu khoa tim kiem tu input

    try {
        const endpoint = selectedCategoryID
            ? `/api/courses/category/${encodeURIComponent(selectedCategoryID)}`
            : '/api/courses';

        const response = await fetch(apiUrl(endpoint));
        if (response.ok) {
            const courses = await response.json();
            let filteredCourses = Array.isArray(courses) ? courses : [];

            // Loc danh sach khoa hoc dua tren tu khoa tim kiem
            if (searchKeyword) {
                filteredCourses = filteredCourses.filter(course =>
                    getCourseName(course).toLowerCase().includes(searchKeyword)
                );
            }

            // Hien thi danh sach khoa hoc da loc
            showcoursesArr(filteredCourses);
        } else {
            console.error('Loi khi lay danh sach khoa hoc:', await response.text());
        }
    } catch (error) {
        console.error('Loi khi goi API:', error);
    }
}


async function cancelSearchcategorie() {
    try {
        const response = await fetch(apiUrl('/api/categories'));
        if (response.ok) {
            const categories = await response.json();
            showcategorie();
        } else {
            console.error('Loi khi lam moi danh muc:', await response.text());
        }
    } catch (error) {
        console.error('Loi khi goi API:', error);
    }
}

async function cancelSearchCourses() {
    try {
        // Dat gia tr9 cua dropdown ve mac 9nh (Tat ca)
        document.getElementById("chon-mon-select").value = "";

        // Xoa tu khoa tim kiem trong o input
        document.getElementById("chon-mon-input").value = "";

        const response = await fetch(apiUrl('/api/courses'));
        if (response.ok) {
            const courses = await response.json();
            showcoursesArr(courses); // Lam moi danh sach khoa hoc
        } else {
            console.error('Loi khi lam moi khoa hoc:', await response.text());
        }
    } catch (error) {
        console.error('Loi khi goi API:', error);
    }
}

function createId(arr) {
    let id = arr.length;
    let check = arr.find((item) => item.id == id);
    while (check != null) {
        id++;
        check = arr.find((item) => item.id == id);
    }
    return id;
}

let pendingLessons = [];
let existingLessons = [];

function normalizeLessonTitle(title) {
    return String(title || '').trim().toLowerCase();
}

function formatLessonDuration(seconds) {
    const total = Number(seconds || 0);
    const mins = Math.floor(total / 60);
    const secs = total % 60;
    return `${mins}:${secs < 10 ? '0' : ''}${secs}`;
}

function getVideoFileName(videoURL) {
    const rawValue = String(videoURL || '').trim();
    if (!rawValue) return '';
    return rawValue.split(/[\\/]/).pop();
}

function getLessonId(lesson) {
    return lesson?.id ?? lesson?.LessonID ?? null;
}

function setSelectedVideoNameDisplay(fileName) {
    const videoNameLabel = document.getElementById('lesson-video-name');
    if (!videoNameLabel) return;
    videoNameLabel.textContent = fileName ? `Da chon: ${fileName}` : 'Chua chon tep video.';
}

function renderLessonList() {
    const lessonList = document.getElementById('lesson-list');
    if (!lessonList) return;

    const rows = [];
    existingLessons.forEach((lesson, idx) => {
        const lessonId = getLessonId(lesson);
        rows.push(`
            <tr>
                <td>${idx + 1}</td>
                <td>${lesson.lessonTitle || ''}</td>
                <td>${getVideoFileName(lesson.videoURL)}</td>
                <td>${formatLessonDuration(lesson.duration)}</td>
                <td>Da luu</td>
                <td>
                    <button type="button" class="btn-delete-lesson" data-lesson-type="existing" data-lesson-id="${lessonId}">
                        Xoa
                    </button>
                </td>
            </tr>
        `);
    });

    pendingLessons.forEach((lesson, idx) => {
        rows.push(`
            <tr>
                <td>${existingLessons.length + idx + 1}</td>
                <td>${lesson.lessonTitle}</td>
                <td>${getVideoFileName(lesson.videoURL)}</td>
                <td>${formatLessonDuration(lesson.duration)}</td>
                <td>Moi</td>
                <td>
                    <button type="button" class="btn-delete-lesson" data-lesson-type="pending" data-lesson-index="${idx}">
                        Xoa
                    </button>
                </td>
            </tr>
        `);
    });

    if (rows.length === 0) {
        lessonList.innerHTML = '<div>Chua co bai hoc nao.</div>';
        return;
    }

    lessonList.innerHTML = `
        <table width="100%">
            <thead>
                <tr>
                    <td>STT</td>
                    <td>Ten bai hoc</td>
                    <td>Tep video</td>
                    <td>Thoi luong</td>
                    <td>Trang thai</td>
                    <td>Thao tac</td>
                </tr>
            </thead>
            <tbody>
                ${rows.join('')}
            </tbody>
        </table>
    `;
}

function resetLessonInputFields() {
    const titleInput = document.getElementById('lesson-title');
    const fileInput = document.getElementById('lesson-video-file');
    const durationInput = document.getElementById('lesson-duration');
    if (titleInput) titleInput.value = '';
    if (fileInput) fileInput.value = '';
    if (durationInput) durationInput.value = '';
    setSelectedVideoNameDisplay('');
}

function lessonTitleExists(title) {
    const normalized = normalizeLessonTitle(title);
    return existingLessons.some(lesson => normalizeLessonTitle(lesson.lessonTitle) === normalized)
        || pendingLessons.some(lesson => normalizeLessonTitle(lesson.lessonTitle) === normalized);
}

function getVideoDurationFromFile(file) {
    return new Promise((resolve, reject) => {
        const video = document.createElement('video');
        video.preload = 'metadata';
        const objectUrl = URL.createObjectURL(file);
        video.src = objectUrl;
        video.onloadedmetadata = () => {
            const duration = Math.floor(video.duration || 0);
            URL.revokeObjectURL(objectUrl);
            resolve(duration);
        };
        video.onerror = () => {
            URL.revokeObjectURL(objectUrl);
            reject(new Error('Khong doc duoc thoi luong video.'));
        };
    });
}

async function loadLessonsForCourse(courseId) {
    existingLessons = [];
    if (!courseId) {
        renderLessonList();
        return;
    }

    try {
        const response = await fetch(apiUrl(`/api/lessons/course/${courseId}/ordered`));
        if (!response.ok) {
            console.error('Loi khi lay danh sach bai hoc:', await response.text());
            renderLessonList();
            return;
        }
        const lessons = await response.json();
        existingLessons = Array.isArray(lessons) ? lessons : [];
    } catch (error) {
        console.error('Loi khi goi API:', error);
    }
    renderLessonList();
}

async function savePendingLessons(courseId) {
    if (!courseId || pendingLessons.length === 0) return;
    let orderIndex = existingLessons.length + 1;

    for (const lesson of pendingLessons) {
        const payload = {
            course: { id: Number(courseId) },
            lessonTitle: lesson.lessonTitle,
            videoURL: lesson.videoURL,
            duration: lesson.duration,
            orderIndex,
        };

        const response = await fetch(apiUrl('/api/lessons'), {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload),
        });

        if (!response.ok) {
            const text = await response.text();
            throw new Error(`Khong the them bai hoc \"${lesson.lessonTitle}\": ${text}`);
        }

        orderIndex += 1;
    }
}

async function handleDeleteLessonFromList(buttonEl) {
    if (!buttonEl) return;
    const lessonType = buttonEl.dataset.lessonType;

    if (lessonType === 'pending') {
        const pendingIndex = Number(buttonEl.dataset.lessonIndex);
        if (!Number.isInteger(pendingIndex) || pendingIndex < 0 || pendingIndex >= pendingLessons.length) {
            return;
        }

        pendingLessons.splice(pendingIndex, 1);
        renderLessonList();
        return;
    }

    const lessonId = Number(buttonEl.dataset.lessonId);
    if (!lessonId) {
        alert('Khong tim thay ID bai hoc de xoa.');
        return;
    }

    if (!confirm('Ban co chac muon xoa bai hoc nay?')) {
        return;
    }

    try {
        const response = await fetch(apiUrl(`/api/lessons/${lessonId}`), {
            method: 'DELETE',
        });

        if (!response.ok) {
            const text = await response.text();
            throw new Error(text || 'Khong the xoa bai hoc.');
        }

        existingLessons = existingLessons.filter(lesson => Number(getLessonId(lesson)) !== lessonId);
        renderLessonList();
        toast({ title: 'Success', message: 'Xoa bai hoc thanh cong!', type: 'success', duration: 3000 });
    } catch (error) {
        console.error(error);
        alert('Xoa bai hoc that bai. Vui long thu lai.');
    }
}

async function handleAddLessonRow() {
    const titleInput = document.getElementById('lesson-title');
    const fileInput = document.getElementById('lesson-video-file');
    const durationInput = document.getElementById('lesson-duration');
    if (!titleInput || !fileInput || !durationInput) return;

    const lessonTitle = titleInput.value.trim();
    if (!lessonTitle) {
        alert('Vui long nhap ten bai hoc.');
        return;
    }

    if (lessonTitleExists(lessonTitle)) {
        alert('Ten bai hoc da ton tai trong khoa hoc nay.');
        return;
    }

    const file = fileInput.files && fileInput.files[0] ? fileInput.files[0] : null;
    const duration = Number(durationInput.value || 0);

    if (!file) {
        alert('Vui long chon tep video.');
        return;
    }

    if (!duration) {
        alert('Vui long chon tep video de he thong tinh thoi luong.');
        return;
    }

    pendingLessons.push({
        lessonTitle,
        videoURL: file.name,
        duration,
    });

    resetLessonInputFields();
    renderLessonList();
}

const lessonVideoFileInput = document.getElementById('lesson-video-file');
if (lessonVideoFileInput) {
    lessonVideoFileInput.addEventListener('change', async (e) => {
        const file = e.target.files && e.target.files[0] ? e.target.files[0] : null;
        const durationInput = document.getElementById('lesson-duration');
        if (!durationInput) return;
        durationInput.value = '';
        if (!file) {
            setSelectedVideoNameDisplay('');
            return;
        }

        setSelectedVideoNameDisplay(file.name);

        try {
            const duration = await getVideoDurationFromFile(file);
            durationInput.value = String(duration);
        } catch (error) {
            console.error(error);
            alert('Khong the doc thoi luong video, vui long thu tep khac.');
        }
    });
}

const addLessonRowButton = document.getElementById('btn-add-lesson-row');
if (addLessonRowButton) {
    addLessonRowButton.addEventListener('click', async (e) => {
        e.preventDefault();
        await handleAddLessonRow();
    });
}

const lessonListContainer = document.getElementById('lesson-list');
if (lessonListContainer) {
    lessonListContainer.addEventListener('click', async (e) => {
        const deleteButton = e.target.closest('.btn-delete-lesson');
        if (!deleteButton) return;
        e.preventDefault();
        await handleDeleteLessonFromList(deleteButton);
    });
}

// Xoa san pham 
async function deletecategorie(id) {
    console.log("ID khoa hoc can xoa:", id); // Kiem tra ID
    if (!id) {
        console.error("ID khong hop le.");
        return;
    }

    if (confirm("Ban co chac muon xoa?")) {
        try {
            const response = await fetch(apiUrl(`/api/courses/${id}`), {
                method: 'DELETE',
            });

            if (response.ok) {
                toast({ title: 'Success', message: 'Xoa khoa hoc thanh cong!', type: 'success', duration: 3000 });
                showcourses(); // Lam moi danh sach khoa hoc
            } else {
                console.error('Loi khi xoa khoa hoc:', await response.text());
            }
        } catch (error) {
            console.error('Loi khi goi API:', error);
        }
    }
}

async function editcategorie(id) {
    try {
        const response = await fetch(apiUrl(`/api/courses/${id}`));
        if (response.ok) {
            const course = await response.json();

            // Luu ID cua khoa hoc hien tai vao bien toan cuc
            indexCur = id;

            // Hien thi giao dien chinh sua
            document.querySelectorAll(".add-categorie-e").forEach(item => {
                item.style.display = "none";
            });
            document.querySelectorAll(".edit-categorie-e").forEach(item => {
                item.style.display = "block";
            });
            document.querySelector(".add-categorie").classList.add("open");

            // Gan gia tri vao form chinh sua
            document.querySelector(".upload-image-preview").src = getCourseImage(course);
            document.querySelector(".upload-image-preview").dataset.filePath = getCourseImage(course);
            document.getElementById("ten-khoa-hoc").value = getCourseName(course);
            document.getElementById("gia-moi").value = getCoursePrice(course) || "";
            document.getElementById("mo-ta").value = getCourseDescription(course);
            document.getElementById("chon-mon-them-moi").value = getCourseCategoryId(course) || "";
            pendingLessons = [];
            resetLessonInputFields();
            await loadLessonsForCourse(id);

        } else {
            console.error('Loi khi lay thong tin khoa hoc:', await response.text());
        }
    } catch (error) {
        console.error('Loi khi goi API:', error);
    }
}


var indexCur;

let btnUpdatecategorieIn = document.getElementById("update-categorie-button");
btnUpdatecategorieIn.addEventListener("click", async (e) => {
    e.preventDefault();

    if (!indexCur) {
        console.error("Khong tim thay ID cua khoa hoc can cap nhat.");
        return;
    }

    const fileInput = document.querySelector("#up-hinh-anh"); // Input file
    let filePath = document.querySelector(".upload-image-preview").dataset.filePath; // Duong dan hien tai

    if (fileInput.files.length > 0) {
        // Neu co file moi, tao duong dan moi
        const fileName = fileInput.files[0].name;
        filePath = `src/assets/img/products/${fileName}`;
    }

    const updatedCourse = {
        courseName: document.getElementById("ten-khoa-hoc").value,
        description: document.getElementById("mo-ta").value,
        category: { id: Number(document.getElementById("chon-mon-them-moi").value) },
        price: parseFloat(document.getElementById("gia-moi").value || 0),
        image: filePath,
    };

    console.log("Du lieu gui len backend:", updatedCourse);

    try {
        const response = await fetch(apiUrl(`/api/courses/${indexCur}`), {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(updatedCourse),
        });
        if (response.ok) {
            try {
                await savePendingLessons(indexCur);
            } catch (lessonError) {
                console.error(lessonError);
                alert('Cap nhat khoa hoc thanh cong nhung them bai hoc that bai. Vui long kiem tra lai.');
            }

            toast({ title: 'Success', message: 'Cap nhat khoa hoc thanh cong!', type: 'success', duration: 3000 });
            showcourses();

            // Dong modal chinh sua
            document.querySelector(".add-categorie").classList.remove("open");
            setDefaultValue();
        } else {
            console.error('Loi khi cap nhat khoa hoc:', await response.text());
        }
    } catch (error) {
        console.error('Loi khi goi API:', error);
    }
});

// Ham lay duong dan anh tu thuoc tinh src
function getPathImage(src) {
    if (!src) {
        console.error("Duong dan anh khong hop le.");
        return "";
    }
    return src;
}

async function addNewCourse() {
    const newCourse = {
        courseName: document.getElementById("ten-khoa-hoc").value,
        description: document.getElementById("mo-ta").value,
        category: { id: Number(document.getElementById("chon-mon-them-moi").value) },
        price: parseFloat(document.getElementById("gia-moi").value || 0),
        image: document.querySelector(".upload-image-preview").dataset.filePath || "",
    };

    console.log("Du lieu gui len backend:", newCourse);

    try {
        const response = await fetch(apiUrl('/api/courses'), {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(newCourse),
        });

        if (response.ok) {
            const createdCourse = await response.json();
            const createdCourseId = getCourseId(createdCourse);
            if (createdCourseId) {
                try {
                    await savePendingLessons(createdCourseId);
                } catch (lessonError) {
                    console.error(lessonError);
                    alert('Them khoa hoc thanh cong nhung them bai hoc that bai. Vui long kiem tra lai.');
                }
            }

            toast({ title: 'Success', message: 'Them khoa hoc thanh cong!', type: 'success', duration: 3000 });
            showcourses(); // Lam moi danh sach khoa hoc
            document.querySelector(".add-categorie").classList.remove("open"); // Dong modal them khoa hoc
            setDefaultValue(); // Dat lai gia tri mac dinh cho form
        } else {
            console.error('Loi khi them khoa hoc:', await response.text());
        }
    } catch (error) {
        console.error('Loi khi goi API:', error);
    }
}

let btnAddcategorieIn = document.getElementById("add-categorie-button");
btnAddcategorieIn.addEventListener("click", (e) => {
    e.preventDefault();
    addNewCourse(); // Goi ham them khoa hoc
});

document.querySelector(".modal-close.categorie-form").addEventListener("click", () => {
    setDefaultValue();
})

function setDefaultValue() {
    document.querySelector(".upload-image-preview").src = "../src/assets/img/blank-image.png";
    document.getElementById("ten-khoa-hoc").value = "";
    document.getElementById("gia-moi").value = "";
    document.getElementById("mo-ta").value = "";
    pendingLessons = [];
    existingLessons = [];
    resetLessonInputFields();
    renderLessonList();
}

// Open Popup Modal
let btnAddcategorie = document.getElementById("btn-add-categorie");
btnAddcategorie.addEventListener("click", () => {
    document.querySelectorAll(".add-categorie-e").forEach(item => {
        item.style.display = "block";
    })
    document.querySelectorAll(".edit-categorie-e").forEach(item => {
        item.style.display = "none";
    })
    indexCur = null;
    pendingLessons = [];
    existingLessons = [];
    resetLessonInputFields();
    renderLessonList();
    document.querySelector(".add-categorie").classList.add("open");
});

// Close Popup Modal
let closePopup = document.querySelectorAll(".modal-close");
let modalPopup = document.querySelectorAll(".modal");

for (let i = 0; i < closePopup.length; i++) {
    closePopup[i].onclick = () => {
        modalPopup[i].classList.remove("open");
    };
}

function uploadImage(el) {
    const file = el.files[0]; // Lay tep tu input
    if (file) {
        const fileName = file.name; // Lay ten file
        const filePath = `src/assets/img/products/${fileName}`; // Tao duong dan file

        // Hien thi hinh anh duoc chon
        const reader = new FileReader();
        reader.onload = function (e) {
            document.querySelector(".upload-image-preview").setAttribute("src", e.target.result);
        };
        reader.readAsDataURL(file); // Doc tep duoi dang URL

        // Luu duong dan file vao mot input an hoac bien toan cuc de gui len backend
        document.querySelector(".upload-image-preview").dataset.filePath = filePath;
    } else {
        console.error("Khong co tep nao duoc chon.");
    }
}

// User
let addAccount = document.getElementById('signup-button');
let updateAccount = document.getElementById("btn-update-account")

document.querySelector(".modal.signup .modal-close").addEventListener("click", () => {
    signUpFormReset();
})

function openCreateAccount() {
    document.querySelector(".signup").classList.add("open");
    document.querySelectorAll(".edit-account-e").forEach(item => {
        item.style.display = "none"
    })
}

function signUpFormReset() {
    document.getElementById('fullname').value = "";
    document.getElementById('email').value = "";
    document.querySelector('.form-message-name').innerHTML = '';
    document.querySelector('.form-message-email').innerHTML = '';
}

function showUserArr(users) {
    let accountHtml = '';
    if (users.length === 0) {
        accountHtml = `<td colspan="6">Khong co du lieu</td>`;
    } else {
        users.forEach((account, index) => {
            const formattedDate = getUserCreateTime(account) ? formatDate(getUserCreateTime(account)) : "Khong xac dinh";
            const status = toUserStatusLabel(getUserStatus(account));
            accountHtml += `
                <tr data-id="${getUserId(account)}">
                    <td>${index + 1}</td>
                    <td>${getUserName(account)}</td>
                    <td>${getUserEmail(account)}</td>
                    <td>${formattedDate}</td>
                    <td>${status}</td>
                    <td>
                        <button class="btn-edit" onclick="editAccount(${getUserId(account)})"><i class="fa-solid fa-pen-to-square"></i></button>
                        <button class="btn-delete" onclick="deleteAccount(${getUserId(account)})"><i class="fa-solid fa-trash"></i></button>
                    </td>
                </tr>
            `;
        });
    }
    document.getElementById('show-user').innerHTML = accountHtml;
}

async function showUser() {
    try {
        const usersResponse = await fetch(apiUrl('/api/users'));

        if (usersResponse.ok) {
            const users = await usersResponse.json();

            // Tranh phu thuoc /api/roles dang tra payload de quy qua sau.
            let nonAdminUsers = users.filter(user => !['admin'].includes(String(getUserName(user)).toLowerCase()));

            const keyword = String(document.getElementById("form-search-user")?.value || "").trim().toLowerCase();
            if (keyword) {
                nonAdminUsers = nonAdminUsers.filter(user =>
                    String(getUserName(user)).toLowerCase().includes(keyword) ||
                    String(getUserEmail(user)).toLowerCase().includes(keyword)
                );
            }

            // Loc theo trang thai (Hoat dong hoac Bi khoa)
            const selectedStatus = parseInt(document.getElementById("tinh-trang-user").value); // Lay gia tri tu dropdown
            if (selectedStatus !== 2) { // 2 = Tat ca
                nonAdminUsers = nonAdminUsers.filter(user =>
                    isActiveUserStatus(getUserStatus(user)) === (selectedStatus === 1)
                );
            }

            // Loc theo khoang thoi gian
            const startDate = document.getElementById("time-start-user").value;
            const endDate = document.getElementById("time-end-user").value;

            if (startDate && endDate && startDate > endDate) {
                alert("Lua chon thoi gian sai!");
                return;
            }

            const startDateAtZero = startDate ? new Date(`${startDate}T00:00:00`) : null;
            const endDateAtLastSecond = endDate ? new Date(`${endDate}T23:59:59`) : null;

            if (startDateAtZero) {
                nonAdminUsers = nonAdminUsers.filter(user =>
                    getUserCreateTime(user) && toDateValue(getUserCreateTime(user)) && toDateValue(getUserCreateTime(user)) >= startDateAtZero
                );
            }

            if (endDateAtLastSecond) {
                nonAdminUsers = nonAdminUsers.filter(user =>
                    getUserCreateTime(user) && toDateValue(getUserCreateTime(user)) && toDateValue(getUserCreateTime(user)) <= endDateAtLastSecond
                );
            }

            // Hien thi danh sach nguoi dung da loc
            showUserArr(nonAdminUsers);
        } else {
            console.error('Loi khi goi API:', await usersResponse.text());
        }
    } catch (error) {
        console.error('Loi khi goi API:', error);
    }
}

async function cancelSearchUser() {
    try {
        const usersResponse = await fetch(apiUrl('/api/users'));

        if (usersResponse.ok) {
            const users = await usersResponse.json();

            const nonAdminUsers = users.filter(user => !['admin'].includes(String(getUserName(user)).toLowerCase()));

            // Hien thi danh sach nguoi dung khong phai Admin
            showUserArr(nonAdminUsers);

            // Dat lai cac gia tri cua bo loc
            document.getElementById("tinh-trang-user").value = 2;
            document.getElementById("form-search-user").value = "";
            document.getElementById("time-start-user").value = "";
            document.getElementById("time-end-user").value = "";
        } else {
            console.error('Loi khi lam moi danh sach nguoi dung:', await usersResponse.text());
        }
    } catch (error) {
        console.error('Loi khi goi API:', error);
    }
}

async function deleteAccount(id) {
    if (confirm("Ban co chac muon xoa?")) {
        try {
            const response = await fetch(apiUrl(`/api/users/${id}`), {
                method: 'DELETE',
            });
            if (response.ok) {
                toast({ title: 'Success', message: 'Xoa nguoi dung thanh cong!', type: 'success', duration: 3000 });
                showUser();
            } else {
                console.error('Loi khi xoa nguoi dung:', await response.text());
            }
        } catch (error) {
            console.error('Loi khi goi API:', error);
        }
    }
}

async function editAccount(id) {
    console.log("ID cua tai khoan:", id);
    try {
        const response = await fetch(apiUrl(`/api/users/${id}`));
        if (response.ok) {
            const account = await response.json();

            // Luu ID cua tai khoan hien tai vao bien toan cuc
            indexFlag = id;

            // Hien thi giao dien chinh sua
            document.querySelector(".signup").classList.add("open");
            document.querySelectorAll(".add-account-e").forEach(item => item.style.display = "none");
            document.querySelectorAll(".edit-account-e").forEach(item => item.style.display = "block");

            // Gan gia tri vao form chinh sua
            document.getElementById("fullname").value = account.UserName || "";
            document.getElementById("fullname").value = getUserName(account);
            document.getElementById("email").value = getUserEmail(account);

            // Kiem tra trang thai va gan gia tri cho checkbox
            document.getElementById("user-status").checked = isActiveUserStatus(getUserStatus(account));

        } else {
            console.error('Loi khi lay thong tin tai khoan:', await response.text());
        }
    } catch (error) {
        console.error('Loi khi goi API:', error);
    }
}

updateAccount.addEventListener("click", async (e) => {
    e.preventDefault();

    if (!indexFlag) {
        console.error("Khong tim thay ID cua nguoi dung can cap nhat.");
        return;
    }

    const updatedAccount = {
        username: document.getElementById("fullname").value,
        email: document.getElementById("email").value,
        status: document.getElementById("user-status").checked ? 'Hoat dong' : 'Bi khoa',
    };

    try {
        const response = await fetch(apiUrl(`/api/users/${indexFlag}`), {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(updatedAccount),
        });
        if (response.ok) {
            toast({ title: 'Success', message: 'Cap nhat thong tin thanh cong!', type: 'success', duration: 3000 });
            document.querySelector(".signup").classList.remove("open");
            signUpFormReset();
            showUser(); // Lam moi danh sach nguoi dung
        } else {
            console.error('Loi khi cap nhat thong tin nguoi dung:', await response.text());
        }
    } catch (error) {
        console.error('Loi khi goi API:', error);
    }
});

async function searchUser() {
    const status = parseInt(document.getElementById("tinh-trang-user").value); // Lay tinh trang
    const keyword = document.getElementById("form-search-user").value.toLowerCase(); // Lay tu khoa tim kiem
    const timeStart = document.getElementById("time-start-user").value; // Lay thoi gian bat dau
    const timeEnd = document.getElementById("time-end-user").value; // Lay thoi gian ket thuc

    if (timeEnd < timeStart && timeEnd !== "" && timeStart !== "") {
        alert("Lua chon thoi gian sai!");
        return;
    }

    try {
        const response = await fetch(apiUrl('/api/users'));
        if (response.ok) {
            const accounts = await response.json();

            // Loc danh sach nguoi dung theo tinh trang
            let filteredAccounts = status !== 2
                ? accounts.filter(account => isActiveUserStatus(getUserStatus(account)) === (status === 1))
                : accounts;

            // Loc danh sach nguoi dung theo tu khoa
            if (keyword) {
                filteredAccounts = filteredAccounts.filter(account =>
                    getUserName(account).toLowerCase().includes(keyword) ||
                    getUserEmail(account).toLowerCase().includes(keyword)
                );
            }

            // Loc danh sach nguoi dung theo thoi gian
            if (timeStart) {
                filteredAccounts = filteredAccounts.filter(account =>
                    getUserCreateTime(account) && new Date(getUserCreateTime(account)) >= new Date(timeStart)
                );
            }
            if (timeEnd) {
                filteredAccounts = filteredAccounts.filter(account =>
                    getUserCreateTime(account) && new Date(getUserCreateTime(account)) <= new Date(timeEnd)
                );
            }

            // Hien thi danh sach nguoi dung da loc
            showUserArr(filteredAccounts);
        } else {
            console.error('Loi khi tim kiem nguoi dung:', await response.text());
        }
    } catch (error) {
        console.error('Loi khi goi API:', error);
    }
}
document.getElementById("form-search-user").addEventListener("input", (e) => {
    showUser(); // Goi ham hien thi khi nhap tu khoa
});

document.getElementById("time-start-user").addEventListener("change", (e) => {
    showUser(); // Goi ham hien thi khi thay doi thoi gian bat dau
});

document.getElementById("time-end-user").addEventListener("change", (e) => {
    showUser(); // Goi ham hien thi khi thay doi thoi gian ket thuc
});

document.getElementById("tinh-trang-user").addEventListener("change", (e) => {
    showUser(); // Goi ham hien thi khi thay doi tinh trang
});

document.querySelector(".btn-reset-order").addEventListener("click", (e) => {
    e.preventDefault();
    cancelSearchUser();
});

document.getElementById("logout-acc").addEventListener('click', (e) => {
    e.preventDefault();
    localStorage.removeItem("currentuser");
    localStorage.removeItem("loggedInUser");
    localStorage.removeItem("authToken");
    localStorage.removeItem("authTokenType");
    localStorage.removeItem("authTokenExpiresAt");
    localStorage.removeItem("authHeader");
    window.location = "/";
})



// Doi trang thai don hang
async function changeStatus(id, el) {
    try {
        const currentStatus = String(el?.dataset?.status || "").toUpperCase();
        const newStatus = currentStatus === "SUCCESS" ? "PENDING" : "SUCCESS";

        const currentOrderResponse = await fetch(apiUrl(`/api/orders/${id}`), {
            headers: withAuthHeaders(),
        });
        if (!currentOrderResponse.ok) {
            console.error('Loi khi lay don hang:', await currentOrderResponse.text());
            return;
        }

        const currentOrder = await currentOrderResponse.json();
        const response = await fetch(apiUrl(`/api/orders/${id}`), {
            method: 'PUT',
            headers: withAuthHeaders({ 'Content-Type': 'application/json' }),
            body: JSON.stringify({
                ...currentOrder,
                id: getOrderId(currentOrder),
                status: newStatus,
            }),
        });

        if (response.ok) {
            el.classList.remove("btn-chuaxuly", "btn-daxuly", "btn-thatbai");
            if (newStatus === "SUCCESS") {
                el.classList.add("btn-daxuly");
                el.innerHTML = "Da xu ly";
            } else {
                el.classList.add("btn-chuaxuly");
                el.innerHTML = "Chua xu ly";
            }
            el.dataset.status = newStatus;
            toast({ title: 'Success', message: 'Cap nhat trang thai thanh cong!', type: 'success', duration: 3000 });
            showOrder();
        } else {
            console.error('Loi khi cap nhat trang thai:', await response.text());
        }
    } catch (error) {
        console.error('Loi khi goi API:', error);
    }
}

// Format Date
function formatDate(date) {
    if (!date) return "Khong xac dinh"; // Tra ve gia tri mac dinh neu khong co ngay

    const formattedDate = date.replace(" ", "T"); // Chuyen doi dinh dang
    const fm = new Date(formattedDate);
    if (isNaN(fm)) return "Khong xac dinh"; // Tra ve gia tri mac dinh neu ngay khong hop le

    const yyyy = fm.getFullYear();
    const mm = fm.getMonth() + 1;
    const dd = fm.getDate();
    return `${dd < 10 ? "0" + dd : dd}/${mm < 10 ? "0" + mm : mm}/${yyyy}`;
}

// Show order
async function showOrder(existingOrders = null) {
    let orderHtml = "";

    try {
        let orders = existingOrders;
        if (!Array.isArray(orders)) {
            orders = await fetchOrdersByStatusFilter('ALL');
        }

            console.log("Danh sach don hang tu API:", orders); // Kiem tra du lieu tra ve tu API

            if (orders.length === 0) {
                orderHtml = `<td colspan="6">Khong co du lieu</td>`;
            } else {
                orders.forEach((item) => {
                    const statusEnum = getOrderStatus(item);
                    let status = getOrderStatusBadge(statusEnum);
                    let date = formatDate(getOrderDate(item));
                    let userName = getOrderUserName(item);
                    const orderId = getOrderId(item);

                    orderHtml += `
                    <tr>
                        <td>${orderId}</td>
                        <td>${userName}</td>
                        <td>${date}</td>
                        <td>${vnd(getOrderTotal(item))}</td>
                        <td>${status}</td>
                        <td class="control">
                            <button class="btn-detail" onclick="detailOrder('${orderId}')"><i class="fa-solid fa-eye"></i> Chi tiet</button>
                        </td>
                    </tr>`;
                });
            }
    } catch (error) {
        console.error('Loi khi goi API:', error);
        orderHtml = `<td colspan="6">Khong co du lieu</td>`;
    }

    document.getElementById("showOrder").innerHTML = orderHtml;
}

// Get Order Details
function getOrderDetails(madon) {
    let orderDetails = localStorage.getItem("orderDetails") ?
        JSON.parse(localStorage.getItem("orderDetails")) : [];
    let ctDon = [];
    orderDetails.forEach((item) => {
        if (item.madon == madon) {
            ctDon.push(item);
        }
    });
    return ctDon;
}

// Show Order Detail
async function detailOrder(id) {
    try {
        const orderResponse = await fetch(apiUrl(`/api/orders/${id}`), {
            headers: withAuthHeaders(),
        });
        const detailsResponse = await fetch(apiUrl(`/api/order-details/order/${id}`));

        if (orderResponse.ok && detailsResponse.ok) {
            const order = await orderResponse.json();
            let details = await detailsResponse.json();

            console.log("Order data:", order);
            console.log("Order details:", details);

            // Kiem tra neu `details` khong phai la mang, chuyen doi thanh mang
            if (!Array.isArray(details)) {
                console.warn("Details is not an array, converting to array:", details);
                details = [details]; // Chuyen doi doi tuong thanh mang chua mot phan tu
            }

            // Lay thong tin khoa hoc dua tren CourseID
            const coursePromises = details.map(async (item) => {
                const detailCourseId = item?.course?.id ?? item?.CourseID ?? item?.courseId;
                const courseResponse = await fetch(apiUrl(`/api/courses/${detailCourseId}`));
                if (courseResponse.ok) {
                    const course = await courseResponse.json();
                    console.log("Thong tin khoa hoc:", course); // Kiem tra du lieu tra ve
                    return course;
                } else {
                    console.error(`Loi khi lay thong tin khoa hoc voi CourseID: ${detailCourseId}`);
                    return null;
                }
            });

            const courses = await Promise.all(coursePromises);

            document.querySelector(".modal.detail-order").classList.add("open");
            // Hien thi danh sach san pham trong don hang
            let spHtml = `<div class="modal-detail-left"><div class="order-item-group">`;
            details.forEach((item, index) => {
                const course = courses[index];
                console.log("Chi tiet don hang:", item);
                console.log("Gia tien:", item.Price); // Lay thong tin khoa hoc tuong ung
                if (course) {
                    spHtml += `
                    <div class="order-product">
                        <div class="order-product-left">
                            <img src="${getCourseImage(course)}" alt="">
                            <div class="order-product-info">
                                <h4>${getCourseName(course)}</h4>
                                <p class="order-product-note"><i class="fa-solid fa-pen"></i> ${course.Note || "Khong co ghi chu"}</p>
                            </div>
                        </div>
                        <div class="order-product-right">
                            <div class="order-product-price">
                                <span class="order-product-current-price">${vnd(getCoursePrice(course))}</span>
                            </div>
                        </div>
                    </div>`;
                } else {
                    const detailCourseId = item?.course?.id ?? item?.CourseID ?? item?.courseId;
                    spHtml += `
                    <div class="order-product">
                        <p>Khong the lay thong tin khoa hoc voi CourseID: ${detailCourseId}</p>
                    </div>`;
                }
            });

            spHtml += `</div></div>`;
            spHtml += `
            <div class="modal-detail-right">
                <ul class="detail-order-group">
                    <li class="detail-order-item">
                        <span class="detail-order-item-left"><i class="fa-solid fa-calendar-days"></i> Ngay dat hang</span>
                        <span class="detail-order-item-right">${formatDate(getOrderDate(order))}</span>
                    </li>
                    <li class="detail-order-item">
                        <span class="detail-order-item-left"><i class="fa-solid fa-person"></i> Nguoi nhan</span>
                        <span class="detail-order-item-right">${getOrderUserName(order)}</span>
                    </li>
                </ul>
            </div>`;
            document.querySelector(".modal-detail-order").innerHTML = spHtml;

            const orderStatus = getOrderStatus(order);
            let classDetailBtn = orderStatus === "SUCCESS"
                ? "btn-daxuly"
                : (orderStatus === "FAILED" ? "btn-thatbai" : "btn-chuaxuly");
            let textDetailBtn = orderStatus === "SUCCESS"
                ? "Da xu ly"
                : (orderStatus === "FAILED" ? "That bai" : "Chua xu ly");
            document.querySelector(".modal-detail-bottom").innerHTML = `
            <div class="modal-detail-bottom-left">
                <div class="price-total">
                    <span class="thanhtien">Thanh tien</span>
                    <span class="price">${vnd(getOrderTotal(order))}</span>
                </div>
            </div>
            <div class="modal-detail-bottom-right">
                <button class="modal-detail-btn ${classDetailBtn}" data-status="${orderStatus}" onclick="changeStatus('${getOrderId(order)}', this)">${textDetailBtn}</button>
            </div>`;

            // Hien thi modal chi tiet don hang
            document.querySelector(".modal.detail-order").classList.add("open");
        } else {
            console.error('Loi khi lay chi tiet don hang:', await orderResponse.text(), await detailsResponse.text());
        }
    } catch (error) {
        console.error('Loi khi goi API:', error);
    }
}

// Find Order
async function findOrder() {
    const status = normalizeOrderStatusFilterValue(document.getElementById("tinh-trang").value);
    const keyword = String(document.getElementById("form-search-order").value || "").trim().toLowerCase();
    const startDate = document.getElementById("time-start").value;
    const endDate = document.getElementById("time-end").value;

    if (startDate && endDate && startDate > endDate) {
        alert("Lua chon thoi gian sai!");
        return;
    }

    try {
        const orders = await fetchOrdersByStatusFilter(status);
        const startDateAtZero = startDate ? new Date(`${startDate}T00:00:00`) : null;
        const endDateAtLastSecond = endDate ? new Date(`${endDate}T23:59:59`) : null;

        const filteredOrders = (Array.isArray(orders) ? orders : []).filter(order => {
            const orderStatus = getOrderStatus(order);
            if (status !== 'ALL' && orderStatus !== status) return false;

            if (keyword) {
                const matchKeyword =
                    String(getOrderId(order) ?? '').toLowerCase().includes(keyword) ||
                    String(getOrderUserName(order) ?? '').toLowerCase().includes(keyword);
                if (!matchKeyword) return false;
            }

            if (startDateAtZero || endDateAtLastSecond) {
                const orderDate = toDateValue(getOrderDate(order));
                if (!orderDate) return false;
                if (startDateAtZero && orderDate < startDateAtZero) return false;
                if (endDateAtLastSecond && orderDate > endDateAtLastSecond) return false;
            }

            return true;
        });

        showOrder(filteredOrders);
    } catch (error) {
        console.error('Loi khi goi API:', error);
    }
}

async function cancelSearchOrder() {
    document.getElementById("tinh-trang").value = 'ALL';
    document.getElementById("form-search-order").value = "";
    document.getElementById("time-start").value = "";
    document.getElementById("time-end").value = "";
    showOrder();
}

function toDateValue(dateLike) {
    if (!dateLike) return null;
    const parsed = new Date(String(dateLike).replace(" ", "T"));
    return isNaN(parsed) ? null : parsed;
}

function renderStatisticsRows(rows) {
    const tableBody = document.getElementById("showTk");
    if (!tableBody) return;

    if (!rows.length) {
        tableBody.innerHTML = '<tr><td colspan="4">Khong co du lieu</td></tr>';
        return;
    }

    let html = "";
    rows.forEach((row, index) => {
        html += `
            <tr>
                <td>${index + 1}</td>
                <td>${row.courseName}</td>
                <td>${row.soldCount}</td>
                <td>${row.userCount}</td>
            </tr>`;
    });
    tableBody.innerHTML = html;
}

async function thongKe(sortMode = 0) {
    const categoryId = String(document.getElementById("chon-mon-thong-ke")?.value || "");
    const keyword = String(document.getElementById("form-search-tk")?.value || "").trim().toLowerCase();

    try {
        const [ordersResponse, coursesResponse] = await Promise.all([
            fetch(apiUrl('/api/orders'), { headers: withAuthHeaders() }),
            fetch(apiUrl('/api/courses'))
        ]);

        if (!ordersResponse.ok || !coursesResponse.ok) {
            console.error('Loi khi lay du lieu thong ke.');
            return;
        }

        const orders = await ordersResponse.json();
        const courses = await coursesResponse.json();
        const courseById = new Map((Array.isArray(courses) ? courses : []).map(course => [String(getCourseId(course)), course]));

        const successOrders = (Array.isArray(orders) ? orders : []).filter(order => {
            if (getOrderStatus(order) !== 'SUCCESS') return false;
            return true;
        });

        const detailsByOrder = await Promise.all(successOrders.map(async order => {
            const orderId = getOrderId(order);
            const detailResponse = await fetch(apiUrl(`/api/order-details/order/${orderId}`));
            if (!detailResponse.ok) return { order, details: [] };
            const details = await detailResponse.json();
            return { order, details: Array.isArray(details) ? details : [] };
        }));

        const statsByCourse = new Map();
        const allUsers = new Set();

        detailsByOrder.forEach(({ order, details }) => {
            const userId = getUserId(order?.user || order?.User) ?? order?.user?.id ?? null;

            details.forEach(detail => {
                const detailCourseId = detail?.course?.id ?? detail?.CourseID ?? detail?.courseId;
                if (detailCourseId == null) return;

                const key = String(detailCourseId);
                const course = courseById.get(key);
                if (!course) return;

                if (categoryId && String(getCourseCategoryId(course)) !== categoryId) return;
                if (keyword && !String(getCourseName(course)).toLowerCase().includes(keyword)) return;

                if (!statsByCourse.has(key)) {
                    statsByCourse.set(key, {
                        courseName: getCourseName(course),
                        soldCount: 0,
                        users: new Set()
                    });
                }

                const stat = statsByCourse.get(key);
                stat.soldCount += 1;
                if (userId != null) {
                    stat.users.add(String(userId));
                    allUsers.add(String(userId));
                }
            });
        });

        let rows = Array.from(statsByCourse.values()).map(stat => ({
            courseName: stat.courseName,
            soldCount: stat.soldCount,
            userCount: stat.users.size
        }));

        if (sortMode === 1) rows.sort((a, b) => a.soldCount - b.soldCount);
        if (sortMode === 2) rows.sort((a, b) => b.soldCount - a.soldCount);
        if (sortMode === 0) rows.sort((a, b) => a.courseName.localeCompare(b.courseName, 'vi'));

        const quantityCategorieEl = document.getElementById("quantity-categorie");
        const quantityOrderEl = document.getElementById("quantity-order");
        const quantityUserEl = document.getElementById("quantity-user");
        if (quantityCategorieEl) quantityCategorieEl.textContent = String(rows.length);
        if (quantityOrderEl) quantityOrderEl.textContent = String(rows.reduce((sum, row) => sum + row.soldCount, 0));
        if (quantityUserEl) quantityUserEl.textContent = String(allUsers.size);

        renderStatisticsRows(rows);
    } catch (error) {
        console.error('Loi khi thong ke:', error);
    }
}



window.onload = async function () {
    updateDashboard();
    showcourses();
    showcategorie();
    showcategorieaddedit();
    cancelSearchCourses();
    cancelSearchcategorie();
    showUser();
    showOrder();
    thongKe();
};




