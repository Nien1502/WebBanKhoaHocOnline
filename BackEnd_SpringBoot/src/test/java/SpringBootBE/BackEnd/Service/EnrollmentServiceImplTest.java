package SpringBootBE.BackEnd.Service;

import SpringBootBE.BackEnd.model.Course;
import SpringBootBE.BackEnd.model.Enrollment;
import SpringBootBE.BackEnd.model.User;
import SpringBootBE.BackEnd.repository.EnrollmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceImplTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private EnrollmentServiceImpl enrollmentService;

    @Test
    void save_AlwaysBlocked_ThrowsIllegalStateException() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> enrollmentService.save(new Enrollment()));

        assertEquals("Chỉ có thể tạo Enrollment từ thanh toán thành công.", ex.getMessage());
        verify(enrollmentRepository, never()).save(org.mockito.ArgumentMatchers.any(Enrollment.class));
    }

    @Test
    void update_WhenEnrollmentMissing_ThrowsIllegalArgumentException() {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(99);
        when(enrollmentRepository.existsById(99)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> enrollmentService.update(enrollment));

        assertEquals("Không tìm thấy Enrollment để cập nhật.", ex.getMessage());
        verify(enrollmentRepository).existsById(99);
        verify(enrollmentRepository, never()).save(enrollment);
    }

    @Test
    void update_WhenEnrollmentExists_SavesAndReturns() {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(10);
        when(enrollmentRepository.existsById(10)).thenReturn(true);
        when(enrollmentRepository.save(enrollment)).thenReturn(enrollment);

        Enrollment actual = enrollmentService.update(enrollment);

        assertSame(enrollment, actual);
        verify(enrollmentRepository).existsById(10);
        verify(enrollmentRepository).save(enrollment);
    }

    @Test
    void grantEnrollmentAfterSuccessfulPayment_InvalidInput_ThrowsIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> enrollmentService.grantEnrollmentAfterSuccessfulPayment(new Enrollment()));

        assertEquals("Enrollment thanh toán không hợp lệ.", ex.getMessage());
        verify(enrollmentRepository, never()).save(org.mockito.ArgumentMatchers.any(Enrollment.class));
    }

    @Test
    void grantEnrollmentAfterSuccessfulPayment_WhenExistingEnrollment_ReturnsExisting() {
        Enrollment paymentEnrollment = createEnrollment(7, 5);
        Enrollment existingEnrollment = createEnrollment(100, 5);
        when(enrollmentRepository.findByUserIdAndCourseId(7, 5)).thenReturn(existingEnrollment);

        Enrollment actual = enrollmentService.grantEnrollmentAfterSuccessfulPayment(paymentEnrollment);

        assertSame(existingEnrollment, actual);
        verify(enrollmentRepository).findByUserIdAndCourseId(7, 5);
        verify(enrollmentRepository, never()).save(paymentEnrollment);
    }

    @Test
    void grantEnrollmentAfterSuccessfulPayment_WhenNotExists_SavesNewEnrollment() {
        Enrollment paymentEnrollment = createEnrollment(8, 12);
        when(enrollmentRepository.findByUserIdAndCourseId(8, 12)).thenReturn(null);
        when(enrollmentRepository.save(paymentEnrollment)).thenReturn(paymentEnrollment);

        Enrollment actual = enrollmentService.grantEnrollmentAfterSuccessfulPayment(paymentEnrollment);

        assertSame(paymentEnrollment, actual);
        verify(enrollmentRepository).findByUserIdAndCourseId(8, 12);
        verify(enrollmentRepository).save(paymentEnrollment);
    }

    private Enrollment createEnrollment(Integer userId, Integer courseId) {
        User user = new User();
        user.setId(userId);

        Course course = new Course();
        course.setId(courseId);

        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setCourse(course);
        return enrollment;
    }
}

