package com.brian.ecommerce.project.service;

import com.brian.ecommerce.project.exceptions.APIException;
import com.brian.ecommerce.project.exceptions.ResourceNotFoundException;
import com.brian.ecommerce.project.model.Category;
import com.brian.ecommerce.project.payload.CategoryDTO;
import com.brian.ecommerce.project.payload.CategoryResponse;
import com.brian.ecommerce.project.repositories.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImplementation implements CategoryService {

    //private List<Category> categories = new ArrayList<>();
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;


    @Override
    @Transactional
    public CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        // Create a Sort object to define sorting order
        Sort sortByAndOrder;

        // Check if the sortOrder is "asc" (ascending)
        if (sortOrder.equalsIgnoreCase("asc")) {
            sortByAndOrder = Sort.by(sortBy).ascending(); // Sort in ascending order
        } else {
            sortByAndOrder = Sort.by(sortBy).descending(); // Sort in descending order
        }


        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Category> categoryPage = categoryRepository.findAll(pageable);

        //   Fetch catogories from the database
        List<Category> categories = categoryPage.getContent();
        // Check if the list is empty
        if (categories.isEmpty()) {
            throw new APIException("No category created till now.");
        }
        // Convert Category objects to Category DTO objects
        List<CategoryDTO> categoryDTOS = new ArrayList<>();
        for (Category category : categories) {
            CategoryDTO categoryDTO = modelMapper.map(category, CategoryDTO.class);
            categoryDTOS.add(categoryDTO);
        }
        // Create Category Response and return it
        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setContent(categoryDTOS);
        categoryResponse.setPageNumber(categoryPage.getNumber());
        categoryResponse.setPageSize(categoryPage.getSize());
        categoryResponse.setTotalElements((int)categoryPage.getTotalElements());
        categoryResponse.setTotalPages(categoryPage.getTotalPages());
        categoryResponse.setLastPage(categoryPage.isLast());
        return categoryResponse;

    }


    @Override
    @Transactional
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        // Convert DTO to Entity
        Category category = modelMapper.map(categoryDTO, Category.class);
        // Check if a category with the same name already exists
        Category existingCategory = categoryRepository.findByCategoryName(category.getCategoryName());
        if (existingCategory != null) {
            throw new APIException("Category with the name: " + category.getCategoryName() + " already exists");
        }
        // Save the new category in the database
        Category savedCategory = categoryRepository.save(category);
        // Convert the saved entity back to DTO
        return modelMapper.map(savedCategory, CategoryDTO.class);
    }


    @Override
    @Transactional
        public CategoryDTO deleteCategory(Long categoryId) {
        List<Category> categories = categoryRepository.findAll();
        for (Category category: categories){
            if (category.getCategoryId().equals(categoryId)){
                categoryRepository.deleteById(categoryId);
                return modelMapper.map(category, CategoryDTO.class);
            }
        }
        throw new ResourceNotFoundException("Category", "categoryId", categoryId);
    }

    @Override
    @Transactional
    public CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId) {
        List<Category> categories = categoryRepository.findAll();
        for (Category c : categories) {
            if (c.getCategoryId().equals(categoryId)) {
                Category category = modelMapper.map(categoryDTO, Category.class);
                // Update the existing category with new values
                c.setCategoryName(category.getCategoryName());
                // Save the updated category
                Category savedCategory = categoryRepository.save(c);
                return modelMapper.map(savedCategory, CategoryDTO.class);
            }
        }
        throw new ResourceNotFoundException("Category", "categoryId", categoryId);
    }
}

