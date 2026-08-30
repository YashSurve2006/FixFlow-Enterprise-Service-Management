package com.fixflow.dto;

import com.fixflow.model.Priority;

public class CreateServiceRequestRequest {
    private Integer categoryId;
    private String title;
    private String description;
    private Priority priority;
    private String location;

    // Getters and Setters
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
