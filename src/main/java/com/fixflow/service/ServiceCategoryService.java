package com.fixflow.service;

import com.fixflow.dao.ServiceCategoryDAO;
import com.fixflow.dto.ServiceCategoryRequest;
import com.fixflow.exception.ConflictException;
import com.fixflow.exception.ResourceNotFoundException;
import com.fixflow.exception.ValidationException;
import com.fixflow.model.ServiceCategory;

import java.util.List;

public class ServiceCategoryService {

    private final ServiceCategoryDAO categoryDAO = new ServiceCategoryDAO();

    public List<ServiceCategory> getAllCategories() {
        return categoryDAO.findAll();
    }

    public ServiceCategory getCategoryById(Integer id) {
        ServiceCategory category = categoryDAO.findById(id);
        if (category == null) {
            throw new ResourceNotFoundException("Category not found with ID: " + id);
        }
        return category;
    }

    public ServiceCategory createCategory(ServiceCategoryRequest request) {
        validateCategoryRequest(request);
        
        ServiceCategory category = new ServiceCategory();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        
        return categoryDAO.create(category);
    }

    public ServiceCategory updateCategory(Integer id, ServiceCategoryRequest request) {
        validateCategoryRequest(request);
        
        ServiceCategory existing = categoryDAO.findById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Category not found with ID: " + id);
        }
        
        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        
        categoryDAO.update(existing);
        return categoryDAO.findById(id);
    }

    public ServiceCategory patchCategory(Integer id, ServiceCategoryRequest request) {
        ServiceCategory existing = categoryDAO.findById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Category not found with ID: " + id);
        }
        
        if (request.getName() != null) {
            if (request.getName().trim().isEmpty()) throw new ValidationException("Name cannot be empty");
            existing.setName(request.getName());
        }
        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }
        
        categoryDAO.update(existing);
        return categoryDAO.findById(id);
    }

    public void deleteCategory(Integer id) {
        if (categoryDAO.findById(id) == null) {
            throw new ResourceNotFoundException("Category not found with ID: " + id);
        }
        
        try {
            categoryDAO.delete(id);
        } catch (RuntimeException e) {
            // Check if it's a constraint violation
            if (e.getCause() instanceof java.sql.SQLException && e.getCause().getMessage().contains("a foreign key constraint fails")) {
                throw new ConflictException("Cannot delete category because there are service requests assigned to it.");
            }
            throw e;
        }
    }

    private void validateCategoryRequest(ServiceCategoryRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new ValidationException("Category name is required");
        }
    }
}
