package SpringBootBE.BackEnd.Service;

import SpringBootBE.BackEnd.model.Enrollment;
import SpringBootBE.BackEnd.repository.EnrollmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    private static final String MANUAL_CREATE_BLOCKED_MESSAGE = "Chỉ có thể tạo Enrollment từ thanh toán thành công.";

    private final EnrollmentRepository enrollmentRepository;

    public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    @Override
    public List<Enrollment> findAll() {
        return enrollmentRepository.findAll();
    }

    @Override
    public Enrollment findById(Integer id) {
        return enrollmentRepository.findById(id).orElse(null);
    }

    @Override
    public List<Enrollment> findByUserId(Integer userId) {
        return enrollmentRepository.findByUserId(userId);
    }

    @Override
    public List<Enrollment> findByCourseId(Integer courseId) {
        return enrollmentRepository.findByCourseId(courseId);
    }

    @Override
    public Enrollment findByUserIdAndCourseId(Integer userId, Integer courseId) {
        return enrollmentRepository.findByUserIdAndCourseId(userId, courseId);
    }

    @Override
    public Enrollment save(Enrollment enrollment) {
        throw new IllegalStateException(MANUAL_CREATE_BLOCKED_MESSAGE);
    }

    @Override
    public Enrollment update(Enrollment enrollment) {
        if (enrollment == null || enrollment.getId() == null || !enrollmentRepository.existsById(enrollment.getId())) {
            throw new IllegalArgumentException("Không tìm thấy Enrollment để cập nhật.");
        }
        return enrollmentRepository.save(enrollment);
    }

    @Override
    public Enrollment grantEnrollmentAfterSuccessfulPayment(Enrollment enrollment) {
        if (enrollment == null || enrollment.getUser() == null || enrollment.getUser().getId() == null
                || enrollment.getCourse() == null || enrollment.getCourse().getId() == null) {
            throw new IllegalArgumentException("Enrollment thanh toán không hợp lệ.");
        }

        Enrollment existingEnrollment = enrollmentRepository.findByUserIdAndCourseId(
                enrollment.getUser().getId(),
                enrollment.getCourse().getId()
        );
        if (existingEnrollment != null) {
            return existingEnrollment;
        }

        return enrollmentRepository.save(enrollment);
    }

    @Override
    public void delete(Integer id) {
        enrollmentRepository.deleteById(id);
    }
}
