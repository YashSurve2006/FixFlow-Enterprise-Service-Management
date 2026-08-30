package com.fixflow.dao;

import com.fixflow.model.ServiceCategory;
import com.fixflow.util.DatabaseUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ServiceCategoryDAO {
    public ServiceCategory findById(Integer id) {
        String sql = "SELECT * FROM service_categories WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ServiceCategory category = new ServiceCategory();
                    category.setId(rs.getInt("id"));
                    category.setName(rs.getString("name"));
                    category.setDescription(rs.getString("description"));
                    category.setCreatedAt(rs.getTimestamp("created_at"));
                    category.setUpdatedAt(rs.getTimestamp("updated_at"));
                    return category;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error in ServiceCategoryDAO", e);
        }
        return null;
    }

    public ServiceCategory create(ServiceCategory category) {
        String sql = "INSERT INTO service_categories (name, description) VALUES (?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Creating category failed, no rows affected.");
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return findById(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating category failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error creating category", e);
        }
    }

    public java.util.List<ServiceCategory> findAll() {
        java.util.List<ServiceCategory> list = new java.util.ArrayList<>();
        String sql = "SELECT * FROM service_categories ORDER BY name ASC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                ServiceCategory category = new ServiceCategory();
                category.setId(rs.getInt("id"));
                category.setName(rs.getString("name"));
                category.setDescription(rs.getString("description"));
                category.setCreatedAt(rs.getTimestamp("created_at"));
                category.setUpdatedAt(rs.getTimestamp("updated_at"));
                list.add(category);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error finding all categories", e);
        }
        return list;
    }

    public boolean update(ServiceCategory category) {
        String sql = "UPDATE service_categories SET name = ?, description = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, category.getName());
            stmt.setString(2, category.getDescription());
            stmt.setInt(3, category.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Database error updating category", e);
        }
    }

    public boolean delete(Integer id) {
        String sql = "DELETE FROM service_categories WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Database error deleting category", e);
        }
    }
}
