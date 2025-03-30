package com.brian.ecommerce.project.repositories;

import com.brian.ecommerce.project.model.Category;
import com.brian.ecommerce.project.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByCategoryOrderByPriceAsc(Category category, Pageable pageDetail);

    Page<Product> findByProductNameLikeIgnoreCase(String keyword, Pageable pageDetail);
}
