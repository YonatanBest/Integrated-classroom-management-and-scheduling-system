package com.classroom.model;

/**
 * Represents a course in the system.
 */
public class Course {
    private int courseId;
    private String courseName;
    private String courseCode;
    private String description;
    private int creditHours;

    // Constructor
    public Course(int courseId, String courseName, String courseCode, String description, int creditHours) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.courseCode = courseCode;
        this.description = description;
        this.creditHours = creditHours;
    }

    // Default constructor
    public Course() {
    }

    // Add getter and setter for creditHours
    public int getCreditHours() {
        return creditHours;
    }

    public void setCreditHours(int creditHours) {
        this.creditHours = creditHours;
    }

    // Getters and Setters
    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return courseCode + " - " + courseName;
    }
}