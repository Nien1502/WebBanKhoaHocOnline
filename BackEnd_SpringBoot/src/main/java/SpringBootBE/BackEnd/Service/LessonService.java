package SpringBootBE.BackEnd.Service;

import SpringBootBE.BackEnd.model.Lesson;
import java.util.List;

public interface LessonService {
    List<Lesson> findAll();
    Lesson findById(Integer id);
    List<Lesson> findByCourseId(Integer courseId);
    List<Lesson> findByCourseIdOrderByOrderIndex(Integer courseId);
    Lesson save(Lesson lesson);
    Lesson update(Lesson lesson);
    void delete(Integer id);
}

