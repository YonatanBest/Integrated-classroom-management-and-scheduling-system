package com.classroom.model;

import java.util.List;

/**
 * Represents a course schedule in the system.
 */
public class Schedule {
    private int scheduleId;
    private int courseId;
    private int instructorId;
    private String dayOfWeek;
    private String startTime;
    private String endTime;
    private String room;
    private String programType; // "regular" or "evening"

    // Additional fields for display purposes (not in database)
    private String courseName;
    private String courseCode;
    private String instructorName;

    // Add new fields for resource tracking
    private List<ScheduleResource> requiredResources;

    // Constructor
    public Schedule(int scheduleId, int courseId, int instructorId, String dayOfWeek,
            String startTime, String endTime, String room, String programType) {
        this.scheduleId = scheduleId;
        this.courseId = courseId;
        this.instructorId = instructorId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
        this.programType = programType;
    }

    // Default constructor
    public Schedule() {
    }

    // Getters and Setters
    public int getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(int scheduleId) {
        this.scheduleId = scheduleId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public int getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(int instructorId) {
        this.instructorId = instructorId;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getProgramType() {
        return programType;
    }

    public void setProgramType(String programType) {
        this.programType = programType;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getInstructorName() {
        return instructorName;
    }

    public void setInstructorName(String instructorName) {
        this.instructorName = instructorName;
    }

    @Override
    public String toString() {
        return courseCode + " - " + startTime + " to " + endTime + " in " + room;
    }

    public List<ScheduleResource> getRequiredResources() {
        return requiredResources;
    }

    public void setRequiredResources(List<ScheduleResource> requiredResources) {
        this.requiredResources = requiredResources;
    }
}