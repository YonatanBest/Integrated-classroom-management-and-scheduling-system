package com.classroom.dao;

import com.classroom.model.Schedule;
import com.classroom.util.DatabaseUtil;
import com.classroom.model.ScheduleResource;
import com.classroom.util.ScheduleValidationUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import java.time.LocalDate;

/**
 * Data Access Object for Schedule-related database operations.
 */
public class ScheduleDAO {

    /**
     * Add a new schedule.
     */
    public static boolean addSchedule(Schedule schedule) {
        System.out.println("\nAttempting to add schedule:");
        System.out.println("Course ID: " + schedule.getCourseId());
        System.out.println("Instructor ID: " + schedule.getInstructorId());
        System.out.println("Day: " + schedule.getDayOfWeek());
        System.out.println("Time: " + schedule.getStartTime() + " - " + schedule.getEndTime());
        System.out.println("Room: " + schedule.getRoom());
        System.out.println("Program Type: " + schedule.getProgramType());

        // First validate the schedule
        try {
            List<ScheduleResource> resources = schedule.getRequiredResources();
            if (resources == null) {
                resources = new ArrayList<>();
            }
            System.out.println("Resources: " + resources.size());

            ScheduleValidationUtil.ValidationResult validationResult = ScheduleValidationUtil.validateSchedule(schedule,
                    resources);

            if (!validationResult.isValid) {
                System.out.println("Validation failed: " + validationResult.message);
                JOptionPane.showMessageDialog(null,
                        validationResult.message,
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
            System.out.println("Validation passed");

            Connection conn = null;
            try {
                conn = DatabaseUtil.getConnection();
                System.out.println("Database connection established");

                // Insert schedule
                String sql = "INSERT INTO Schedule (course_id, instructor_id, day_of_week, start_time, end_time, room, program_type) "
                        +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";

                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                pstmt.setInt(1, schedule.getCourseId());
                pstmt.setInt(2, schedule.getInstructorId());
                pstmt.setString(3, schedule.getDayOfWeek());
                pstmt.setString(4, schedule.getStartTime());
                pstmt.setString(5, schedule.getEndTime());
                pstmt.setString(6, schedule.getRoom());
                pstmt.setString(7, schedule.getProgramType());

                System.out.println("Executing SQL: " + sql);
                System.out.println("Parameters: " + schedule.getCourseId() + ", " + schedule.getInstructorId() + ", " +
                        schedule.getDayOfWeek() + ", " + schedule.getStartTime() + ", " + schedule.getEndTime() + ", " +
                        schedule.getRoom() + ", " + schedule.getProgramType());

                int affectedRows = pstmt.executeUpdate();
                System.out.println("Affected rows: " + affectedRows);

                if (affectedRows > 0) {
                    // Get the generated schedule ID
                    ResultSet generatedKeys = pstmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int scheduleId = generatedKeys.getInt(1);
                        schedule.setScheduleId(scheduleId);
                        System.out.println("Generated schedule ID: " + scheduleId);

                        // Add resources if any
                        if (!resources.isEmpty()) {
                            String resourceSql = "INSERT INTO ScheduleResources (schedule_id, resource_id, quantity_needed) VALUES (?, ?, ?)";
                            PreparedStatement resourceStmt = conn.prepareStatement(resourceSql);

                            for (ScheduleResource resource : resources) {
                                resourceStmt.setInt(1, scheduleId);
                                resourceStmt.setInt(2, resource.getResourceId());
                                resourceStmt.setInt(3, resource.getQuantityNeeded());
                                resourceStmt.executeUpdate();
                                System.out.println("Added resource: " + resource.getResourceId() + " with quantity "
                                        + resource.getQuantityNeeded());
                            }
                        }

                        conn.commit();
                        System.out.println("Transaction committed successfully");
                        return true;
                    }
                }

                conn.rollback();
                System.out.println("Transaction rolled back - no rows affected");
                return false;
            } catch (SQLException e) {
                if (conn != null) {
                    try {
                        conn.rollback();
                        System.out.println("Transaction rolled back due to error");
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
                e.printStackTrace();
                System.out.println("SQL Error: " + e.getMessage());
                JOptionPane.showMessageDialog(null,
                        "Error adding schedule: " + e.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            } finally {
                if (conn != null) {
                    try {
                        conn.close();
                        System.out.println("Database connection closed");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Validation Error: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "Error during schedule validation: " + e.getMessage(),
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * Update an existing schedule.
     */
    public static boolean updateSchedule(Schedule schedule) {
        // First validate the schedule
        ScheduleValidationUtil.ValidationResult validationResult = ScheduleValidationUtil.validateSchedule(schedule,
                schedule.getRequiredResources());

        if (!validationResult.isValid) {
            JOptionPane.showMessageDialog(null,
                    validationResult.message,
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();

            // Update the schedule
            String sql = "UPDATE Schedule SET course_id = ?, instructor_id = ?, day_of_week = ?, " +
                    "start_time = ?, end_time = ?, room = ?, program_type = ? WHERE schedule_id = ?";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, schedule.getCourseId());
            pstmt.setInt(2, schedule.getInstructorId());
            pstmt.setString(3, schedule.getDayOfWeek());
            pstmt.setString(4, schedule.getStartTime());
            pstmt.setString(5, schedule.getEndTime());
            pstmt.setString(6, schedule.getRoom());
            pstmt.setString(7, schedule.getProgramType());
            pstmt.setInt(8, schedule.getScheduleId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                // Update resources
                // First delete existing resources
                String deleteResourcesSql = "DELETE FROM ScheduleResources WHERE schedule_id = ?";
                PreparedStatement deleteStmt = conn.prepareStatement(deleteResourcesSql);
                deleteStmt.setInt(1, schedule.getScheduleId());
                deleteStmt.executeUpdate();

                // Then add new resources
                if (schedule.getRequiredResources() != null && !schedule.getRequiredResources().isEmpty()) {
                    String resourceSql = "INSERT INTO ScheduleResources (schedule_id, resource_id, quantity_needed) VALUES (?, ?, ?)";
                    PreparedStatement resourceStmt = conn.prepareStatement(resourceSql);

                    for (ScheduleResource resource : schedule.getRequiredResources()) {
                        resourceStmt.setInt(1, schedule.getScheduleId());
                        resourceStmt.setInt(2, resource.getResourceId());
                        resourceStmt.setInt(3, resource.getQuantityNeeded());
                        resourceStmt.executeUpdate();
                    }
                }

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
     * Delete a schedule.
     */
    public static boolean deleteSchedule(int scheduleId) {
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();

            // First delete associated resources
            String deleteResourcesSql = "DELETE FROM ScheduleResources WHERE schedule_id = ?";
            PreparedStatement resourceStmt = conn.prepareStatement(deleteResourcesSql);
            resourceStmt.setInt(1, scheduleId);
            resourceStmt.executeUpdate();

            // Then delete the schedule
            String deleteScheduleSql = "DELETE FROM Schedule WHERE schedule_id = ?";
            PreparedStatement scheduleStmt = conn.prepareStatement(deleteScheduleSql);
            scheduleStmt.setInt(1, scheduleId);

            int affectedRows = scheduleStmt.executeUpdate();
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

        // Properly set status and publish date
        String status = rs.getString("status");
        schedule.setStatus(status != null ? status : "draft");

        java.sql.Date publishDate = rs.getDate("publish_date");
        if (publishDate != null) {
            schedule.setPublishDate(publishDate.toString());
        }

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
                "(s.start_time < ? AND s.end_time > ?))";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, schedule.getDayOfWeek());
            pstmt.setInt(2, schedule.getScheduleId());
            pstmt.setString(3, schedule.getRoom());
            pstmt.setInt(4, schedule.getInstructorId());
            pstmt.setString(5, schedule.getEndTime()); // Check if existing schedule starts before our end
            pstmt.setString(6, schedule.getStartTime()); // Check if existing schedule ends after our start

            System.out.println("Checking schedule conflict:");
            System.out.println("Day: " + schedule.getDayOfWeek());
            System.out.println("Time: " + schedule.getStartTime() + " - " + schedule.getEndTime());
            System.out.println("Room: " + schedule.getRoom());
            System.out.println("Instructor ID: " + schedule.getInstructorId());

            try (ResultSet rs = pstmt.executeQuery()) {
                boolean hasConflict = rs.next();
                if (hasConflict) {
                    System.out.println("Conflict found:");
                    System.out.println(
                            "Existing schedule: " + rs.getString("start_time") + " - " + rs.getString("end_time"));
                }
                return hasConflict;
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
                "NOT (s.end_time <= ? OR s.start_time >= ?))";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, schedule.getDayOfWeek());
            pstmt.setInt(2, schedule.getScheduleId());
            pstmt.setString(3, schedule.getRoom());
            pstmt.setInt(4, schedule.getInstructorId());
            pstmt.setString(5, schedule.getStartTime()); // No overlap if existing ends before or at our start
            pstmt.setString(6, schedule.getEndTime()); // No overlap if existing starts after or at our end

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
                "NOT (s.end_time <= ? OR s.start_time >= ?)";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, day);
            pstmt.setString(2, startTime); // No overlap if existing ends before or at our start
            pstmt.setString(3, endTime); // No overlap if existing starts after or at our end

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Schedule schedule = extractScheduleFromResultSet(rs);
                    schedule.setCourseName(rs.getString("course_name"));
                    schedule.setCourseCode(rs.getString("course_code"));
                    schedule.setInstructorName(rs.getString("instructor_name"));
                    schedule.setRequiredResources(getScheduleResources(schedule.getScheduleId()));
                    schedules.add(schedule);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return schedules;
    }

    /**
     * Get schedule details for a specific room for debugging
     */
    public static void debugRoomSchedule(String room) {
        String sql = "SELECT s.*, c.course_name, c.course_code " +
                "FROM Schedule s " +
                "JOIN Courses c ON s.course_id = c.course_id " +
                "WHERE s.room = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, room);
            System.out.println("\nSchedule details for room: " + room);
            System.out.println("----------------------------------------");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    System.out.println(String.format(
                            "Course: %s - %s\n" +
                                    "Day: %s, Time: %s - %s\n" +
                                    "Program Type: %s\n" +
                                    "----------------------------------------",
                            rs.getString("course_code"),
                            rs.getString("course_name"),
                            rs.getString("day_of_week"),
                            rs.getString("start_time"),
                            rs.getString("end_time"),
                            rs.getString("program_type")));
                }
            }
            conn.commit();
        } catch (SQLException e) {
            System.err.println("Error checking room schedule: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get all schedules for a specific course.
     */
    public static List<Schedule> getSchedulesByCourseId(int courseId) {
        List<Schedule> schedules = new ArrayList<>();
        String sql = "SELECT s.*, c.course_name, c.course_code, u.full_name as instructor_name " +
                "FROM Schedule s " +
                "JOIN Courses c ON s.course_id = c.course_id " +
                "JOIN Users u ON s.instructor_id = u.user_id " +
                "WHERE s.course_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, courseId);

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
     * Get all schedules in draft status.
     */
    public static List<Schedule> getAllDraftSchedules() {
        List<Schedule> schedules = new ArrayList<>();
        String sql = "SELECT s.*, c.course_name, c.course_code, u.full_name as instructor_name " +
                "FROM Schedule s " +
                "JOIN Courses c ON s.course_id = c.course_id " +
                "JOIN Users u ON s.instructor_id = u.user_id " +
                "WHERE s.status = 'draft'";

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
     * Finalize a draft schedule by setting its status to published and setting the
     * publish date.
     */
    public static boolean finalizeSchedule(int scheduleId, LocalDate publishDate) {
        String sql = "UPDATE Schedule SET status = 'published', publish_date = ? WHERE schedule_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, java.sql.Date.valueOf(publishDate));
            pstmt.setInt(2, scheduleId);

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
}
