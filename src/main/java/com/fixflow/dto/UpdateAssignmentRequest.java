package com.fixflow.dto;

public class UpdateAssignmentRequest {
    private Integer technicianId;
    private String notes;

    public Integer getTechnicianId() { return technicianId; }
    public void setTechnicianId(Integer technicianId) { this.technicianId = technicianId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
