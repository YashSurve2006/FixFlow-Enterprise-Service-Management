package com.fixflow.dao;

import com.fixflow.model.RequestAssignment;
import com.fixflow.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RequestAssignmentDAO {

    private RequestAssignment mapResultSetToEntity(ResultSet rs) throws SQLException {
        RequestAssignment assignment = new RequestAssignment();
        assignment.setId(rs.getInt("id"));
        assignment.setRequestId(rs.getInt("request_id"));
        assignment.setTechnicianId(rs.getInt("technician_id"));
        assignment.setAssignedBy(rs.getInt("assigned_by"));
        assignment.setAssignedAt(rs.getTimestamp("assigned_at"));
        assignment.setAcceptedAt(rs.getTimestamp("accepted_at"));
        assignment.setCompletedAt(rs.getTimestamp("completed_at"));
        assignment.setNotes(rs.getString("notes"));
        assignment.setCreatedAt(rs.getTimestamp("created_at"));
        assignment.setUpdatedAt(rs.getTimestamp("updated_at"));
        return assignment;
    }

    public RequestAssignment create(RequestAssignment assignment, Connection conn) throws SQLException {
        String sql = "INSERT INTO request_assignments (request_id, technician_id, assigned_by, notes) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, assignment.getRequestId());
            stmt.setInt(2, assignment.getTechnicianId());
            stmt.setInt(3, assignment.getAssignedBy());
            stmt.setString(4, assignment.getNotes());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating assignment failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    assignment.setId(generatedKeys.getInt(1));
                    return assignment;
                } else {
                    throw new SQLException("Creating assignment failed, no ID obtained.");
                }
            }
        }
    }

    public RequestAssignment findById(Integer id) {
        String sql = "SELECT * FROM request_assignments WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error finding assignment by id", e);
        }
        return null;
    }

    public List<RequestAssignment> findByRequestId(Integer requestId) {
        List<RequestAssignment> list = new ArrayList<>();
        String sql = "SELECT * FROM request_assignments WHERE request_id = ? ORDER BY assigned_at DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, requestId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error finding assignments by request_id", e);
        }
        return list;
    }
    
    public RequestAssignment findActiveByRequestId(Integer requestId) {
        String sql = "SELECT * FROM request_assignments WHERE request_id = ? AND completed_at IS NULL ORDER BY assigned_at DESC LIMIT 1";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, requestId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error finding active assignment by request_id", e);
        }
        return null;
    }

    public List<RequestAssignment> findByTechnicianId(Integer technicianId) {
        List<RequestAssignment> list = new ArrayList<>();
        String sql = "SELECT * FROM request_assignments WHERE technician_id = ? ORDER BY assigned_at DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, technicianId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error finding assignments by technician_id", e);
        }
        return list;
    }

    public List<RequestAssignment> findAll() {
        List<RequestAssignment> list = new ArrayList<>();
        String sql = "SELECT * FROM request_assignments";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error finding all assignments", e);
        }
        return list;
    }

    public boolean update(RequestAssignment assignment) {
        String sql = "UPDATE request_assignments SET technician_id = ?, notes = ?, accepted_at = ?, completed_at = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, assignment.getTechnicianId());
            stmt.setString(2, assignment.getNotes());
            stmt.setTimestamp(3, assignment.getAcceptedAt());
            stmt.setTimestamp(4, assignment.getCompletedAt());
            stmt.setInt(5, assignment.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Database error updating assignment", e);
        }
    }
    
    public boolean update(RequestAssignment assignment, Connection conn) throws SQLException {
        String sql = "UPDATE request_assignments SET technician_id = ?, notes = ?, accepted_at = ?, completed_at = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, assignment.getTechnicianId());
            stmt.setString(2, assignment.getNotes());
            stmt.setTimestamp(3, assignment.getAcceptedAt());
            stmt.setTimestamp(4, assignment.getCompletedAt());
            stmt.setInt(5, assignment.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean delete(Integer id) {
        String sql = "DELETE FROM request_assignments WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Database error deleting assignment", e);
        }
    }
}
