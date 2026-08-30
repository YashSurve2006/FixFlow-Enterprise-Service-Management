package com.fixflow.dto;

import com.fixflow.model.RequestStatus;

public class AssignmentResponse {
    private Integer id;
    private Integer requestId;
    private UserResponse technician;
    private UserResponse assignedBy;
    private RequestStatus requestStatus;
    private String assignedAt;
    private String acceptedAt;
    private String completedAt;
    private String notes;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getRequestId() { return requestId; }
    public void setRequestId(Integer requestId) { this.requestId = requestId; }

    public UserResponse getTechnician() { return technician; }
    public void setTechnician(UserResponse technician) { this.technician = technician; }

    public UserResponse getAssignedBy() { return assignedBy; }
    public void setAssignedBy(UserResponse assignedBy) { this.assignedBy = assignedBy; }
    
    public RequestStatus getRequestStatus() { return requestStatus; }
    public void setRequestStatus(RequestStatus requestStatus) { this.requestStatus = requestStatus; }

    public String getAssignedAt() { return assignedAt; }
    public void setAssignedAt(String assignedAt) { this.assignedAt = assignedAt; }

    public String getAcceptedAt() { return acceptedAt; }
    public void setAcceptedAt(String acceptedAt) { this.acceptedAt = acceptedAt; }

    public String getCompletedAt() { return completedAt; }
    public void setCompletedAt(String completedAt) { this.completedAt = completedAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
