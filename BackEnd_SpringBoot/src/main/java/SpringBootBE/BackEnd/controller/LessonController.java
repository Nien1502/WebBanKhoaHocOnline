package SpringBootBE.BackEnd.controller;

import SpringBootBE.BackEnd.Service.LessonService;
import SpringBootBE.BackEnd.model.Lesson;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lessons")
@CrossOrigin
@Transactional
public class LessonController {

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @GetMapping
    public ResponseEntity<List<Lesson>> getAllLessons() {
        return ResponseEntity.ok(lessonService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Lesson> getLessonById(@PathVariable Integer id) {
        Lesson lesson = lessonService.findById(id);
        return lesson != null ? ResponseEntity.ok(lesson) : ResponseEntity.notFound().build();
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Lesson>> getLessonsByCourse(@PathVariable Integer courseId) {
        return ResponseEntity.ok(lessonService.findByCourseId(courseId));
    }

    @GetMapping("/course/{courseId}/ordered")
    public ResponseEntity<List<Lesson>> getLessonsByCourseOrdered(@PathVariable Integer courseId) {
        return ResponseEntity.ok(lessonService.findByCourseIdOrderByOrderIndex(courseId));
    }

    @PostMapping
    public ResponseEntity<Lesson> createLesson(@RequestBody Lesson lesson) {
        try {
            return ResponseEntity.ok(lessonService.save(lesson));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Lesson> updateLesson(@PathVariable Integer id, @RequestBody Lesson lesson) {
        Lesson existingLesson = lessonService.findById(id);
        if (existingLesson != null) {
            lesson.setId(id);
            try {
                return ResponseEntity.ok(lessonService.update(lesson));
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.badRequest().build();
            }
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLesson(@PathVariable Integer id) {
        lessonService.delete(id);
        return ResponseEntity.ok().build();
    }
}
