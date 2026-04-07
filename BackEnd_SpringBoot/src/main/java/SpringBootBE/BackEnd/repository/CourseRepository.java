package SpringBootBE.BackEnd.repository;

import SpringBootBE.BackEnd.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer> {
    List<Course> findByCategoryId(Integer categoryId);
    Course findByCourseName(String courseName);
}

