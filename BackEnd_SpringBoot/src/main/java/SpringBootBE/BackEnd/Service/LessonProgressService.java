package SpringBootBE.BackEnd.Service;

import SpringBootBE.BackEnd.model.LessonProgress;
import java.util.List;

public interface LessonProgressService {
    List<LessonProgress> findAll();
    LessonProgress findById(Integer id);
    List<LessonProgress> findByEnrollmentId(Integer enrollmentId);
    LessonProgress findByEnrollmentIdAndLessonId(Integer enrollmentId, Integer lessonId);
    LessonProgress save(LessonProgress lessonProgress);
    LessonProgress update(LessonProgress lessonProgress);
    void delete(Integer id);
}

