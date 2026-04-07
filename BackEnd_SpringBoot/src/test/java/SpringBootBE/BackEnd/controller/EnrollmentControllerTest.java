package SpringBootBE.BackEnd.controller;

import SpringBootBE.BackEnd.Service.EnrollmentService;
import SpringBootBE.BackEnd.model.Enrollment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollmentControllerTest {

    @Mock
    private EnrollmentService enrollmentService;

    @InjectMocks
    private EnrollmentController enrollmentController;

    @Test
    void getEnrollmentsByUser_ReturnsPurchasedCoursesAsEnrollments() {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(1);
        List<Enrollment> expected = List.of(enrollment);
        when(enrollmentService.findByUserId(7)).thenReturn(expected);

        ResponseEntity<List<Enrollment>> response = enrollmentController.getEnrollmentsByUser(7);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
        verify(enrollmentService).findByUserId(7);
    }

    @Test
    void getEnrollmentByUserAndCourse_WhenFound_ReturnsOk() {
        Enrollment expected = new Enrollment();
        expected.setId(9);
        when(enrollmentService.findByUserIdAndCourseId(7, 3)).thenReturn(expected);

        ResponseEntity<Enrollment> response = enrollmentController.getEnrollmentByUserAndCourse(7, 3);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
    }

    @Test
    void getEnrollmentByUserAndCourse_WhenMissing_ReturnsNotFound() {
        when(enrollmentService.findByUserIdAndCourseId(7, 3)).thenReturn(null);

        ResponseEntity<Enrollment> response = enrollmentController.getEnrollmentByUserAndCourse(7, 3);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void createEnrollment_AlwaysBlocked_ReturnsForbidden() {
        ResponseEntity<Map<String, Object>> response = enrollmentController.createEnrollment();

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        Map<?, ?> body = assertInstanceOf(Map.class, response.getBody());
        assertNotNull(body);
        assertEquals("Chỉ có thể tạo Enrollment từ thanh toán thành công.", body.get("message"));
        assertEquals(false, body.get("success"));
    }
}

