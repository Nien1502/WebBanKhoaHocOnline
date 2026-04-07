package SpringBootBE.BackEnd.repository;

import SpringBootBE.BackEnd.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Category findByCategoryName(String categoryName);
    List<Category> findByParentId(Integer parentId);
}

