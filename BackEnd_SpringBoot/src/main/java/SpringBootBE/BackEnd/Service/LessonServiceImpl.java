package SpringBootBE.BackEnd.Service;

import SpringBootBE.BackEnd.model.Lesson;
import SpringBootBE.BackEnd.repository.LessonRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;

    public LessonServiceImpl(LessonRepository lessonRepository) {
        this.lessonRepository = lessonRepository;
    }

    @Override
    public List<Lesson> findAll() {
        return lessonRepository.findAll();
    }

    @Override
    public Lesson findById(Integer id) {
        return lessonRepository.findById(id).orElse(null);
    }

    @Override
    public List<Lesson> findByCourseId(Integer courseId) {
        return lessonRepository.findByCourseId(courseId);
    }

    @Override
    public List<Lesson> findByCourseIdOrderByOrderIndex(Integer courseId) {
        return lessonRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
    }

    @Override
    public Lesson save(Lesson lesson) {
        validateLessonTitleUnique(lesson, null);
        return lessonRepository.save(lesson);
    }

    @Override
    public Lesson update(Lesson lesson) {
        validateLessonTitleUnique(lesson, lesson.getId());
        return lessonRepository.save(lesson);
    }

    @Override
    public void delete(Integer id) {
        lessonRepository.deleteById(id);
    }

    private void validateLessonTitleUnique(Lesson lesson, Integer currentLessonId) {
        if (lesson == null || lesson.getCourse() == null || lesson.getCourse().getId() == null) {
            return;
        }

        String title = lesson.getLessonTitle();
        if (title == null || title.trim().isEmpty()) {
            return;
        }

        Integer courseId = lesson.getCourse().getId();
        boolean exists;
        if (currentLessonId == null) {
            exists = lessonRepository.existsByCourse_IdAndLessonTitleIgnoreCase(courseId, title.trim());
        } else {
            exists = lessonRepository.existsByCourse_IdAndLessonTitleIgnoreCaseAndIdNot(courseId, title.trim(), currentLessonId);
        }

        if (exists) {
            throw new IllegalArgumentException("Ten bai hoc da ton tai trong khoa hoc nay.");
        }
    }
}

