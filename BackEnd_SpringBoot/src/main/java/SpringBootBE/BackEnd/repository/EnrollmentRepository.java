package SpringBootBE.BackEnd.repository;

import SpringBootBE.BackEnd.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Integer> {
    List<Enrollment> findByUserId(Integer userId);
    List<Enrollment> findByCourseId(Integer courseId);
    Enrollment findByUserIdAndCourseId(Integer userId, Integer courseId);
}

