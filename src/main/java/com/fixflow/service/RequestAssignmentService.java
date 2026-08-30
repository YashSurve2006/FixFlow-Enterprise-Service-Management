package com.fixflow.service;

import com.fixflow.dao.RequestAssignmentDAO;
import com.fixflow.dao.ServiceRequestDAO;
import com.fixflow.dao.UserDAO;
import com.fixflow.dto.AssignTechnicianRequest;
import com.fixflow.dto.AssignmentResponse;
import com.fixflow.dto.UpdateAssignmentRequest;
import com.fixflow.dto.UserResponse;
import com.fixflow.exception.ConflictException;
import com.fixflow.exception.ResourceNotFoundException;
import com.fixflow.exception.ValidationException;
import com.fixflow.model.RequestAssignment;
import com.fixflow.model.RequestStatus;
import com.fixflow.model.Role;
import com.fixflow.model.ServiceRequest;
import com.fixflow.model.User;
import com.fixflow.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

public class RequestAssignmentService {

    private final RequestAssignmentDAO assignmentDAO = new RequestAssignmentDAO();
    private final ServiceRequestDAO requestDAO = new ServiceRequestDAO();
    private final UserDAO userDAO = new UserDAO();

    public AssignmentResponse assignTechnician(Integer requestId, AssignTechnicianRequest dto, Integer assignedById) {
        if (dto.getTechnicianId() == null) {
            throw new ValidationException("Technician ID is required");
        }

        User technician = userDAO.findById(dto.getTechnicianId());
        if (technician == null || technician.getRole() != Role.TECHNICIAN) {
            throw new ValidationException("Selected user is not a technician");
        }

        ServiceRequest request = requestDAO.findById(requestId);
        if (request == null) {
            throw new ResourceNotFoundException("Service request not found");
        }
        
        if (request.getStatus() != RequestStatus.PENDING && request.getStatus() != RequestStatus.ASSIGNED) {
            throw new ValidationException("Only PENDING or ASSIGNED requests can be assigned to a technician");
        }

        RequestAssignment active = assignmentDAO.findActiveByRequestId(requestId);
        if (active != null) {
            if (active.getTechnicianId().equals(dto.getTechnicianId())) {
                throw new ConflictException("Technician is already actively assigned to this request");
            }
            // If reassigned, mark old assignment as completed automatically (or we could just leave it, but marking it makes it inactive)
            active.setCompletedAt(new Timestamp(System.currentTimeMillis()));
            assignmentDAO.update(active);
        }

        RequestAssignment assignment = new RequestAssignment();
        assignment.setRequestId(requestId);
        assignment.setTechnicianId(dto.getTechnicianId());
        assignment.setAssignedBy(assignedById);
        assignment.setNotes(dto.getNotes());

        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Create Assignment
                assignment = assignmentDAO.create(assignment, conn);
                
                // 2. Update Request Status to ASSIGNED if it was PENDING
                if (request.getStatus() == RequestStatus.PENDING) {
                    requestDAO.updateStatus(requestId, RequestStatus.ASSIGNED, conn);
                }
                
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw new RuntimeException("Transaction failed while assigning technician", ex);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error during assignment transaction", e);
        }
        
        return mapToResponse(assignmentDAO.findById(assignment.getId()));
    }

    public List<AssignmentResponse> getAllAssignments() {
        return assignmentDAO.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public AssignmentResponse getAssignmentById(Integer id, Integer authUserId, Role authRole) {
        RequestAssignment assignment = assignmentDAO.findById(id);
        if (assignment == null) {
            throw new ResourceNotFoundException("Assignment not found");
        }
        
        if (authRole == Role.TECHNICIAN && !assignment.getTechnicianId().equals(authUserId)) {
            throw new ValidationException("You do not have permission to view this assignment");
        }
        
        if (authRole == Role.USER) {
            ServiceRequest request = requestDAO.findById(assignment.getRequestId());
            if (!request.getUserId().equals(authUserId)) {
                throw new ValidationException("You do not have permission to view this assignment");
            }
        }

        return mapToResponse(assignment);
    }

    public List<AssignmentResponse> getAssignmentsByRequestId(Integer requestId, Integer authUserId, Role authRole) {
        if (authRole == Role.USER) {
            ServiceRequest request = requestDAO.findById(requestId);
            if (request == null || !request.getUserId().equals(authUserId)) {
                throw new ValidationException("You do not have permission to view assignments for this request");
            }
        }
        return assignmentDAO.findByRequestId(requestId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public AssignmentResponse updateAssignment(Integer id, UpdateAssignmentRequest dto) {
        RequestAssignment assignment = assignmentDAO.findById(id);
        if (assignment == null) {
            throw new ResourceNotFoundException("Assignment not found");
        }
        
        if (dto.getTechnicianId() != null) {
            User technician = userDAO.findById(dto.getTechnicianId());
            if (technician == null || technician.getRole() != Role.TECHNICIAN) {
                throw new ValidationException("Selected user is not a technician");
            }
            assignment.setTechnicianId(dto.getTechnicianId());
        }
        
        if (dto.getNotes() != null) {
            assignment.setNotes(dto.getNotes());
        }
        
        assignmentDAO.update(assignment);
        return mapToResponse(assignmentDAO.findById(id));
    }

    public void deleteAssignment(Integer id) {
        if (assignmentDAO.findById(id) == null) {
            throw new ResourceNotFoundException("Assignment not found");
        }
        assignmentDAO.delete(id);
    }

    private AssignmentResponse mapToResponse(RequestAssignment assignment) {
        AssignmentResponse res = new AssignmentResponse();
        res.setId(assignment.getId());
        res.setRequestId(assignment.getRequestId());
        
        User tech = userDAO.findById(assignment.getTechnicianId());
        res.setTechnician(mapUserToResponse(tech));
        
        User admin = userDAO.findById(assignment.getAssignedBy());
        res.setAssignedBy(mapUserToResponse(admin));
        
        ServiceRequest req = requestDAO.findById(assignment.getRequestId());
        if (req != null) {
            res.setRequestStatus(req.getStatus());
        }
        
        res.setAssignedAt(assignment.getAssignedAt() != null ? assignment.getAssignedAt().toString() : null);
        res.setAcceptedAt(assignment.getAcceptedAt() != null ? assignment.getAcceptedAt().toString() : null);
        res.setCompletedAt(assignment.getCompletedAt() != null ? assignment.getCompletedAt().toString() : null);
        res.setNotes(assignment.getNotes());
        return res;
    }
    
    private UserResponse mapUserToResponse(User user) {
        if (user == null) return null;
        UserResponse res = new UserResponse();
        res.setId(user.getId());
        res.setName(user.getName());
        res.setEmail(user.getEmail());
        res.setPhone(user.getPhone());
        res.setRole(user.getRole());
        return res;
    }
}
