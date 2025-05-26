package com.classroom.util;

import com.classroom.dao.CourseDAO;
import com.classroom.dao.ScheduleDAO;
import com.classroom.model.Course;
import com.classroom.model.Schedule;
import com.classroom.model.ScheduleResource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ScheduleValidationUtil {

    public static class ValidationResult {
        public boolean isValid;
        public String message;

        public ValidationResult(boolean isValid, String message) {
            this.isValid = isValid;
            this.message = message;
        }
    }

    public static ValidationResult validateSchedule(Schedule schedule, List<ScheduleResource> requiredResources) {
        System.out.println("\nValidating schedule:");
        System.out.println("Course ID: " + schedule.getCourseId());
        System.out.println("Instructor ID: " + schedule.getInstructorId());
        System.out.println("Day: " + schedule.getDayOfWeek());
        System.out.println("Time: " + schedule.getStartTime() + " - " + schedule.getEndTime());
        System.out.println("Room: " + schedule.getRoom());
        System.out.println("Program Type: " + schedule.getProgramType());
        System.out.println("Required Resources: " + (requiredResources != null ? requiredResources.size() : "null"));

        // Check instructor availability
        ValidationResult instructorCheck = checkInstructorAvailability(schedule);
        if (!instructorCheck.isValid) {
            System.out.println("Instructor validation failed: " + instructorCheck.message);
            return instructorCheck;
        }
        System.out.println("Instructor validation passed");

        // Check room availability
        ValidationResult roomCheck = checkRoomAvailability(schedule);
        if (!roomCheck.isValid) {
            System.out.println("Room validation failed: " + roomCheck.message);
            return roomCheck;
        }
        System.out.println("Room validation passed");

        // Check resource availability
        ValidationResult resourceCheck = checkResourceAvailability(schedule, requiredResources);
        if (!resourceCheck.isValid) {
            System.out.println("Resource validation failed: " + resourceCheck.message);
            return resourceCheck;
        }
        System.out.println("Resource validation passed");

        // Check credit hours
        ValidationResult creditCheck = checkCreditHours(schedule);
        if (!creditCheck.isValid) {
            System.out.println("Credit hours validation failed: " + creditCheck.message);
            return creditCheck;
        }
        System.out.println("Credit hours validation passed");

        System.out.println("All validations passed successfully");
        return new ValidationResult(true, "Schedule is valid");
    }

    private static ValidationResult checkInstructorAvailability(Schedule schedule) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "SELECT s.*, c.course_name FROM Schedule s " +
                    "JOIN Courses c ON s.course_id = c.course_id " +
                    "WHERE s.instructor_id = ? AND s.day_of_week = ? " +
                    "AND s.schedule_id != ? " +
                    "AND ((? BETWEEN s.start_time AND s.end_time) OR " +
                    "(? BETWEEN s.start_time AND s.end_time))";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, schedule.getInstructorId());
            pstmt.setString(2, schedule.getDayOfWeek());
            pstmt.setInt(3, schedule.getScheduleId());
            pstmt.setString(4, schedule.getStartTime());
            pstmt.setString(5, schedule.getEndTime());

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new ValidationResult(false,
                        "Instructor is already scheduled for " + rs.getString("course_name") +
                                " at " + rs.getString("start_time") + " - " + rs.getString("end_time"));
            }
        } catch (SQLException e) {
            return new ValidationResult(false, "Database error checking instructor availability");
        }
        return new ValidationResult(true, "Instructor is available");
    }

    private static ValidationResult checkRoomAvailability(Schedule schedule) {
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "SELECT s.*, c.course_name FROM Schedule s " +
                    "JOIN Courses c ON s.course_id = c.course_id " +
                    "WHERE s.room = ? AND s.day_of_week = ? " +
                    "AND s.schedule_id != ? " +
                    "AND ((? BETWEEN s.start_time AND s.end_time) OR " +
                    "(? BETWEEN s.start_time AND s.end_time))";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, schedule.getRoom());
            pstmt.setString(2, schedule.getDayOfWeek());
            pstmt.setInt(3, schedule.getScheduleId());
            pstmt.setString(4, schedule.getStartTime());
            pstmt.setString(5, schedule.getEndTime());

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new ValidationResult(false,
                        "Room " + schedule.getRoom() + " is already scheduled for " +
                                rs.getString("course_name") + " at " + rs.getString("start_time") +
                                " - " + rs.getString("end_time"));
            }
        } catch (SQLException e) {
            return new ValidationResult(false, "Database error checking room availability");
        }
        return new ValidationResult(true, "Room is available");
    }

    private static ValidationResult checkResourceAvailability(Schedule schedule,
            List<ScheduleResource> requiredResources) {
        if (requiredResources == null || requiredResources.isEmpty()) {
            return new ValidationResult(true, "No resources required");
        }

        try (Connection conn = DatabaseUtil.getConnection()) {
            for (ScheduleResource resource : requiredResources) {
                String sql = "SELECT r.quantity - COALESCE((" +
                        "SELECT SUM(sr.quantity_needed) FROM ScheduleResources sr " +
                        "JOIN Schedule s ON sr.schedule_id = s.schedule_id " +
                        "WHERE sr.resource_id = ? AND s.day_of_week = ? " +
                        "AND s.schedule_id != ? " +
                        "AND ((? BETWEEN s.start_time AND s.end_time) OR " +
                        "(? BETWEEN s.start_time AND s.end_time))" +
                        "), 0) as available_quantity " +
                        "FROM Resources r WHERE r.resource_id = ?";

                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, resource.getResourceId());
                pstmt.setString(2, schedule.getDayOfWeek());
                pstmt.setInt(3, schedule.getScheduleId());
                pstmt.setString(4, schedule.getStartTime());
                pstmt.setString(5, schedule.getEndTime());
                pstmt.setInt(6, resource.getResourceId());

                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    int availableQuantity = rs.getInt("available_quantity");
                    if (availableQuantity < resource.getQuantityNeeded()) {
                        return new ValidationResult(false,
                                "Insufficient resources available. Need " + resource.getQuantityNeeded() +
                                        " but only " + availableQuantity + " available.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new ValidationResult(false, "Database error checking resource availability: " + e.getMessage());
        }
        return new ValidationResult(true, "Resources are available");
    }

    private static ValidationResult checkCreditHours(Schedule schedule) {
        try {
            Course course = CourseDAO.getCourseById(schedule.getCourseId());
            if (course == null) {
                return new ValidationResult(false, "Course not found");
            }

            // Calculate total scheduled minutes for this course in this room
            List<Schedule> existingSchedules = ScheduleDAO.getSchedulesByCourseId(schedule.getCourseId());
            int totalMinutes = 0;

            // Add minutes from existing schedules in the same room
            for (Schedule existing : existingSchedules) {
                if (existing.getScheduleId() != schedule.getScheduleId() &&
                        existing.getRoom().equals(schedule.getRoom())) {
                    LocalTime start = LocalTime.parse(existing.getStartTime());
                    LocalTime end = LocalTime.parse(existing.getEndTime());
                    totalMinutes += (int) ChronoUnit.MINUTES.between(start, end);
                }
            }

            // Add minutes from new schedule
            LocalTime start = LocalTime.parse(schedule.getStartTime());
            LocalTime end = LocalTime.parse(schedule.getEndTime());
            totalMinutes += (int) ChronoUnit.MINUTES.between(start, end);

            // Check if total minutes exceed credit hours (1 credit = 50 minutes) for this
            // room
            int maxMinutes = course.getCreditHours() * 50;
            if (totalMinutes > maxMinutes) {
                return new ValidationResult(false,
                        "Schedule exceeds course credit hours for room " + schedule.getRoom() +
                                ". Maximum allowed: " + maxMinutes + " minutes per room, " +
                                "Scheduled: " + totalMinutes + " minutes");
            }

            System.out.println("Credit hours check for room " + schedule.getRoom() + ": " +
                    totalMinutes + " minutes out of " + maxMinutes + " maximum minutes");
        } catch (Exception e) {
            return new ValidationResult(false, "Error checking credit hours: " + e.getMessage());
        }
        return new ValidationResult(true, "Credit hours check passed");
    }
}