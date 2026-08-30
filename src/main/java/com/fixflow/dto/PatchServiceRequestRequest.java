package com.fixflow.dto;

import com.fixflow.model.Priority;
import com.fixflow.model.RequestStatus;

public class PatchServiceRequestRequest {
    private String title;
    private String description;
    private Priority priority;
    private RequestStatus status;
    private String location;

    // Optional fields can be null if not updated
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    
    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
