package SpringBootBE.BackEnd.Service;

import SpringBootBE.BackEnd.model.LessonProgress;
import SpringBootBE.BackEnd.repository.LessonProgressRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class LessonProgressServiceImpl implements LessonProgressService {

    private final LessonProgressRepository lessonProgressRepository;

    public LessonProgressServiceImpl(LessonProgressRepository lessonProgressRepository) {
        this.lessonProgressRepository = lessonProgressRepository;
    }

    @Override
    public List<LessonProgress> findAll() {
        return lessonProgressRepository.findAll();
    }

    @Override
    public LessonProgress findById(Integer id) {
        return lessonProgressRepository.findById(id).orElse(null);
    }

    @Override
    public List<LessonProgress> findByEnrollmentId(Integer enrollmentId) {
        return lessonProgressRepository.findByEnrollmentId(enrollmentId);
    }

    @Override
    public LessonProgress findByEnrollmentIdAndLessonId(Integer enrollmentId, Integer lessonId) {
        return lessonProgressRepository.findByEnrollmentIdAndLessonId(enrollmentId, lessonId);
    }

    @Override
    public LessonProgress save(LessonProgress lessonProgress) {
        return lessonProgressRepository.save(lessonProgress);
    }

    @Override
    public LessonProgress update(LessonProgress lessonProgress) {
        return lessonProgressRepository.save(lessonProgress);
    }

    @Override
    public void delete(Integer id) {
        lessonProgressRepository.deleteById(id);
    }
}

