package com.classroom.dao;

import com.classroom.model.Schedule;
import com.classroom.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Schedule-related database operations.
 */
public class ScheduleDAO {
    
    /**
     * Add a new schedule.
     */
    public static boolean addSchedule(Schedule schedule) {
        String sql = "INSERT INTO Schedule (course_id, instructor_id, day_of_week, start_time, end_time, room, program_type) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        
        try {
            conn = DatabaseUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            
            pstmt.setInt(1, schedule.getCourseId());
            pstmt.setInt(2, schedule.getInstructorId());
            pstmt.setString(3, schedule.getDayOfWeek());
            pstmt.setString(4, schedule.getStartTime());
            pstmt.setString(5, schedule.getEndTime());
            pstmt.setString(6, schedule.getRoom());
            pstmt.setString(7, schedule.getProgramType());
            
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
     * Update an existing schedule.
     */
    public static boolean updateSchedule(Schedule schedule) {
        String sql = "UPDATE Schedule SET course_id = ?, instructor_id = ?, day_of_week = ?, " +
                     "start_time = ?, end_time = ?, room = ?, program_type = ? WHERE schedule_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, schedule.getCourseId());
            pstmt.setInt(2, schedule.getInstructorId());
            pstmt.setString(3, schedule.getDayOfWeek());
            pstmt.setString(4, schedule.getStartTime());
            pstmt.setString(5, schedule.getEndTime());
            pstmt.setString(6, schedule.getRoom());
            pstmt.setString(7, schedule.getProgramType());
            pstmt.setInt(8, schedule.getScheduleId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete a schedule.
     */
    public static boolean deleteSchedule(int scheduleId) {
        String sql = "DELETE FROM Schedule WHERE schedule_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, scheduleId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get all schedules with detailed information.
     */
    public static List<Schedule> getAllSchedulesWithDetails() {
        List<Schedule> schedules = new ArrayList<>();
        String sql = "SELECT s.*, c.course_name, c.course_code, u.full_name as instructor_name " +
                     "FROM Schedule s " +
                     "JOIN Courses c ON s.course_id = c.course_id " +
                     "JOIN Users u ON s.instructor_id = u.user_id";
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Schedule schedule = extractScheduleFromResultSet(rs);
                schedule.setCourseName(rs.getString("course_name"));
                schedule.setCourseCode(rs.getString("course_code"));
                schedule.setInstructorName(rs.getString("instructor_name"));
                schedules.add(schedule);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return schedules;
    }
    
    /**
     * Get schedules for a specific student.
     */
    public static List<Schedule> getSchedulesByStudentId(int studentId) {
        List<Schedule> schedules = new ArrayList<>();
        String sql = "SELECT s.*, c.course_name, c.course_code, u.full_name as instructor_name " +
                     "FROM Schedule s " +
                     "JOIN Courses c ON s.course_id = c.course_id " +
                     "JOIN Users u ON s.instructor_id = u.user_id " +
                     "JOIN Enrollments e ON s.course_id = e.course_id " +
                     "WHERE e.student_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, studentId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Schedule schedule = extractScheduleFromResultSet(rs);
                    schedule.setCourseName(rs.getString("course_name"));
                    schedule.setCourseCode(rs.getString("course_code"));
                    schedule.setInstructorName(rs.getString("instructor_name"));
                    schedules.add(schedule);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return schedules;
    }
    
    /**
     * Helper method to create a Schedule object from ResultSet.
     */
    private static Schedule extractScheduleFromResultSet(ResultSet rs) throws SQLException {
        Schedule schedule = new Schedule();
        schedule.setScheduleId(rs.getInt("schedule_id"));
        schedule.setCourseId(rs.getInt("course_id"));
        schedule.setInstructorId(rs.getInt("instructor_id"));
        schedule.setDayOfWeek(rs.getString("day_of_week"));
        schedule.setStartTime(rs.getString("start_time"));
        schedule.setEndTime(rs.getString("end_time"));
        schedule.setRoom(rs.getString("room"));
        schedule.setProgramType(rs.getString("program_type"));
        return schedule;
    }
    
    /**
     * Get all unique rooms from the Schedule table.
     */
    public static List<String> getAllRooms() {
        List<String> rooms = new ArrayList<>();
        String sql = "SELECT DISTINCT room FROM Schedule ORDER BY room";
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                rooms.add(rs.getString("room"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return rooms;
    }
    
    /**
     * Get all schedules for a specific room with detailed information.
     */
    public static List<Schedule> getSchedulesByRoom(String room) {
        List<Schedule> schedules = new ArrayList<>();
        String sql = "SELECT s.*, c.course_name, c.course_code, u.full_name as instructor_name " +
                     "FROM Schedule s " +
                     "JOIN Courses c ON s.course_id = c.course_id " +
                     "JOIN Users u ON s.instructor_id = u.user_id " +
                     "WHERE s.room = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, room);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Schedule schedule = extractScheduleFromResultSet(rs);
                    schedule.setCourseName(rs.getString("course_name"));
                    schedule.setCourseCode(rs.getString("course_code"));
                    schedule.setInstructorName(rs.getString("instructor_name"));
                    schedules.add(schedule);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return schedules;
    }
    
    /**
     * Get schedules for a specific instructor.
     */
    public static List<Schedule> getSchedulesByInstructorId(int instructorId) {
        List<Schedule> schedules = new ArrayList<>();
        String sql = "SELECT s.*, c.course_name, c.course_code, u.full_name as instructor_name " +
                     "FROM Schedule s " +
                     "JOIN Courses c ON s.course_id = c.course_id " +
                     "JOIN Users u ON s.instructor_id = u.user_id " +
                     "WHERE s.instructor_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, instructorId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Schedule schedule = extractScheduleFromResultSet(rs);
                    schedule.setCourseName(rs.getString("course_name"));
                    schedule.setCourseCode(rs.getString("course_code"));
                    schedule.setInstructorName(rs.getString("instructor_name"));
                    schedules.add(schedule);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return schedules;
    }
}