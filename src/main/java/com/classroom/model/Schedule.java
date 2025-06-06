package com.classroom.model;

import java.util.List;
import java.util.ArrayList;

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
    private String status; // "draft", "published", "revision_requested"
    private String publishDate;

    // Additional fields for display purposes (not in database)
    private String courseName;
    private String courseCode;
    private String instructorName;

    // Required resources for this schedule
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
        this.status = "draft"; // Default status
        this.requiredResources = new ArrayList<>();
    }

    // Default constructor
    public Schedule() {
        this.requiredResources = new ArrayList<>();
        this.status = "draft"; // Default status
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

    public String getStatus() {
        return status != null ? status : "draft"; // Default to draft if null
    }

    public void setStatus(String status) {
        if (status != null
                && (status.equals("draft") || status.equals("published") || status.equals("revision_requested"))) {
            this.status = status;
        } else {
            this.status = "draft"; // Default to draft for invalid values
        }
    }

    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public boolean isDraft() {
        return "draft".equals(getStatus());
    }

    public boolean isPublished() {
        return "published".equals(getStatus());
    }

    public boolean isRevisionRequested() {
        return "revision_requested".equals(getStatus());
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

    public void addRequiredResource(ScheduleResource resource) {
        if (this.requiredResources == null) {
            this.requiredResources = new ArrayList<>();
        }
        this.requiredResources.add(resource);
    }
}