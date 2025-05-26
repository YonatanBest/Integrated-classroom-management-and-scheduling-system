package com.classroom.dao;

import com.classroom.model.ScheduleRevisionRequest;
import com.classroom.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ScheduleRevisionRequestDAO {

    public static boolean createRevisionRequest(ScheduleRevisionRequest request) {
        String sql = "INSERT INTO ScheduleRevisionRequests (schedule_id, instructor_id, request_reason, requested_changes) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, request.getScheduleId());
            pstmt.setInt(2, request.getInstructorId());
            pstmt.setString(3, request.getRequestReason());
            pstmt.setString(4, request.getRequestedChanges());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                // Update schedule status
                String updateSql = "UPDATE Schedule SET status = 'revision_requested' WHERE schedule_id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                updateStmt.setInt(1, request.getScheduleId());
                updateStmt.executeUpdate();

                conn.commit();
                return true;
            }

            conn.rollback();
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<ScheduleRevisionRequest> getRevisionRequestsByInstructor(int instructorId) {
        List<ScheduleRevisionRequest> requests = new ArrayList<>();
        String sql = """
                    SELECT r.*, s.course_id, c.course_name, u.full_name as instructor_name
                    FROM ScheduleRevisionRequests r
                    JOIN Schedule s ON r.schedule_id = s.schedule_id
                    JOIN Courses c ON s.course_id = c.course_id
                    JOIN Users u ON r.instructor_id = u.user_id
                    WHERE r.instructor_id = ?
                    ORDER BY r.created_at DESC
                """;

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, instructorId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ScheduleRevisionRequest request = extractFromResultSet(rs);
                    request.setCourseName(rs.getString("course_name"));
                    request.setInstructorName(rs.getString("instructor_name"));
                    requests.add(request);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return requests;
    }

    public static List<ScheduleRevisionRequest> getAllPendingRevisionRequests() {
        List<ScheduleRevisionRequest> requests = new ArrayList<>();
        String sql = """
                    SELECT r.*, s.course_id, c.course_name, u.full_name as instructor_name
                    FROM ScheduleRevisionRequests r
                    JOIN Schedule s ON r.schedule_id = s.schedule_id
                    JOIN Courses c ON s.course_id = c.course_id
                    JOIN Users u ON r.instructor_id = u.user_id
                    WHERE r.status = 'pending'
                    ORDER BY r.created_at DESC
                """;

        try (Connection conn = DatabaseUtil.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ScheduleRevisionRequest request = extractFromResultSet(rs);
                request.setCourseName(rs.getString("course_name"));
                request.setInstructorName(rs.getString("instructor_name"));
                requests.add(request);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return requests;
    }

    public static boolean updateRequestStatus(int requestId, String status) {
        String sql = "UPDATE ScheduleRevisionRequests SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE request_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, requestId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                conn.commit();
                return true;
            }

            conn.rollback();
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static ScheduleRevisionRequest extractFromResultSet(ResultSet rs) throws SQLException {
        ScheduleRevisionRequest request = new ScheduleRevisionRequest();
        request.setRequestId(rs.getInt("request_id"));
        request.setScheduleId(rs.getInt("schedule_id"));
        request.setInstructorId(rs.getInt("instructor_id"));
        request.setRequestReason(rs.getString("request_reason"));
        request.setRequestedChanges(rs.getString("requested_changes"));
        request.setStatus(rs.getString("status"));
        request.setCreatedAt(rs.getString("created_at"));
        request.setUpdatedAt(rs.getString("updated_at"));
        return request;
    }
}