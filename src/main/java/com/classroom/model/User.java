package com.classroom.model;

/**
 * Represents a user in the system.
 */
public class User {
    private int userId;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String userType; // "student", "instructor", "coordinator"
    private String programType; // "regular", "evening" (for students)
    private String assignedRoom; // For students

    // Constructor
    public User(int userId, String username, String password, String fullName,
            String email, String userType, String programType) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.userType = userType;
        this.programType = programType;
    }

    // Default constructor
    public User() {
    }

    // Update constructor
    public User(int userId, String username, String password, String fullName,
            String email, String userType, String programType, String assignedRoom) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.userType = userType;
        this.programType = programType;
        this.assignedRoom = assignedRoom;
    }

    // Add getter and setter
    public String getAssignedRoom() {
        return assignedRoom;
    }

    public void setAssignedRoom(String assignedRoom) {
        this.assignedRoom = assignedRoom;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getProgramType() {
        return programType;
    }

    public void setProgramType(String programType) {
        this.programType = programType;
    }

    public boolean isCoordinator() {
        return "coordinator".equals(userType);
    }

    public boolean isInstructor() {
        return "instructor".equals(userType);
    }

    public boolean isStudent() {
        return "student".equals(userType);
    }

    @Override
    public String toString() {
        return fullName + " (" + email + ")";
    }
}