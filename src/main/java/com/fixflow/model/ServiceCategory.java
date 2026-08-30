package com.fixflow.model;

import java.sql.Timestamp;

public class ServiceCategory {
    private Integer id;
    private String name;
    private String description;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Constructors, Getters and Setters
    public ServiceCategory() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
