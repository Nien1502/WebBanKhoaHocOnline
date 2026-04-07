package SpringBootBE.BackEnd.Service;

import SpringBootBE.BackEnd.model.Course;
import java.util.List;

public interface CourseService {
    List<Course> findAll();
    Course findById(Integer id);
    Course findByCourseName(String courseName);
    List<Course> findByCategoryId(Integer categoryId);
    Course save(Course course);
    Course update(Course course);
    void delete(Integer id);
}

