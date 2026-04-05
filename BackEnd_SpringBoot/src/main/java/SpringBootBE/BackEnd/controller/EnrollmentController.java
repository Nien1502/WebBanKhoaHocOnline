package SpringBootBE.BackEnd.controller;

import SpringBootBE.BackEnd.Service.EnrollmentService;
import SpringBootBE.BackEnd.model.Enrollment;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enrollments")
@CrossOrigin
@Transactional
public class EnrollmentController {

    private static final String MANUAL_CREATE_BLOCKED_MESSAGE = "Chỉ có thể tạo Enrollment từ thanh toán thành công.";

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping
    public ResponseEntity<List<Enrollment>> getAllEnrollments() {
        return ResponseEntity.ok(enrollmentService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Enrollment> getEnrollmentById(@PathVariable Integer id) {
        Enrollment enrollment = enrollmentService.findById(id);
        return enrollment != null ? ResponseEntity.ok(enrollment) : ResponseEntity.notFound().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Enrollment>> getEnrollmentsByUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(enrollmentService.findByUserId(userId));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Enrollment>> getEnrollmentsByCourse(@PathVariable Integer courseId) {
        return ResponseEntity.ok(enrollmentService.findByCourseId(courseId));
    }

    @GetMapping("/user/{userId}/course/{courseId}")
    public ResponseEntity<Enrollment> getEnrollmentByUserAndCourse(@PathVariable Integer userId, @PathVariable Integer courseId) {
        Enrollment enrollment = enrollmentService.findByUserIdAndCourseId(userId, courseId);
        return enrollment != null ? ResponseEntity.ok(enrollment) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createEnrollment() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(errorBody());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Enrollment> updateEnrollment(@PathVariable Integer id, @RequestBody Enrollment enrollment) {
        Enrollment existingEnrollment = enrollmentService.findById(id);
        if (existingEnrollment != null) {
            enrollment.setId(id);
            return ResponseEntity.ok(enrollmentService.update(enrollment));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEnrollment(@PathVariable Integer id) {
        enrollmentService.delete(id);
        return ResponseEntity.ok().build();
    }

    private Map<String, Object> errorBody() {
        LinkedHashMap<String, Object> body = new LinkedHashMap<>();
        body.put("message", MANUAL_CREATE_BLOCKED_MESSAGE);
        body.put("success", false);
        return body;
    }
}
