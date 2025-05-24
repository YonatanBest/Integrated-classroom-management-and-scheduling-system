package com.classroom.dao;

import com.classroom.model.User;
import com.classroom.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for User-related database operations.
 */
public class UserDAO {
    
    /**
     * Authenticate a user with username and password.
     */
    public static User authenticateUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        Connection conn = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = extractUserFromResultSet(rs);
                conn.commit();
                return user;
            }
            conn.rollback();
            
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return null;
    }
    
    /**
     * Register a new user.
     */
    public static boolean registerUser(User user) {
        String checkSql = "SELECT COUNT(*) FROM users WHERE username = ?";
        String insertSql = "INSERT INTO users (username, password, full_name, email, user_type, program_type) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection()) {
            // Check for existing username
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, user.getUsername());
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    conn.rollback(); // Rollback if username exists
                    return false;
                }
            }
            
            // If username doesn't exist, proceed with insertion
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setString(1, user.getUsername());
                pstmt.setString(2, user.getPassword());
                pstmt.setString(3, user.getFullName());
                pstmt.setString(4, user.getEmail());
                pstmt.setString(5, user.getUserType());
                pstmt.setString(6, user.getProgramType());
                
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    conn.commit(); // Commit the transaction if successful
                    return true;
                }
                conn.rollback(); // Rollback if insert failed
            }
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                // Get connection and rollback in case of any error
                Connection conn = DatabaseUtil.getConnection();
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }
    
    /**
     * Get user by ID.
     */
    public static User getUserById(int userId) {
        String sql = "SELECT * FROM Users WHERE user_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Get all instructors.
     */
    public static List<User> getAllInstructors() {
        List<User> instructors = new ArrayList<>();
        String sql = "SELECT * FROM Users WHERE user_type = 'instructor'";
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                instructors.add(extractUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return instructors;
    }
    
    /**
     * Get all students.
     */
    public static List<User> getAllStudents() {
        List<User> students = new ArrayList<>();
        String sql = "SELECT * FROM Users WHERE user_type = 'student'";
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                students.add(extractUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return students;
    }
    
    /**
     * Get students enrolled in a specific course.
     */
    public static List<User> getStudentsByCourse(int courseId) {
        List<User> students = new ArrayList<>();
        String sql = "SELECT u.* FROM Users u " +
                     "JOIN Enrollments e ON u.user_id = e.student_id " +
                     "WHERE e.course_id = ? AND u.user_type = 'student'";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, courseId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    students.add(extractUserFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return students;
    }
    
    /**
     * Helper method to create a User object from ResultSet.
     */
    private static User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setUserType(rs.getString("user_type"));
        user.setProgramType(rs.getString("program_type"));
        return user;
    }
    
    /**
     * Assign a room to a student.
     */
    public static boolean assignRoom(int studentId, String room) {
        String sql = "UPDATE users SET assigned_room = ? WHERE user_id = ? AND user_type = 'student'";
        
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, room);
            pstmt.setInt(2, studentId);
            
            int affectedRows = pstmt.executeUpdate();
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
     * Get all students assigned to a specific room.
     */
    public static List<User> getStudentsByRoom(String room) {
        List<User> students = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE assigned_room = ? AND user_type = 'student'";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, room);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    students.add(extractUserFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return students;
    }

    /**
     * Get all students without an assigned room.
     */
    public static List<User> getUnassignedStudents() {
        List<User> students = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE assigned_room IS NULL AND user_type = 'student'";
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                students.add(extractUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return students;
    }
}