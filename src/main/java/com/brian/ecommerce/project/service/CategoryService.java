package com.brian.ecommerce.project.service;

import com.brian.ecommerce.project.model.Category;
import com.brian.ecommerce.project.payload.CategoryDTO;
import com.brian.ecommerce.project.payload.CategoryResponse;

import java.util.List;


public interface CategoryService {
    CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    CategoryDTO createCategory(CategoryDTO categoryDTO);

    CategoryDTO deleteCategory(Long categoryId);


    CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId);
}
