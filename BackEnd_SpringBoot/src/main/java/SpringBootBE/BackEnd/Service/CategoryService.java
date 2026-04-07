package SpringBootBE.BackEnd.Service;

import SpringBootBE.BackEnd.model.Category;
import java.util.List;

public interface CategoryService {
    List<Category> findAll();
    Category findById(Integer id);
    Category findByCategoryName(String categoryName);
    List<Category> findByParentId(Integer parentId);
    Category save(Category category);
    Category update(Category category);
    void delete(Integer id);
}

