package com.classroom.dao;

import com.classroom.model.Course;
import com.classroom.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Course-related database operations.
 */
public class CourseDAO {

    /**
     * Add a new course.
     */
    public static boolean addCourse(Course course) {
        String sql = "INSERT INTO Courses (course_name, course_code, description, credit_hours) VALUES (?, ?, ?, ?)";
        Connection conn = null;

        try {
            conn = DatabaseUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, course.getCourseName());
            pstmt.setString(2, course.getCourseCode());
            pstmt.setString(3, course.getDescription());
            pstmt.setInt(4, course.getCreditHours());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                conn.commit();
                return true;
            }
            conn.rollback();
            return false;
        } catch (SQLException e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get all courses.
     */
    public static List<Course> getAllCourses() {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM Courses";

        try (Connection conn = DatabaseUtil.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                courses.add(extractCourseFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return courses;
    }

    /**
     * Get course by ID.
     */
    public static Course getCourseById(int courseId) {
        String sql = "SELECT * FROM Courses WHERE course_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courseId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractCourseFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Get courses for a specific student.
     */
    public static List<Course> getCoursesByStudentId(int studentId) {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT c.* FROM Courses c " +
                "JOIN Enrollments e ON c.course_id = e.course_id " +
                "WHERE e.student_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, studentId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    courses.add(extractCourseFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return courses;
    }

    /**
     * Get courses by room and program type
     */
    public static List<Course> getCoursesByRoom(String room) {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT DISTINCT c.* FROM Courses c " +
                "JOIN Schedule s ON c.course_id = s.course_id " +
                "WHERE s.room = ? " +
                "ORDER BY c.course_code";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, room);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    courses.add(extractCourseFromResultSet(rs));
                }
            }
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return courses;
    }

    /**
     * Get courses by room and program type
     */
    public static List<Course> getCoursesByRoomAndProgramType(String room, String programType) {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT DISTINCT c.* FROM Courses c " +
                "JOIN Schedule s ON c.course_id = s.course_id " +
                "WHERE s.room = ? AND s.program_type = ? " +
                "ORDER BY c.course_code";

        System.out.println("Searching for courses with room: " + room + ", program type: " + programType);

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, room);
            pstmt.setString(2, programType);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Course course = extractCourseFromResultSet(rs);
                    System.out.println("Found course: " + course.getCourseCode() + " - " + course.getCourseName());
                    courses.add(course);
                }
            }
            conn.commit();

            if (courses.isEmpty()) {
                System.out.println("No courses found for the given room and program type");
                // Let's check if there are any courses in this room at all
                String checkSql = "SELECT COUNT(*) FROM Schedule WHERE room = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setString(1, room);
                    ResultSet checkRs = checkStmt.executeQuery();
                    if (checkRs.next()) {
                        int count = checkRs.getInt(1);
                        System.out.println("Total schedules in room " + room + ": " + count);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
            e.printStackTrace();
        }

        return courses;
    }

    /**
     * Helper method to create a Course object from ResultSet.
     */
    private static Course extractCourseFromResultSet(ResultSet rs) throws SQLException {
        Course course = new Course();
        course.setCourseId(rs.getInt("course_id"));
        course.setCourseName(rs.getString("course_name"));
        course.setCourseCode(rs.getString("course_code"));
        course.setDescription(rs.getString("description"));
        course.setCreditHours(rs.getInt("credit_hours"));
        return course;
    }
}