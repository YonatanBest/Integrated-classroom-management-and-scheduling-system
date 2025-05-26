package com.classroom.ui;

import com.classroom.model.User;
import com.classroom.model.Course;
import com.classroom.dao.CourseDAO;
import com.classroom.dao.ScheduleDAO;
import com.classroom.dao.UserDAO;
import com.classroom.util.DatabaseUtil;
import com.classroom.ui.components.MakeupRequestPanel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.classroom.dao.EnrollmentDAO;
import com.classroom.ui.components.CalendarPanel;
import com.classroom.util.ColorScheme;
import com.classroom.util.UIUtils;

import javax.swing.*;
import java.awt.*;

/**
 * Dashboard for student users.
 */
public class StudentDashboard extends JFrame {
    private final User currentUser;
    private JTabbedPane tabbedPane;
    private CalendarPanel calendarPanel;
    private JPanel coursesPanel;
    private JPanel profilePanel;
    private JLabel roomLabel; // Add this field

    public StudentDashboard(User user) {
        this.currentUser = user;

        setTitle("Student Dashboard - " + user.getFullName());
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        // Create header panel
        JPanel headerPanel = createHeaderPanel();

        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.setBackground(Color.WHITE);

        // Create tabs
        calendarPanel = new CalendarPanel(currentUser);
        coursesPanel = createCoursesPanel();
        profilePanel = createProfilePanel();

        tabbedPane.addTab("Schedule", new ImageIcon(), new JScrollPane(calendarPanel), "View your weekly schedule");
        tabbedPane.addTab("My Courses", new ImageIcon(), new JScrollPane(coursesPanel), "View your enrolled courses");

        // Add Makeup tab if user is a room representative
        if (UserDAO.isRoomRep(currentUser.getUserId())) {
            tabbedPane.addTab("Makeup", new ImageIcon(), new MakeupRequestPanel(currentUser), "Request makeup classes");
        }

        tabbedPane.addTab("Profile", new ImageIcon(), new JScrollPane(profilePanel), "View and edit your profile");

        // Add components to main panel
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Set content pane
        setContentPane(mainPanel);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ColorScheme.PRIMARY);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("Classroom Management System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        JPanel userInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userInfoPanel.setOpaque(false);

        // Add room information
        roomLabel = new JLabel();
        roomLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        roomLabel.setForeground(Color.WHITE);
        updateRoomLabel();

        JLabel userLabel = new JLabel(currentUser.getFullName() + " (" + currentUser.getProgramType() + " Student)");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userLabel.setForeground(Color.WHITE);

        // Add refresh button
        JButton refreshButton = new JButton("Refresh");
        UIUtils.styleButton(refreshButton, ColorScheme.SECONDARY);
        refreshButton.addActionListener(e -> refreshAll());

        JButton logoutButton = new JButton("Logout");
        UIUtils.styleButton(logoutButton, Color.WHITE);
        logoutButton.setForeground(ColorScheme.PRIMARY);
        logoutButton.addActionListener(e -> logout());

        userInfoPanel.add(roomLabel);
        userInfoPanel.add(Box.createHorizontalStrut(20));
        userInfoPanel.add(userLabel);
        userInfoPanel.add(Box.createHorizontalStrut(10));
        userInfoPanel.add(refreshButton);
        userInfoPanel.add(Box.createHorizontalStrut(10));
        userInfoPanel.add(logoutButton);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(userInfoPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create header panel with title and enroll button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("My Courses");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(ColorScheme.PRIMARY);

        JButton enrollButton = new JButton("Enroll in Courses");
        UIUtils.styleButton(enrollButton, ColorScheme.PRIMARY);
        enrollButton.addActionListener(e -> showEnrollmentDialog());

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(enrollButton, BorderLayout.EAST);

        List<Course> courses = CourseDAO.getCoursesByStudentId(currentUser.getUserId());

        JPanel coursesListPanel = new JPanel();
        coursesListPanel.setLayout(new BoxLayout(coursesListPanel, BoxLayout.Y_AXIS));
        coursesListPanel.setBackground(Color.WHITE);

        if (courses.isEmpty()) {
            JLabel noCoursesLabel = new JLabel("You are not enrolled in any courses yet.");
            noCoursesLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            noCoursesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            coursesListPanel.add(noCoursesLabel);
        } else {
            for (Course course : courses) {
                JPanel courseCard = createCourseCard(course);
                coursesListPanel.add(courseCard);
                coursesListPanel.add(Box.createVerticalStrut(10));
            }
        }

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(coursesListPanel), BorderLayout.CENTER);

        return panel;
    }

    private void showEnrollmentDialog() {
        JDialog dialog = new JDialog(this, "Available Courses", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Get student's assigned room
        String assignedRoom = currentUser.getAssignedRoom();
        String programType = currentUser.getProgramType();

        System.out.println("\nStudent Info:");
        System.out.println("- Assigned Room: " + assignedRoom);
        System.out.println("- Program Type: " + programType);

        if (assignedRoom == null || assignedRoom.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "You need to be assigned to a room before you can enroll in courses.\nPlease contact your coordinator.",
                    "No Room Assigned",
                    JOptionPane.WARNING_MESSAGE);
            dialog.dispose();
            return;
        }

        // Debug: Print schedule details for the room
        ScheduleDAO.debugRoomSchedule(assignedRoom);

        // Ensure program type is properly capitalized
        if (programType != null) {
            programType = programType.substring(0, 1).toUpperCase() + programType.substring(1).toLowerCase();
        }

        // Get courses available in the student's assigned room and program type
        List<Course> availableCourses = CourseDAO.getCoursesByRoomAndProgramType(assignedRoom, programType);

        System.out.println("Found " + availableCourses.size() + " available courses");

        if (availableCourses.isEmpty()) {
            String message = String.format("No courses are currently available in room %s for %s program.",
                    assignedRoom, programType);
            JLabel noCoursesLabel = new JLabel(message);
            noCoursesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            contentPanel.add(noCoursesLabel);

            // Add a debug panel
            JPanel debugPanel = new JPanel();
            debugPanel.setLayout(new BoxLayout(debugPanel, BoxLayout.Y_AXIS));
            debugPanel.setBorder(BorderFactory.createTitledBorder("Debug Information"));
            debugPanel.add(new JLabel("Room: " + assignedRoom));
            debugPanel.add(new JLabel("Program Type: " + programType));
            debugPanel.add(new JLabel("Student ID: " + currentUser.getUserId()));

            // Add a refresh button
            JButton refreshButton = new JButton("Refresh Available Courses");
            UIUtils.styleButton(refreshButton, ColorScheme.PRIMARY);
            refreshButton.addActionListener(e -> {
                dialog.dispose();
                showEnrollmentDialog();
            });
            debugPanel.add(Box.createVerticalStrut(10));
            debugPanel.add(refreshButton);

            contentPanel.add(Box.createVerticalStrut(20));
            contentPanel.add(debugPanel);
        } else {
            for (Course course : availableCourses) {
                if (!EnrollmentDAO.isStudentEnrolled(currentUser.getUserId(), course.getCourseId())) {
                    JPanel coursePanel = new JPanel(new BorderLayout());
                    coursePanel.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(ColorScheme.SECONDARY),
                            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
                    coursePanel.setBackground(Color.WHITE);

                    JPanel infoPanel = new JPanel(new GridLayout(3, 1, 5, 5));
                    infoPanel.setBackground(Color.WHITE);

                    JLabel nameLabel = new JLabel(course.getCourseCode() + " - " + course.getCourseName());
                    nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

                    JLabel creditsLabel = new JLabel("Credits: " + course.getCreditHours());
                    JLabel descLabel = new JLabel(course.getDescription());

                    infoPanel.add(nameLabel);
                    infoPanel.add(creditsLabel);
                    infoPanel.add(descLabel);

                    JButton enrollButton = new JButton("Enroll");
                    UIUtils.styleButton(enrollButton, ColorScheme.PRIMARY);
                    enrollButton.addActionListener(e -> {
                        int result = EnrollmentDAO.enrollStudent(currentUser.getUserId(), course.getCourseId());
                        switch (result) {
                            case 1:
                                JOptionPane.showMessageDialog(dialog,
                                        "Successfully enrolled in " + course.getCourseName(),
                                        "Success",
                                        JOptionPane.INFORMATION_MESSAGE);
                                dialog.dispose();
                                refreshCoursesPanel();
                                calendarPanel.refreshCalendar();
                                break;
                            case 0:
                                JOptionPane.showMessageDialog(dialog,
                                        "You are already enrolled in this course",
                                        "Already Enrolled",
                                        JOptionPane.INFORMATION_MESSAGE);
                                break;
                            case -1:
                                JOptionPane.showMessageDialog(dialog,
                                        "You need to be assigned to a room before enrolling in courses.\nPlease contact your coordinator.",
                                        "No Room Assigned",
                                        JOptionPane.WARNING_MESSAGE);
                                break;
                            case -2:
                                JOptionPane.showMessageDialog(dialog,
                                        "This course is not available in your assigned room or program type",
                                        "Course Not Available",
                                        JOptionPane.WARNING_MESSAGE);
                                break;
                            default:
                                JOptionPane.showMessageDialog(dialog,
                                        "Failed to enroll in the course. Please try again later.",
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE);
                        }
                    });

                    coursePanel.add(infoPanel, BorderLayout.CENTER);
                    coursePanel.add(enrollButton, BorderLayout.EAST);
                    contentPanel.add(coursePanel);
                    contentPanel.add(Box.createVerticalStrut(10));
                }
            }
        }

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void refreshCoursesPanel() {
        // Remove all existing components
        coursesPanel.removeAll();

        // Create header panel with title and enroll button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("My Courses");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(ColorScheme.PRIMARY);

        JButton enrollButton = new JButton("Enroll in Courses");
        UIUtils.styleButton(enrollButton, ColorScheme.PRIMARY);
        enrollButton.addActionListener(e -> showEnrollmentDialog());

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(enrollButton, BorderLayout.EAST);

        // Create courses list panel
        JPanel coursesListPanel = new JPanel();
        coursesListPanel.setLayout(new BoxLayout(coursesListPanel, BoxLayout.Y_AXIS));
        coursesListPanel.setBackground(Color.WHITE);
        coursesListPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        // Get enrolled courses
        List<Course> enrolledCourses = CourseDAO.getCoursesByStudentId(currentUser.getUserId());

        if (enrolledCourses.isEmpty()) {
            JLabel noCoursesLabel = new JLabel("You are not enrolled in any courses yet.");
            noCoursesLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            noCoursesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            coursesListPanel.add(noCoursesLabel);
        } else {
            for (Course course : enrolledCourses) {
                JPanel courseCard = createCourseCard(course);
                coursesListPanel.add(courseCard);
                coursesListPanel.add(Box.createVerticalStrut(10));
            }
        }

        // Add components to the main panel
        coursesPanel.setLayout(new BorderLayout());
        coursesPanel.add(headerPanel, BorderLayout.NORTH);
        coursesPanel.add(new JScrollPane(coursesListPanel), BorderLayout.CENTER);

        // Refresh the panel
        coursesPanel.revalidate();
        coursesPanel.repaint();
    }

    private JPanel createCourseCard(Course course) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorScheme.SECONDARY, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        card.setBackground(Color.WHITE);

        JLabel nameLabel = new JLabel(course.getCourseName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel codeLabel = new JLabel("Code: " + course.getCourseCode());
        codeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel creditsLabel = new JLabel("Credits: " + course.getCreditHours());
        creditsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton enrollButton = new JButton("Enroll");
        UIUtils.styleButton(enrollButton, ColorScheme.PRIMARY);
        enrollButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Check if student is already enrolled
        boolean isEnrolled = EnrollmentDAO.isStudentEnrolled(currentUser.getUserId(), course.getCourseId());
        if (isEnrolled) {
            enrollButton.setText("Enrolled");
            enrollButton.setEnabled(false);
        } else {
            enrollButton.addActionListener(e -> {
                int result = EnrollmentDAO.enrollStudent(currentUser.getUserId(), course.getCourseId());
                switch (result) {
                    case 1:
                        JOptionPane.showMessageDialog(this,
                                "Successfully enrolled in " + course.getCourseName(),
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        enrollButton.setText("Enrolled");
                        enrollButton.setEnabled(false);
                        calendarPanel.refreshCalendar();
                        break;
                    case 0:
                        JOptionPane.showMessageDialog(this,
                                "You are already enrolled in this course",
                                "Already Enrolled",
                                JOptionPane.INFORMATION_MESSAGE);
                        break;
                    case -1:
                        JOptionPane.showMessageDialog(this,
                                "You need to be assigned to a room before enrolling in courses.\nPlease contact your coordinator.",
                                "No Room Assigned",
                                JOptionPane.WARNING_MESSAGE);
                        break;
                    case -2:
                        JOptionPane.showMessageDialog(this,
                                "This course is not available in your assigned room or program type",
                                "Course Not Available",
                                JOptionPane.WARNING_MESSAGE);
                        break;
                    default:
                        JOptionPane.showMessageDialog(this,
                                "Failed to enroll in the course. Please try again later.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                }
            });
        }

        card.add(nameLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(codeLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(creditsLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(enrollButton);

        return card;
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("My Profile");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(ColorScheme.PRIMARY);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // User information fields
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel userInfoLabel = new JLabel("User Information");
        userInfoLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formPanel.add(userInfoLabel, gbc);

        // Username
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        JLabel usernameLabel = new JLabel("Username:");
        formPanel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        JLabel usernameValue = new JLabel(currentUser.getUsername());
        usernameValue.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(usernameValue, gbc);

        // Full Name
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel nameLabel = new JLabel("Full Name:");
        formPanel.add(nameLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        JLabel nameValue = new JLabel(currentUser.getFullName());
        nameValue.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(nameValue, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel emailLabel = new JLabel("Email:");
        formPanel.add(emailLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        JLabel emailValue = new JLabel(currentUser.getEmail());
        emailValue.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(emailValue, gbc);

        // User Type
        gbc.gridx = 0;
        gbc.gridy = 4;
        JLabel typeLabel = new JLabel("User Type:");
        formPanel.add(typeLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        JLabel typeValue = new JLabel(currentUser.getUserType().substring(0, 1).toUpperCase() +
                currentUser.getUserType().substring(1));
        typeValue.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(typeValue, gbc);

        // Program Type
        gbc.gridx = 0;
        gbc.gridy = 5;
        JLabel programLabel = new JLabel("Program Type:");
        formPanel.add(programLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 5;
        JLabel programValue = new JLabel(currentUser.getProgramType().substring(0, 1).toUpperCase() +
                currentUser.getProgramType().substring(1));
        programValue.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        formPanel.add(programValue, gbc);

        // Change Password section
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 8, 8, 8);
        JLabel passwordSectionLabel = new JLabel("Change Password");
        passwordSectionLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formPanel.add(passwordSectionLabel, gbc);

        gbc.insets = new Insets(8, 8, 8, 8);

        // Current Password
        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 1;
        JLabel currentPasswordLabel = new JLabel("Current Password:");
        formPanel.add(currentPasswordLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 8;
        JPasswordField currentPasswordField = new JPasswordField(15);
        UIUtils.styleTextField(currentPasswordField);
        formPanel.add(currentPasswordField, gbc);

        // New Password
        gbc.gridx = 0;
        gbc.gridy = 9;
        JLabel newPasswordLabel = new JLabel("New Password:");
        formPanel.add(newPasswordLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 9;
        JPasswordField newPasswordField = new JPasswordField(15);
        UIUtils.styleTextField(newPasswordField);
        formPanel.add(newPasswordField, gbc);

        // Confirm New Password
        gbc.gridx = 0;
        gbc.gridy = 10;
        JLabel confirmPasswordLabel = new JLabel("Confirm New Password:");
        formPanel.add(confirmPasswordLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 10;
        JPasswordField confirmPasswordField = new JPasswordField(15);
        UIUtils.styleTextField(confirmPasswordField);
        formPanel.add(confirmPasswordField, gbc);

        // Update Button
        gbc.gridx = 1;
        gbc.gridy = 11;
        gbc.anchor = GridBagConstraints.EAST;
        JButton updateButton = new JButton("Update Password");
        UIUtils.styleButton(updateButton, ColorScheme.PRIMARY);
        formPanel.add(updateButton, gbc);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);

        return panel;
    }

    private void logout() {
        int option = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            this.dispose();
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        }
    }

    private void updateRoomLabel() {
        String assignedRoom = currentUser.getAssignedRoom();
        roomLabel.setText(
                assignedRoom != null && !assignedRoom.isEmpty() ? "Room: " + assignedRoom : "No Room Assigned");
    }

    // Add this method to refresh all components
    public void refreshAll() {
        // Refresh user data from database
        try (Connection conn = DatabaseUtil.getConnection()) {
            String sql = "SELECT * FROM Users WHERE user_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, currentUser.getUserId());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                currentUser.setAssignedRoom(rs.getString("assigned_room"));
                currentUser.setProgramType(rs.getString("program_type"));
            }
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Update room label
        updateRoomLabel();

        // Refresh calendar
        calendarPanel.refreshCalendar();

        // Refresh courses panel
        refreshCoursesPanel();

        // Refresh the entire frame
        revalidate();
        repaint();
    }

    // Add this method to refresh the dashboard
    public void refreshDashboard() {
        // Refresh room label
        updateRoomLabel();

        // Refresh calendar with new room filter
        if (calendarPanel != null) {
            calendarPanel.setRoomFilter(currentUser.getAssignedRoom());
            calendarPanel.refreshCalendar();
        }
    }
}
