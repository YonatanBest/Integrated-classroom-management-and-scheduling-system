package com.classroom.dao;

import com.classroom.util.DatabaseUtil;

import java.sql.*;
import java.time.LocalDate;

/**
 * Data Access Object for Enrollment-related database operations.
 */
public class EnrollmentDAO {
    
    /**
     * Enroll a student in a course.
     */
    public static boolean enrollStudent(int studentId, int courseId) {
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            
            // Check if already enrolled
            String checkSql = "SELECT COUNT(*) FROM Enrollments WHERE student_id = ? AND course_id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, studentId);
            checkStmt.setInt(2, courseId);
            ResultSet checkRs = checkStmt.executeQuery();
            if (checkRs.next() && checkRs.getInt(1) > 0) {
                conn.rollback();
                return false; // Already enrolled
            }
            
            // Get student's assigned room
            String roomSql = "SELECT assigned_room FROM Users WHERE user_id = ?";
            PreparedStatement roomStmt = conn.prepareStatement(roomSql);
            roomStmt.setInt(1, studentId);
            ResultSet rs = roomStmt.executeQuery();
            
            if (!rs.next() || rs.getString("assigned_room") == null) {
                conn.rollback();
                return false; // Student has no assigned room
            }
            
            String assignedRoom = rs.getString("assigned_room");
            
            // Check if course is available in the assigned room
            String courseSql = "SELECT COUNT(*) FROM Schedule WHERE course_id = ? AND room = ?";
            PreparedStatement courseStmt = conn.prepareStatement(courseSql);
            courseStmt.setInt(1, courseId);
            courseStmt.setString(2, assignedRoom);
            ResultSet courseRs = courseStmt.executeQuery();
            
            if (!courseRs.next() || courseRs.getInt(1) == 0) {
                conn.rollback();
                return false; // Course not available in student's room
            }
            
            // Proceed with enrollment
            String enrollSql = "INSERT INTO Enrollments (student_id, course_id, enrollment_date) VALUES (?, ?, ?)";
            PreparedStatement enrollStmt = conn.prepareStatement(enrollSql);
            enrollStmt.setInt(1, studentId);
            enrollStmt.setInt(2, courseId);
            enrollStmt.setString(3, LocalDate.now().toString());
            
            int affectedRows = enrollStmt.executeUpdate();
            if (affectedRows > 0) {
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
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Unenroll a student from a course.
     */
    public static boolean unenrollStudent(int studentId, int courseId) {
        String sql = "DELETE FROM Enrollments WHERE student_id = ? AND course_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Check if a student is enrolled in a course.
     */
    public static boolean isStudentEnrolled(int studentId, int courseId) {
        String sql = "SELECT COUNT(*) FROM Enrollments WHERE student_id = ? AND course_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, courseId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Get the number of students enrolled in a course.
     */
    public static int getEnrollmentCount(int courseId) {
        String sql = "SELECT COUNT(*) FROM Enrollments WHERE course_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, courseId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }
}