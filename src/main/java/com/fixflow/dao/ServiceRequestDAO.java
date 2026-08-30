package com.fixflow.dao;

import com.fixflow.model.ServiceRequest;
import com.fixflow.model.Priority;
import com.fixflow.model.RequestStatus;
import com.fixflow.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceRequestDAO {

    private ServiceRequest mapResultSetToEntity(ResultSet rs) throws SQLException {
        ServiceRequest req = new ServiceRequest();
        req.setId(rs.getInt("id"));
        req.setUserId(rs.getInt("user_id"));
        req.setCategoryId(rs.getInt("category_id"));
        req.setTitle(rs.getString("title"));
        req.setDescription(rs.getString("description"));
        req.setPriority(Priority.valueOf(rs.getString("priority")));
        req.setStatus(RequestStatus.valueOf(rs.getString("status")));
        req.setLocation(rs.getString("location"));
        req.setCreatedAt(rs.getTimestamp("created_at"));
        req.setUpdatedAt(rs.getTimestamp("updated_at"));
        return req;
    }

    public ServiceRequest create(ServiceRequest req) {
        String sql = "INSERT INTO service_requests (user_id, category_id, title, description, priority, status, location) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
             
            stmt.setInt(1, req.getUserId());
            stmt.setInt(2, req.getCategoryId());
            stmt.setString(3, req.getTitle());
            stmt.setString(4, req.getDescription());
            stmt.setString(5, req.getPriority().name());
            stmt.setString(6, req.getStatus().name());
            stmt.setString(7, req.getLocation());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating service request failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    req.setId(generatedKeys.getInt(1));
                    return findById(req.getId()); // Re-fetch to get timestamps
                } else {
                    throw new SQLException("Creating service request failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error creating service request", e);
        }
    }

    public List<ServiceRequest> findAll() {
        List<ServiceRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM service_requests";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error finding all service requests", e);
        }
        return list;
    }

    public ServiceRequest findById(Integer id) {
        String sql = "SELECT * FROM service_requests WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error finding service request by id", e);
        }
        return null;
    }

    public boolean update(ServiceRequest req) {
        String sql = "UPDATE service_requests SET title = ?, description = ?, priority = ?, status = ?, location = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, req.getTitle());
            stmt.setString(2, req.getDescription());
            stmt.setString(3, req.getPriority().name());
            stmt.setString(4, req.getStatus().name());
            stmt.setString(5, req.getLocation());
            stmt.setInt(6, req.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Database error updating service request", e);
        }
    }

    public boolean updateStatus(Integer id, RequestStatus status, Connection conn) throws SQLException {
        String sql = "UPDATE service_requests SET status = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(Integer id, RequestStatus status) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            return updateStatus(id, status, conn);
        } catch (SQLException e) {
            throw new RuntimeException("Database error updating service request status", e);
        }
    }

    public boolean delete(Integer id) {
        String sql = "DELETE FROM service_requests WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Database error deleting service request", e);
        }
    }

    public List<ServiceRequest> findFiltered(com.fixflow.dto.RequestFilterDTO filter) {
        StringBuilder sql = new StringBuilder("SELECT DISTINCT r.* FROM service_requests r ");
        if (filter.getUserId() != null && filter.getUserId() == -1) {
             // -1 signifies TECHNICIAN role where we need a join
             sql.append("JOIN request_assignments a ON r.id = a.request_id ");
        }
        sql.append("WHERE 1=1 ");

        List<Object> params = buildFilterQuery(sql, filter);
        
        // Sorting
        if (filter.getSortBy() != null) {
            String col = mapSortColumn(filter.getSortBy());
            if (col != null) {
                sql.append(" ORDER BY r.").append(col);
                if ("desc".equalsIgnoreCase(filter.getSortOrder())) {
                    sql.append(" DESC");
                } else {
                    sql.append(" ASC");
                }
            }
        } else {
            sql.append(" ORDER BY r.created_at DESC"); // default
        }

        // Pagination
        if (filter.getLimit() != null && filter.getPage() != null) {
            sql.append(" LIMIT ? OFFSET ?");
            params.add(filter.getLimit());
            params.add((filter.getPage() - 1) * filter.getLimit());
        }

        List<ServiceRequest> list = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error finding filtered requests", e);
        }
        return list;
    }

    public long countFiltered(com.fixflow.dto.RequestFilterDTO filter) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(DISTINCT r.id) FROM service_requests r ");
        if (filter.getUserId() != null && filter.getUserId() == -1) {
             sql.append("JOIN request_assignments a ON r.id = a.request_id ");
        }
        sql.append("WHERE 1=1 ");

        List<Object> params = buildFilterQuery(sql, filter);

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error counting filtered requests", e);
        }
        return 0;
    }

    private List<Object> buildFilterQuery(StringBuilder sql, com.fixflow.dto.RequestFilterDTO filter) {
        List<Object> params = new ArrayList<>();

        if (filter.getUserId() != null) {
            if (filter.getUserId() != -1) {
                sql.append(" AND r.user_id = ? ");
                params.add(filter.getUserId());
            }
        }
        
        if (filter.getTechnicianId() != null) {
            sql.append(" AND a.technician_id = ? AND a.completed_at IS NULL ");
            params.add(filter.getTechnicianId());
        }
        
        if (filter.getStatus() != null) {
            sql.append(" AND r.status = ? ");
            params.add(filter.getStatus().name());
        }
        if (filter.getPriority() != null) {
            sql.append(" AND r.priority = ? ");
            params.add(filter.getPriority().name());
        }
        if (filter.getCategoryId() != null) {
            sql.append(" AND r.category_id = ? ");
            params.add(filter.getCategoryId());
        }
        if (filter.getLocation() != null && !filter.getLocation().trim().isEmpty()) {
            sql.append(" AND r.location = ? ");
            params.add(filter.getLocation());
        }
        if (filter.getSearch() != null && !filter.getSearch().trim().isEmpty()) {
            sql.append(" AND (LOWER(r.title) LIKE LOWER(?) OR LOWER(r.description) LIKE LOWER(?) OR LOWER(r.location) LIKE LOWER(?)) ");
            String searchPattern = "%" + filter.getSearch().trim() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
        }
        if (filter.getFromDate() != null) {
            sql.append(" AND r.created_at >= ? ");
            params.add(filter.getFromDate() + " 00:00:00");
        }
        if (filter.getToDate() != null) {
            sql.append(" AND r.created_at <= ? ");
            params.add(filter.getToDate() + " 23:59:59");
        }

        return params;
    }
    
    private String mapSortColumn(String sortBy) {
        if ("createdAt".equals(sortBy)) return "created_at";
        if ("priority".equals(sortBy)) return "priority";
        if ("status".equals(sortBy)) return "status";
        if ("title".equals(sortBy)) return "title";
        return null;
    }

    public com.fixflow.dto.StatisticsDTO getStatistics() {
        com.fixflow.dto.StatisticsDTO stats = new com.fixflow.dto.StatisticsDTO();
        String sqlTotal = "SELECT COUNT(*) FROM service_requests";
        String sqlStatus = "SELECT status, COUNT(*) FROM service_requests GROUP BY status";
        String sqlUrgent = "SELECT COUNT(*) FROM service_requests WHERE priority = 'URGENT'";
        String sqlCat = "SELECT c.name, COUNT(r.id) FROM service_categories c LEFT JOIN service_requests r ON c.id = r.category_id GROUP BY c.name";
        
        try (Connection conn = DatabaseUtil.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(sqlTotal); ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) stats.setTotalRequests(rs.getLong(1));
            }
            try (PreparedStatement stmt = conn.prepareStatement(sqlUrgent); ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) stats.setUrgent(rs.getLong(1));
            }
            try (PreparedStatement stmt = conn.prepareStatement(sqlStatus); ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String status = rs.getString(1);
                    long count = rs.getLong(2);
                    if ("PENDING".equals(status)) stats.setPending(count);
                    else if ("ASSIGNED".equals(status)) stats.setAssigned(count);
                    else if ("IN_PROGRESS".equals(status)) stats.setInProgress(count);
                    else if ("RESOLVED".equals(status)) stats.setResolved(count);
                    else if ("CLOSED".equals(status)) stats.setClosed(count);
                    else if ("CANCELLED".equals(status)) stats.setCancelled(count);
                }
            }
            List<com.fixflow.dto.CategoryStatsDTO> catStats = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sqlCat); ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    catStats.add(new com.fixflow.dto.CategoryStatsDTO(rs.getString(1) != null ? rs.getString(1) : "Unknown", rs.getLong(2)));
                }
            }
            stats.setRequestsByCategory(catStats);
            return stats;
        } catch (SQLException e) {
            throw new RuntimeException("Database error getting statistics", e);
        }
    }
}
