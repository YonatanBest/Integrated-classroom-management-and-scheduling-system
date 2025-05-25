package com.classroom.dao;

import com.classroom.model.Schedule;
import com.classroom.util.DatabaseUtil;
import com.classroom.model.ScheduleResource;
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
        String sql = "INSERT INTO Schedule (course_id, instructor_id, day_of_week, start_time, end_time, room, program_type) "
                +
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
    public static boolean updateScheduleWithResources(Schedule schedule, List<ScheduleResource> resources) {
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);

            // First update the schedule
            String scheduleSql = "UPDATE Schedule SET course_id = ?, instructor_id = ?, day_of_week = ?, " +
                    "start_time = ?, end_time = ?, room = ?, program_type = ? WHERE schedule_id = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(scheduleSql)) {
                pstmt.setInt(1, schedule.getCourseId());
                pstmt.setInt(2, schedule.getInstructorId());
                pstmt.setString(3, schedule.getDayOfWeek());
                pstmt.setString(4, schedule.getStartTime());
                pstmt.setString(5, schedule.getEndTime());
                pstmt.setString(6, schedule.getRoom());
                pstmt.setString(7, schedule.getProgramType());
                pstmt.setInt(8, schedule.getScheduleId());

                pstmt.executeUpdate();

                // Delete existing resources
                String deleteResourcesSql = "DELETE FROM ScheduleResources WHERE schedule_id = ?";
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteResourcesSql)) {
                    deleteStmt.setInt(1, schedule.getScheduleId());
                    deleteStmt.executeUpdate();
                }

                // Insert new resources
                String resourceSql = "INSERT INTO ScheduleResources (schedule_id, resource_id, quantity_needed) VALUES (?, ?, ?)";
                try (PreparedStatement resourceStmt = conn.prepareStatement(resourceSql)) {
                    for (ScheduleResource resource : resources) {
                        resourceStmt.setInt(1, schedule.getScheduleId());
                        resourceStmt.setInt(2, resource.getResourceId());
                        resourceStmt.setInt(3, resource.getQuantityNeeded());
                        resourceStmt.executeUpdate();
                    }
                }

                conn.commit();
                return true;
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
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

    public static boolean hasScheduleConflict(Schedule schedule) {
        String sql = "SELECT s.*, c.course_name, c.course_code, u.full_name as instructor_name " +
                "FROM Schedule s " +
                "JOIN Courses c ON s.course_id = c.course_id " +
                "JOIN Users u ON s.instructor_id = u.user_id " +
                "WHERE s.day_of_week = ? AND s.schedule_id != ? AND " +
                "((s.room = ? OR s.instructor_id = ?) AND " +
                "(s.start_time < ? AND s.end_time > ?))"; // Modified condition

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, schedule.getDayOfWeek());
            pstmt.setInt(2, schedule.getScheduleId());
            pstmt.setString(3, schedule.getRoom());
            pstmt.setInt(4, schedule.getInstructorId());
            pstmt.setString(5, schedule.getEndTime()); // Swapped parameter order
            pstmt.setString(6, schedule.getStartTime()); // Swapped parameter order

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // Returns true if there's a conflict
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return true; // Assume conflict on error
        }
    }

    public static String getConflictDetails(Schedule schedule) {
        String sql = "SELECT s.*, c.course_name, c.course_code, u.full_name as instructor_name " +
                "FROM Schedule s " +
                "JOIN Courses c ON s.course_id = c.course_id " +
                "JOIN Users u ON s.instructor_id = u.user_id " +
                "WHERE s.day_of_week = ? AND s.schedule_id != ? AND " +
                "((s.room = ? OR s.instructor_id = ?) AND " +
                "(? < s.end_time AND ? > s.start_time))";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, schedule.getDayOfWeek());
            pstmt.setInt(2, schedule.getScheduleId());
            pstmt.setString(3, schedule.getRoom());
            pstmt.setInt(4, schedule.getInstructorId());
            pstmt.setString(5, schedule.getEndTime());
            pstmt.setString(6, schedule.getStartTime());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String conflictType = rs.getString("room").equals(schedule.getRoom()) ? "Room" : "Instructor";
                    return String.format("%s conflict: %s is already scheduled for %s from %s to %s",
                            conflictType,
                            conflictType.equals("Room") ? rs.getString("room") : rs.getString("instructor_name"),
                            rs.getString("course_code"),
                            rs.getString("start_time"),
                            rs.getString("end_time"));
                }
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error checking conflicts";
        }
    }

    public static boolean addScheduleWithResources(Schedule schedule, List<ScheduleResource> resources) {
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);

            // First insert the schedule
            String scheduleSql = "INSERT INTO Schedule (course_id, instructor_id, day_of_week, start_time, end_time, room, program_type) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(scheduleSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, schedule.getCourseId());
                pstmt.setInt(2, schedule.getInstructorId());
                pstmt.setString(3, schedule.getDayOfWeek());
                pstmt.setString(4, schedule.getStartTime());
                pstmt.setString(5, schedule.getEndTime());
                pstmt.setString(6, schedule.getRoom());
                pstmt.setString(7, schedule.getProgramType());

                int affectedRows = pstmt.executeUpdate();

                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int scheduleId = generatedKeys.getInt(1);

                            // Add resources if any
                            if (!resources.isEmpty()) {
                                String resourceSql = "INSERT INTO ScheduleResources (schedule_id, resource_id, quantity_needed) VALUES (?, ?, ?)";
                                try (PreparedStatement resourceStmt = conn.prepareStatement(resourceSql)) {
                                    for (ScheduleResource resource : resources) {
                                        resourceStmt.setInt(1, scheduleId);
                                        resourceStmt.setInt(2, resource.getResourceId());
                                        resourceStmt.setInt(3, resource.getQuantityNeeded());
                                        resourceStmt.executeUpdate();
                                    }
                                }
                            }

                            conn.commit();
                            return true;
                        }
                    }
                }

                conn.rollback();
                return false;

            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<ScheduleResource> getScheduleResources(int scheduleId) {
        List<ScheduleResource> resources = new ArrayList<>();
        String sql = "SELECT * FROM ScheduleResources WHERE schedule_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, scheduleId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    resources.add(new ScheduleResource(
                            rs.getInt("schedule_id"),
                            rs.getInt("resource_id"),
                            rs.getInt("quantity_needed")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return resources;
    }

    public static List<Schedule> getSchedulesInTimeRange(String day, String startTime, String endTime) {
        List<Schedule> schedules = new ArrayList<>();
        String sql = "SELECT s.*, c.course_name, c.course_code, u.full_name as instructor_name " +
                "FROM Schedule s " +
                "JOIN Courses c ON s.course_id = c.course_id " +
                "JOIN Users u ON s.instructor_id = u.user_id " +
                "WHERE s.day_of_week = ? AND " +
                "? < s.end_time AND ? > s.start_time";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, day);
            pstmt.setString(2, startTime);
            pstmt.setString(3, endTime);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Schedule schedule = extractScheduleFromResultSet(rs);
                    schedule.setRequiredResources(getScheduleResources(schedule.getScheduleId()));
                    schedules.add(schedule);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return schedules;
    }
}
