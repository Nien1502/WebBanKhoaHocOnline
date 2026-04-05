package SpringBootBE.BackEnd.Service;

import SpringBootBE.BackEnd.model.Enrollment;
import java.util.List;

public interface EnrollmentService {
    List<Enrollment> findAll();
    Enrollment findById(Integer id);
    List<Enrollment> findByUserId(Integer userId);
    List<Enrollment> findByCourseId(Integer courseId);
    Enrollment findByUserIdAndCourseId(Integer userId, Integer courseId);
    Enrollment save(Enrollment enrollment);
    Enrollment update(Enrollment enrollment);
    Enrollment grantEnrollmentAfterSuccessfulPayment(Enrollment enrollment);
    void delete(Integer id);
}

