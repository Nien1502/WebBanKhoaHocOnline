package SpringBootBE.BackEnd.repository;

import SpringBootBE.BackEnd.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Integer> {
    List<Lesson> findByCourseId(Integer courseId);
    List<Lesson> findByCourseIdOrderByOrderIndexAsc(Integer courseId);
    boolean existsByCourse_IdAndLessonTitleIgnoreCase(Integer courseId, String lessonTitle);
    boolean existsByCourse_IdAndLessonTitleIgnoreCaseAndIdNot(Integer courseId, String lessonTitle, Integer id);
}

