package com.classroom.dao;

import com.classroom.model.Resource;
import com.classroom.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ResourceDAO {
    public static List<Resource> getAvailableResources(String room) {
        List<Resource> resources = new ArrayList<>();
        String sql = """
                    SELECT r.*,
                           COALESCE(r.quantity - SUM(COALESCE(sr.quantity_needed, 0)), r.quantity) as available_quantity
                    FROM Resources r
                    LEFT JOIN ScheduleResources sr ON r.resource_id = sr.resource_id
                    WHERE r.room = ? AND r.status = 'Available'
                    GROUP BY r.resource_id, r.room, r.resource_type, r.quantity, r.status, r.last_checked
                """;

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, room);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    resources.add(new Resource(
                            rs.getInt("resource_id"),
                            rs.getString("room"),
                            rs.getString("resource_type"),
                            rs.getInt("available_quantity"),
                            rs.getString("status"),
                            rs.getString("last_checked")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return resources;
    }

    public static int getTotalResourceQuantity(String resourceType) {
        String sql = "SELECT SUM(quantity) as total FROM Resources WHERE resource_type = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, resourceType);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static int getAvailableResourceQuantity(String resourceType) {
        String sql = """
                    SELECT COALESCE(r.quantity - SUM(COALESCE(sr.quantity_needed, 0)), r.quantity) as available
                    FROM Resources r
                    LEFT JOIN ScheduleResources sr ON r.resource_id = sr.resource_id
                    WHERE r.resource_type = ? AND r.status = 'Available'
                    GROUP BY r.resource_id, r.quantity
                """;

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, resourceType);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("available");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static boolean updateResourceStatus(int resourceId, String status) {
        String sql = "UPDATE Resources SET status = ?, last_checked = CURRENT_TIMESTAMP WHERE resource_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, resourceId);

            int affectedRows = pstmt.executeUpdate();
            conn.commit();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Resource getResourceById(int resourceId) {
        String sql = "SELECT * FROM Resources WHERE resource_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, resourceId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Resource(
                            rs.getInt("resource_id"),
                            rs.getString("room"),
                            rs.getString("resource_type"),
                            rs.getInt("quantity"),
                            rs.getString("status"),
                            rs.getString("last_checked"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}