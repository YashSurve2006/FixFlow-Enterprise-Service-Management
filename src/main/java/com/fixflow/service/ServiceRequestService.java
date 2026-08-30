package com.fixflow.service;

import com.fixflow.dao.ServiceCategoryDAO;
import com.fixflow.dao.ServiceRequestDAO;
import com.fixflow.dao.UserDAO;
import com.fixflow.dto.*;
import com.fixflow.exception.ForbiddenException;
import com.fixflow.exception.ResourceNotFoundException;
import com.fixflow.exception.ValidationException;
import com.fixflow.model.RequestStatus;
import com.fixflow.model.Role;
import com.fixflow.model.ServiceRequest;

import java.util.List;
import java.util.stream.Collectors;

public class ServiceRequestService {

    private final ServiceRequestDAO requestDAO = new ServiceRequestDAO();
    private final UserDAO userDAO = new UserDAO();
    private final ServiceCategoryDAO categoryDAO = new ServiceCategoryDAO();

    public ServiceRequestResponse createRequest(CreateServiceRequestRequest dto, Integer authUserId) {
        validateCreateRequest(dto);

        ServiceRequest req = new ServiceRequest();
        req.setUserId(authUserId);
        req.setCategoryId(dto.getCategoryId());
        req.setTitle(dto.getTitle());
        req.setDescription(dto.getDescription());
        req.setPriority(dto.getPriority());
        req.setStatus(RequestStatus.PENDING); // Default status
        req.setLocation(dto.getLocation());

        ServiceRequest created = requestDAO.create(req);
        return mapToResponse(created);
    }

    public PaginatedResponse<ServiceRequestResponse> getFilteredRequests(com.fixflow.dto.RequestFilterDTO filter, Integer authUserId, Role authRole) {
        if (authRole == Role.USER) {
            filter.setUserId(authUserId); // Only see their own
        } else if (authRole == Role.TECHNICIAN) {
            filter.setUserId(-1); // Trigger JOIN
            filter.setTechnicianId(authUserId); // Only see assigned
        }
        
        // Validation for sort
        if (filter.getSortBy() != null) {
            if (!filter.getSortBy().matches("^(createdAt|priority|status|title)$")) {
                throw new ValidationException("Invalid sortBy field. Allowed: createdAt, priority, status, title");
            }
        }
        
        if (filter.getSortOrder() != null) {
            if (!filter.getSortOrder().matches("^(asc|desc|ASC|DESC)$")) {
                throw new ValidationException("Invalid sortOrder field. Allowed: asc, desc");
            }
        }
        
        if (filter.getPage() == null || filter.getPage() < 1) {
            filter.setPage(1);
        }
        if (filter.getLimit() == null || filter.getLimit() < 1 || filter.getLimit() > 100) {
            filter.setLimit(10);
        }
        
        List<ServiceRequest> requests = requestDAO.findFiltered(filter);
        long totalItems = requestDAO.countFiltered(filter);
        
        List<ServiceRequestResponse> responseList = requests.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
                
        com.fixflow.dto.PaginationMeta meta = new com.fixflow.dto.PaginationMeta(filter.getPage(), filter.getLimit(), totalItems);
        return new PaginatedResponse<>(responseList, meta);
    }

    public ServiceRequestResponse getRequestById(Integer id, Integer authUserId, Role authRole) {
        ServiceRequest req = requestDAO.findById(id);
        if (req == null) {
            throw new ResourceNotFoundException("Service request not found with ID: " + id);
        }
        
        if (authRole == Role.USER && !req.getUserId().equals(authUserId)) {
            throw new ForbiddenException("You do not have permission to access this request");
        }
        
        return mapToResponse(req);
    }

    public ServiceRequestResponse updateRequest(Integer id, UpdateServiceRequestRequest dto, Integer authUserId, Role authRole) {
        ServiceRequest existing = requestDAO.findById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Service request not found with ID: " + id);
        }

        if (authRole == Role.USER && !existing.getUserId().equals(authUserId)) {
            throw new ForbiddenException("You can only modify your own requests");
        }

        validateUpdateRequest(dto);

        existing.setTitle(dto.getTitle());
        existing.setDescription(dto.getDescription());
        existing.setPriority(dto.getPriority());
        existing.setStatus(dto.getStatus());
        existing.setLocation(dto.getLocation());

        requestDAO.update(existing);
        
        return mapToResponse(requestDAO.findById(id)); 
    }

    public ServiceRequestResponse patchRequest(Integer id, PatchServiceRequestRequest dto, Integer authUserId, Role authRole) {
        ServiceRequest existing = requestDAO.findById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Service request not found with ID: " + id);
        }

        if (authRole == Role.USER && !existing.getUserId().equals(authUserId)) {
            throw new ForbiddenException("You can only modify your own requests");
        }

        if (dto.getTitle() != null) {
            if (dto.getTitle().trim().isEmpty()) throw new ValidationException("Title cannot be empty");
            existing.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            if (dto.getDescription().trim().isEmpty()) throw new ValidationException("Description cannot be empty");
            existing.setDescription(dto.getDescription());
        }
        if (dto.getPriority() != null) {
            existing.setPriority(dto.getPriority());
        }
        if (dto.getStatus() != null) {
            existing.setStatus(dto.getStatus());
        }
        if (dto.getLocation() != null) {
            if (dto.getLocation().trim().isEmpty()) throw new ValidationException("Location cannot be empty");
            existing.setLocation(dto.getLocation());
        }

        requestDAO.update(existing);
        
        return mapToResponse(requestDAO.findById(id));
    }

    public void deleteRequest(Integer id, Integer authUserId, Role authRole) {
        ServiceRequest existing = requestDAO.findById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Service request not found with ID: " + id);
        }
        
        if (authRole == Role.USER && !existing.getUserId().equals(authUserId)) {
            throw new ForbiddenException("You can only delete your own requests");
        }
        
        requestDAO.delete(id);
    }

    public ServiceRequestResponse updateStatus(Integer id, RequestStatus newStatus, Integer authUserId, Role authRole) {
        ServiceRequest existing = requestDAO.findById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("Service request not found with ID: " + id);
        }

        RequestStatus currentStatus = existing.getStatus();
        
        if (authRole == Role.USER) {
            throw new ForbiddenException("Users cannot directly change the status of requests");
        }

        if (authRole == Role.TECHNICIAN) {
            // Technician can only transition ASSIGNED -> IN_PROGRESS and IN_PROGRESS -> RESOLVED
            if (!isAssignedTechnician(id, authUserId)) {
                throw new ForbiddenException("You are not assigned to this request");
            }
            if (currentStatus == RequestStatus.ASSIGNED && newStatus != RequestStatus.IN_PROGRESS) {
                throw new ValidationException("Cannot transition request from " + currentStatus + " to " + newStatus);
            }
            if (currentStatus == RequestStatus.IN_PROGRESS && newStatus != RequestStatus.RESOLVED) {
                throw new ValidationException("Cannot transition request from " + currentStatus + " to " + newStatus);
            }
            if (currentStatus != RequestStatus.ASSIGNED && currentStatus != RequestStatus.IN_PROGRESS) {
                throw new ValidationException("Technicians cannot change status from " + currentStatus);
            }
        }

        if (authRole == Role.ADMIN) {
            // Admin workflow rules
            if (currentStatus == RequestStatus.PENDING && newStatus != RequestStatus.ASSIGNED && newStatus != RequestStatus.CANCELLED) {
                throw new ValidationException("Cannot transition request from PENDING to " + newStatus);
            }
            if (currentStatus == RequestStatus.RESOLVED && newStatus != RequestStatus.CLOSED) {
                throw new ValidationException("Cannot transition request from RESOLVED to " + newStatus);
            }
        }

        existing.setStatus(newStatus);
        requestDAO.updateStatus(id, newStatus);
        
        return mapToResponse(requestDAO.findById(id));
    }
    
    private boolean isAssignedTechnician(Integer requestId, Integer authUserId) {
        // Need to check RequestAssignmentDAO.
        // We will just instantiate it here for this simple check.
        com.fixflow.dao.RequestAssignmentDAO assignDAO = new com.fixflow.dao.RequestAssignmentDAO();
        com.fixflow.model.RequestAssignment active = assignDAO.findActiveByRequestId(requestId);
        return active != null && active.getTechnicianId().equals(authUserId);
    }

    private void validateCreateRequest(CreateServiceRequestRequest dto) {
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new ValidationException("Title is required");
        }
        if (dto.getDescription() == null || dto.getDescription().trim().isEmpty()) {
            throw new ValidationException("Description is required");
        }
        if (dto.getLocation() == null || dto.getLocation().trim().isEmpty()) {
            throw new ValidationException("Location is required");
        }
        if (dto.getPriority() == null) {
            throw new ValidationException("Priority is required");
        }
        if (dto.getCategoryId() == null) {
            throw new ValidationException("Category ID is required");
        }

        if (categoryDAO.findById(dto.getCategoryId()) == null) {
            throw new ValidationException("Invalid Category ID");
        }
    }

    private void validateUpdateRequest(UpdateServiceRequestRequest dto) {
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new ValidationException("Title is required");
        }
        if (dto.getDescription() == null || dto.getDescription().trim().isEmpty()) {
            throw new ValidationException("Description is required");
        }
        if (dto.getLocation() == null || dto.getLocation().trim().isEmpty()) {
            throw new ValidationException("Location is required");
        }
        if (dto.getPriority() == null) {
            throw new ValidationException("Priority is required");
        }
        if (dto.getStatus() == null) {
            throw new ValidationException("Status is required");
        }
    }

    private ServiceRequestResponse mapToResponse(ServiceRequest req) {
        ServiceRequestResponse res = new ServiceRequestResponse();
        res.setId(req.getId());
        res.setUserId(req.getUserId());
        res.setCategoryId(req.getCategoryId());
        res.setTitle(req.getTitle());
        res.setDescription(req.getDescription());
        res.setPriority(req.getPriority());
        res.setStatus(req.getStatus());
        res.setLocation(req.getLocation());
        res.setCreatedAt(req.getCreatedAt() != null ? req.getCreatedAt().toString() : null);
        res.setUpdatedAt(req.getUpdatedAt() != null ? req.getUpdatedAt().toString() : null);
        return res;
    }

    public com.fixflow.dto.StatisticsDTO getStatistics(Role authRole) {
        if (authRole != Role.ADMIN) {
            throw new ForbiddenException("Only administrators can view statistics");
        }
        return requestDAO.getStatistics();
    }
}
