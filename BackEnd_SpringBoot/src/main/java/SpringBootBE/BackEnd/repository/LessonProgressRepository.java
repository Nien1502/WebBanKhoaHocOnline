package SpringBootBE.BackEnd.repository;

import SpringBootBE.BackEnd.model.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, Integer> {
    List<LessonProgress> findByEnrollmentId(Integer enrollmentId);
    LessonProgress findByEnrollmentIdAndLessonId(Integer enrollmentId, Integer lessonId);
}

