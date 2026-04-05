package SpringBootBE.BackEnd.controller;

import SpringBootBE.BackEnd.Service.LessonProgressService;
import SpringBootBE.BackEnd.model.LessonProgress;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lesson-progress")
@CrossOrigin
@Transactional
public class LessonProgressController {

    private final LessonProgressService lessonProgressService;

    public LessonProgressController(LessonProgressService lessonProgressService) {
        this.lessonProgressService = lessonProgressService;
    }

    @GetMapping
    public ResponseEntity<List<LessonProgress>> getAllLessonProgresses() {
        return ResponseEntity.ok(lessonProgressService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LessonProgress> getLessonProgressById(@PathVariable Integer id) {
        LessonProgress lessonProgress = lessonProgressService.findById(id);
        return lessonProgress != null ? ResponseEntity.ok(lessonProgress) : ResponseEntity.notFound().build();
    }

    @GetMapping("/enrollment/{enrollmentId}")
    public ResponseEntity<List<LessonProgress>> getLessonProgressByEnrollment(@PathVariable Integer enrollmentId) {
        return ResponseEntity.ok(lessonProgressService.findByEnrollmentId(enrollmentId));
    }

    @GetMapping("/enrollment/{enrollmentId}/lesson/{lessonId}")
    public ResponseEntity<LessonProgress> getLessonProgressByEnrollmentAndLesson(@PathVariable Integer enrollmentId, @PathVariable Integer lessonId) {
        LessonProgress lessonProgress = lessonProgressService.findByEnrollmentIdAndLessonId(enrollmentId, lessonId);
        return lessonProgress != null ? ResponseEntity.ok(lessonProgress) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<LessonProgress> createLessonProgress(@RequestBody LessonProgress lessonProgress) {
        return ResponseEntity.ok(lessonProgressService.save(lessonProgress));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LessonProgress> updateLessonProgress(@PathVariable Integer id, @RequestBody LessonProgress lessonProgress) {
        LessonProgress existingLessonProgress = lessonProgressService.findById(id);
        if (existingLessonProgress != null) {
            lessonProgress.setId(id);
            return ResponseEntity.ok(lessonProgressService.update(lessonProgress));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLessonProgress(@PathVariable Integer id) {
        lessonProgressService.delete(id);
        return ResponseEntity.ok().build();
    }
}

