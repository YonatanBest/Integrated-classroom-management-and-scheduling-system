package com.classroom.dao;

import com.classroom.model.MakeupRequest;
import com.classroom.model.Notification;
import com.classroom.util.DatabaseUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MakeupRequestDAO {

    public static boolean createMakeupRequest(MakeupRequest request) {
        String sql = """
                    INSERT INTO MakeupRequests (room, course_id, rep_id, instructor_id, requested_date,
                    requested_time, status) VALUES (?, ?, ?, ?, ?, ?, 'Pending')
                """;

        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();

            // Insert the makeup request
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, request.getRoom());
            pstmt.setInt(2, request.getCourseId());
            pstmt.setInt(3, request.getRepId());
            pstmt.setInt(4, request.getInstructorId());
            pstmt.setDate(5, Date.valueOf(request.getRequestedDate()));
            pstmt.setString(6, request.getRequestedTime());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // Get the generated request ID
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {

                    // Create notification for instructor
                    String notificationMessage = String.format(
                            "New makeup class request for %s on %s at %s",
                            request.getCourseCode(),
                            request.getRequestedDate(),
                            request.getRequestedTime());

                    Notification notification = new Notification(
                            request.getInstructorId(),
                            notificationMessage,
                            "MakeupRequest");

                    NotificationDAO.createNotification(notification);
                    conn.commit();
                    return true;
                }
            }

            conn.rollback();
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean updateRequestStatus(int requestId, String status) {
        String sql = "UPDATE MakeupRequests SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE request_id = ?";

        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();

            // Get request details first
            MakeupRequest request = getMakeupRequestById(requestId);
            if (request == null) {
                return false;
            }

            // Update the status
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setInt(2, requestId);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                // Create notification for rep
                String notificationMessage = String.format(
                        "Your makeup class request for %s on %s at %s has been %s",
                        request.getCourseCode(),
                        request.getRequestedDate(),
                        request.getRequestedTime(),
                        status.toLowerCase());

                Notification notification = new Notification(
                        request.getRepId(),
                        notificationMessage,
                        "Makeup" + status);

                NotificationDAO.createNotification(notification);
                conn.commit();
                return true;
            }

            conn.rollback();
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<MakeupRequest> getRequestsByInstructor(int instructorId) {
        List<MakeupRequest> requests = new ArrayList<>();
        String sql = """
                    SELECT mr.*, c.course_name, c.course_code,
                           u1.full_name as rep_name, u2.full_name as instructor_name
                    FROM MakeupRequests mr
                    JOIN Courses c ON mr.course_id = c.course_id
                    JOIN Users u1 ON mr.rep_id = u1.user_id
                    JOIN Users u2 ON mr.instructor_id = u2.user_id
                    WHERE mr.instructor_id = ?
                    ORDER BY mr.created_at DESC
                """;

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, instructorId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    requests.add(extractMakeupRequestFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return requests;
    }

    public static List<MakeupRequest> getRequestsByRep(int repId) {
        List<MakeupRequest> requests = new ArrayList<>();
        String sql = """
                    SELECT mr.*, c.course_name, c.course_code,
                           u1.full_name as rep_name, u2.full_name as instructor_name
                    FROM MakeupRequests mr
                    JOIN Courses c ON mr.course_id = c.course_id
                    JOIN Users u1 ON mr.rep_id = u1.user_id
                    JOIN Users u2 ON mr.instructor_id = u2.user_id
                    WHERE mr.rep_id = ?
                    ORDER BY mr.created_at DESC
                """;

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, repId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    requests.add(extractMakeupRequestFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return requests;
    }

    public static MakeupRequest getMakeupRequestById(int requestId) {
        String sql = """
                    SELECT mr.*, c.course_name, c.course_code,
                           u1.full_name as rep_name, u2.full_name as instructor_name
                    FROM MakeupRequests mr
                    JOIN Courses c ON mr.course_id = c.course_id
                    JOIN Users u1 ON mr.rep_id = u1.user_id
                    JOIN Users u2 ON mr.instructor_id = u2.user_id
                    WHERE mr.request_id = ?
                """;

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, requestId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractMakeupRequestFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static MakeupRequest extractMakeupRequestFromResultSet(ResultSet rs) throws SQLException {
        MakeupRequest request = new MakeupRequest();
        request.setRequestId(rs.getInt("request_id"));
        request.setRoom(rs.getString("room"));
        request.setCourseId(rs.getInt("course_id"));
        request.setRepId(rs.getInt("rep_id"));
        request.setInstructorId(rs.getInt("instructor_id"));
        request.setRequestedDate(rs.getDate("requested_date").toLocalDate());
        request.setRequestedTime(rs.getString("requested_time"));
        request.setStatus(rs.getString("status"));
        request.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        request.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

        // Additional display fields
        request.setCourseName(rs.getString("course_name"));
        request.setCourseCode(rs.getString("course_code"));
        request.setRepName(rs.getString("rep_name"));
        request.setInstructorName(rs.getString("instructor_name"));

        return request;
    }
}