package SpringBootBE.BackEnd.Service;

import SpringBootBE.BackEnd.config.MomoConfig;
import SpringBootBE.BackEnd.dto.MomoCallbackResponse;
import SpringBootBE.BackEnd.dto.MomoPaymentRequest;
import SpringBootBE.BackEnd.dto.MomoPaymentResponse;
import SpringBootBE.BackEnd.dto.MomoUtil;
import SpringBootBE.BackEnd.model.Course;
import SpringBootBE.BackEnd.model.Enrollment;
import SpringBootBE.BackEnd.model.Order;
import SpringBootBE.BackEnd.model.OrderDetail;
import SpringBootBE.BackEnd.model.User;
import SpringBootBE.BackEnd.repository.CourseRepository;
import SpringBootBE.BackEnd.repository.EnrollmentRepository;
import SpringBootBE.BackEnd.repository.OrderDetailRepository;
import SpringBootBE.BackEnd.repository.OrderRepository;
import SpringBootBE.BackEnd.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final MomoConfig momoConfig;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentService enrollmentService;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final long pendingTimeoutMinutes;

    public PaymentService(MomoConfig momoConfig,
                          OrderRepository orderRepository,
                          OrderDetailRepository orderDetailRepository,
                          UserRepository userRepository,
                          CourseRepository courseRepository,
                          EnrollmentRepository enrollmentRepository,
                          EnrollmentService enrollmentService,
                          @Value("${payment.pending-timeout-minutes:30}") long pendingTimeoutMinutes) {
        this.momoConfig = momoConfig;
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.enrollmentService = enrollmentService;
        this.pendingTimeoutMinutes = Math.max(1L, pendingTimeoutMinutes);
    }

    @Scheduled(fixedDelayString = "${payment.pending-order-check-delay-ms:60000}")
    public void expirePendingOrders() {
        LocalDateTime expireBefore = LocalDateTime.now().minusMinutes(pendingTimeoutMinutes);
        List<Order> expiredOrders = orderRepository.findByStatusAndOrderDateBefore(Order.OrderStatus.PENDING, expireBefore);

        if (expiredOrders.isEmpty()) {
            return;
        }

        for (Order order : expiredOrders) {
            if (order == null || order.getStatus() != Order.OrderStatus.PENDING) {
                continue;
            }

            String requestId = extractNoteValue(order.getNotes(), "requestId");
            markOrderFailed(order, requestId, null, 408, "Hết thời gian thanh toán.");
            logger.info("Auto-expired pending order id={} after {} minutes", order.getId(), pendingTimeoutMinutes);
        }
    }

    public synchronized MomoPaymentResponse createMomoPayment(MomoPaymentRequest request) {
        validateCreateRequest(request);

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng."));

        List<Course> courses = loadCourses(request.getCourseIds());
        ensureCoursesNotPurchasedYet(user.getId(), courses);

        long calculatedAmount = calculateAmount(courses);
        if (request.getAmount() != null && !Objects.equals(request.getAmount(), calculatedAmount)) {
            throw new IllegalArgumentException("Số tiền gửi lên không khớp tổng giá khóa học.");
        }

        String requestSeed = buildRequestId();
        String orderInfo = buildOrderInfo(request, courses);
        String extraData = MomoUtil.nullToEmpty(request.getExtraData());

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(BigDecimal.valueOf(calculatedAmount));
        order.setStatus(Order.OrderStatus.PENDING);
        order.setNotes("provider=MOMO;status=" + Order.OrderStatus.PENDING.name());
        orderRepository.save(order);

        String momoOrderId = buildMomoOrderId(order.getId(), requestSeed);
        String requestId = momoOrderId;
        order.setNotes(buildOrderNotes(momoOrderId, requestId, Order.OrderStatus.PENDING.name(), null, null));
        orderRepository.save(order);

        saveOrderDetails(order, courses);

        try {
            LinkedHashMap<String, String> signatureFields = new LinkedHashMap<>();
            signatureFields.put("accessKey", momoConfig.getAccessKey());
            signatureFields.put("amount", String.valueOf(calculatedAmount));
            signatureFields.put("extraData", extraData);
            signatureFields.put("ipnUrl", momoConfig.getIpnUrl());
            signatureFields.put("orderId", momoOrderId);
            signatureFields.put("orderInfo", orderInfo);
            signatureFields.put("partnerCode", momoConfig.getPartnerCode());
            signatureFields.put("redirectUrl", momoConfig.getRedirectUrl());
            signatureFields.put("requestId", requestId);
            signatureFields.put("requestType", momoConfig.getRequestType());

            String rawHash = MomoUtil.buildRawSignature(signatureFields);
            String signature = MomoUtil.hmacSHA256(rawHash, momoConfig.getSecretKey());

            LinkedHashMap<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("partnerCode", momoConfig.getPartnerCode());
            requestBody.put("partnerName", "BackEnd");
            requestBody.put("storeId", "BackEndStore");
            requestBody.put("requestId", requestId);
            requestBody.put("amount", calculatedAmount);
            requestBody.put("orderId", momoOrderId);
            requestBody.put("orderInfo", orderInfo);
            requestBody.put("redirectUrl", momoConfig.getRedirectUrl());
            requestBody.put("ipnUrl", momoConfig.getIpnUrl());
            requestBody.put("lang", momoConfig.getLang());
            requestBody.put("requestType", momoConfig.getRequestType());
            requestBody.put("autoCapture", true);
            requestBody.put("extraData", extraData);
            requestBody.put("signature", signature);

            String responseJson = sendCreatePaymentRequest(requestBody);
            Integer resultCode = MomoUtil.extractJsonInteger(responseJson, "resultCode");
            String message = defaultIfBlank(MomoUtil.extractJsonString(responseJson, "message"), "Không thể khởi tạo thanh toán MoMo.");
            String payUrl = MomoUtil.extractJsonString(responseJson, "payUrl");
            String deeplink = MomoUtil.extractJsonString(responseJson, "deeplink");
            String qrCodeUrl = MomoUtil.extractJsonString(responseJson, "qrCodeUrl");

            if (resultCode == null || resultCode != 0 || isBlank(payUrl) && isBlank(deeplink) && isBlank(qrCodeUrl)) {
                markOrderFailed(order, requestId, null, resultCode, message);
                throw new IllegalStateException(message);
            }

            MomoPaymentResponse response = new MomoPaymentResponse();
            response.setMessage(message);
            response.setOrderId(order.getId());
            response.setMomoOrderId(momoOrderId);
            response.setRequestId(requestId);
            response.setAmount(calculatedAmount);
            response.setPayUrl(payUrl);
            response.setDeeplink(deeplink);
            response.setQrCodeUrl(qrCodeUrl);
            response.setResultCode(resultCode);
            response.setStatus(order.getStatus().name());
            return response;
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (Exception exception) {
            markOrderFailed(order, requestId, null, -1, exception.getMessage());
            throw new IllegalStateException("Không thể tạo thanh toán MoMo: " + exception.getMessage(), exception);
        }
    }

    public MomoCallbackResponse handleMomoReturn(Map<String, String> callbackData) {
        return processCallback(callbackData);
    }

    public Map<String, Object> handleMomoIpn(Map<String, String> callbackData) {
        MomoCallbackResponse callbackResponse = processCallback(callbackData);
        LinkedHashMap<String, Object> response = new LinkedHashMap<>();
        response.put("partnerCode", momoConfig.getPartnerCode());
        response.put("orderId", callbackResponse.getOrderId());
        response.put("requestId", callbackResponse.getRequestId());
        response.put("resultCode", resolveIpnResultCode(callbackResponse));
        response.put("message", callbackResponse.getMessage());
        return response;
    }

    private MomoCallbackResponse processCallback(Map<String, String> callbackData) {
        Map<String, String> values = normalizeCallbackData(callbackData);

        MomoCallbackResponse response = new MomoCallbackResponse();
        response.setOrderId(values.get("orderId"));
        response.setRequestId(values.get("requestId"));
        response.setTransId(values.get("transId"));
        response.setResultCode(parseInteger(values.get("resultCode")));
        response.setValidSignature(isValidCallbackSignature(values));

        if (!response.isValidSignature()) {
            response.setMessage("Chữ ký callback MoMo không hợp lệ.");
            response.setStatus("INVALID_SIGNATURE");
            return response;
        }

        Order order = resolveOrderByMomoOrderId(values.get("orderId"));
        if (order == null) {
            response.setMessage("Không tìm thấy đơn hàng.");
            response.setStatus("NOT_FOUND");
            return response;
        }

        String callbackMismatch = validateCallbackAgainstOrder(order, values);
        if (callbackMismatch != null) {
            response.setMessage(callbackMismatch);
            response.setStatus("MISMATCHED_CALLBACK");
            return response;
        }

        if (order.getStatus() == Order.OrderStatus.SUCCESS) {
            response.setMessage("Đơn hàng đã được xác nhận trước đó.");
            response.setStatus(Order.OrderStatus.SUCCESS.name());
            return response;
        }

        Integer resultCode = parseInteger(values.get("resultCode"));
        if (resultCode != null && resultCode == 0) {
            markOrderSuccess(order, values.get("requestId"), values.get("transId"), values.get("message"));
            createEnrollmentsIfNeeded(order);
            response.setStatus(Order.OrderStatus.SUCCESS.name());
        } else {
            markOrderFailed(order, values.get("requestId"), values.get("transId"), resultCode, values.get("message"));
            response.setStatus(Order.OrderStatus.FAILED.name());
        }

        response.setMessage(defaultIfBlank(values.get("message"), resultCode != null && resultCode == 0 ? "Thanh toán thành công." : "Thanh toán thất bại."));
        return response;
    }

    private void validateCreateRequest(MomoPaymentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Yêu cầu thanh toán không hợp lệ.");
        }
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("Thiếu userId.");
        }
        if (request.getCourseIds() == null || request.getCourseIds().isEmpty()) {
            throw new IllegalArgumentException("Danh sách khóa học không được để trống.");
        }
    }

    private List<Course> loadCourses(List<Integer> requestedCourseIds) {
        List<Integer> uniqueIds = new ArrayList<>(new LinkedHashSet<>(requestedCourseIds));
        List<Course> loadedCourses = courseRepository.findAllById(uniqueIds);

        if (loadedCourses.size() != uniqueIds.size()) {
            throw new IllegalArgumentException("Có khóa học không tồn tại hoặc đã bị xóa.");
        }

        Map<Integer, Course> courseById = loadedCourses.stream()
                .collect(Collectors.toMap(Course::getId, course -> course));

        return uniqueIds.stream().map(courseById::get).toList();
    }

    private void ensureCoursesNotPurchasedYet(Integer userId, List<Course> courses) {
        List<Integer> duplicatedCourseIds = courses.stream()
                .map(Course::getId)
                .filter(courseId -> enrollmentRepository.findByUserIdAndCourseId(userId, courseId) != null)
                .toList();

        if (!duplicatedCourseIds.isEmpty()) {
            throw new IllegalArgumentException("Người dùng đã sở hữu khóa học: " + duplicatedCourseIds);
        }
    }

    private long calculateAmount(List<Course> courses) {
        BigDecimal total = courses.stream().map(Course::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.longValueExact();
    }

    private String buildOrderInfo(MomoPaymentRequest request, List<Course> courses) {
        if (!isBlank(request.getOrderInfo())) {
            return request.getOrderInfo().trim();
        }
        if (courses.size() == 1) {
            return momoConfig.getOrderInfoPrefix() + ": " + courses.get(0).getCourseName();
        }
        return momoConfig.getOrderInfoPrefix() + ": " + courses.size() + " khoa hoc";
    }

    private String buildRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String buildMomoOrderId(Integer orderId, String requestId) {
        String partnerCode = defaultIfBlank(momoConfig.getPartnerCode(), "MOMO")
                .replaceAll("[^A-Za-z0-9_]", "")
                .toUpperCase();
        String requestSuffix = defaultIfBlank(requestId, UUID.randomUUID().toString().replace("-", ""));
        if (requestSuffix.length() > 12) {
            requestSuffix = requestSuffix.substring(requestSuffix.length() - 12);
        }
        return partnerCode + "_" + orderId + "_" + requestSuffix;
    }

    private String buildOrderNotes(String momoOrderId,
                                   String requestId,
                                   String status,
                                   Integer resultCode,
                                   String transId) {
        StringBuilder notes = new StringBuilder("provider=MOMO");
        notes.append(";momoOrderId=").append(limitNotePart(momoOrderId, 40));
        notes.append(";requestId=").append(limitNotePart(requestId, 40));
        notes.append(";status=").append(limitNotePart(status, 12));
        if (resultCode != null) {
            notes.append(";resultCode=").append(resultCode);
        }
        if (!isBlank(transId)) {
            notes.append(";transId=").append(limitNotePart(transId, 24));
        }
        return notes.toString();
    }

    private String limitNotePart(String value, int maxLength) {
        String normalized = defaultIfBlank(value, "");
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, maxLength);
    }

    private Order resolveOrderByMomoOrderId(String momoOrderId) {
        if (isBlank(momoOrderId)) {
            return null;
        }

        for (Order order : orderRepository.findAll()) {
            if (momoOrderId.equals(extractNoteValue(order.getNotes(), "momoOrderId"))) {
                return order;
            }
        }
        return null;
    }

    private void saveOrderDetails(Order order, List<Course> courses) {
        for (Course course : courses) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(order);
            orderDetail.setCourse(course);
            orderDetailRepository.save(orderDetail);
        }
    }

    private String sendCreatePaymentRequest(Map<String, Object> requestBody) throws Exception {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(momoConfig.getEndpoint()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(MomoUtil.toJson(requestBody)))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("MoMo API trả về HTTP " + response.statusCode());
        }
        return response.body();
    }

    private boolean isValidCallbackSignature(Map<String, String> values) {
        String providedSignature = values.get("signature");
        if (isBlank(providedSignature)) {
            return false;
        }

        LinkedHashMap<String, String> signatureFields = new LinkedHashMap<>();
        signatureFields.put("accessKey", momoConfig.getAccessKey());
        signatureFields.put("amount", values.get("amount"));
        signatureFields.put("extraData", values.get("extraData"));
        signatureFields.put("message", values.get("message"));
        signatureFields.put("orderId", values.get("orderId"));
        signatureFields.put("orderInfo", values.get("orderInfo"));
        signatureFields.put("orderType", values.get("orderType"));
        signatureFields.put("partnerCode", momoConfig.getPartnerCode());
        signatureFields.put("payType", values.get("payType"));
        signatureFields.put("requestId", values.get("requestId"));
        signatureFields.put("responseTime", values.get("responseTime"));
        signatureFields.put("resultCode", values.get("resultCode"));
        signatureFields.put("transId", values.get("transId"));

        String rawHash = MomoUtil.buildRawSignature(signatureFields);
        String expectedSignature = MomoUtil.hmacSHA256(rawHash, momoConfig.getSecretKey());
        return expectedSignature.equals(providedSignature);
    }

    private void markOrderSuccess(Order order, String requestId, String transId, String message) {
        order.setStatus(Order.OrderStatus.SUCCESS);
        String momoOrderId = extractNoteValue(order.getNotes(), "momoOrderId");
        order.setNotes(buildOrderNotes(momoOrderId, requestId, Order.OrderStatus.SUCCESS.name(), 0, transId));
        orderRepository.save(order);
    }

    private void markOrderFailed(Order order, String requestId, String transId, Integer resultCode, String message) {
        order.setStatus(Order.OrderStatus.FAILED);
        String momoOrderId = extractNoteValue(order.getNotes(), "momoOrderId");
        order.setNotes(buildOrderNotes(momoOrderId, requestId, Order.OrderStatus.FAILED.name(), resultCode, transId));
        orderRepository.save(order);
    }

    private void createEnrollmentsIfNeeded(Order order) {
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderId(order.getId());
        for (OrderDetail orderDetail : orderDetails) {
            Integer userId = order.getUser().getId();
            Integer courseId = orderDetail.getCourse().getId();
            if (enrollmentRepository.findByUserIdAndCourseId(userId, courseId) != null) {
                continue;
            }

            Enrollment enrollment = new Enrollment();
            enrollment.setUser(order.getUser());
            enrollment.setCourse(orderDetail.getCourse());
            enrollment.setEnrollmentDate(LocalDateTime.now());
            enrollment.setCompletionStatus("Not Started");
            enrollmentService.grantEnrollmentAfterSuccessfulPayment(enrollment);
        }
    }

    private Map<String, String> normalizeCallbackData(Map<String, String> callbackData) {
        LinkedHashMap<String, String> values = new LinkedHashMap<>();
        if (callbackData != null) {
            callbackData.forEach((key, value) -> values.put(key, MomoUtil.nullToEmpty(value)));
        }
        values.putIfAbsent("partnerCode", momoConfig.getPartnerCode());
        values.putIfAbsent("accessKey", momoConfig.getAccessKey());
        values.putIfAbsent("extraData", "");
        values.putIfAbsent("orderType", momoConfig.getRequestType());
        values.putIfAbsent("payType", "qr");
        values.putIfAbsent("message", "");
        values.putIfAbsent("responseTime", String.valueOf(System.currentTimeMillis()));
        values.putIfAbsent("resultCode", "0");
        values.putIfAbsent("transId", "");
        values.putIfAbsent("requestId", "");
        values.putIfAbsent("orderId", "");
        values.putIfAbsent("orderInfo", "");
        values.putIfAbsent("amount", "0");
        values.putIfAbsent("signature", "");
        return values;
    }

    private Integer parseInteger(String value) {
        try {
            return value == null || value.isBlank() ? null : Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String defaultIfBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private String extractNoteValue(String notes, String key) {
        if (isBlank(notes) || isBlank(key)) {
            return null;
        }
        String prefix = key + "=";
        for (String part : notes.split("[;\\r\\n]+")) {
            String trimmed = part.trim();
            if (trimmed.startsWith(prefix)) {
                return trimmed.substring(prefix.length());
            }
        }
        return null;
    }

    private String validateCallbackAgainstOrder(Order order, Map<String, String> values) {
        if (!isBlank(order.getNotes())) {
            String storedRequestId = extractNoteValue(order.getNotes(), "requestId");
            if (!isBlank(storedRequestId) && !storedRequestId.equals(values.get("requestId"))) {
                return "requestId callback không khớp với đơn hàng.";
            }
        }

        if (!amountMatches(order.getTotalAmount(), values.get("amount"))) {
            return "Số tiền callback không khớp với đơn hàng.";
        }

        return null;
    }

    private boolean amountMatches(BigDecimal orderAmount, String callbackAmount) {
        if (orderAmount == null || isBlank(callbackAmount)) {
            return false;
        }
        try {
            return orderAmount.compareTo(new BigDecimal(callbackAmount)) == 0;
        } catch (NumberFormatException exception) {
            return false;
        }
    }


    private int resolveIpnResultCode(MomoCallbackResponse callbackResponse) {
        if (!callbackResponse.isValidSignature()) {
            return 97;
        }
        String status = callbackResponse.getStatus();
        if ("INVALID_ORDER".equals(status) || "NOT_FOUND".equals(status) || "MISMATCHED_CALLBACK".equals(status)) {
            return 1;
        }
        return 0;
    }
}
