package com.classroom.ui;

// Add these imports at the top of the file
import com.classroom.model.User;
import com.classroom.model.Course;
import com.classroom.dao.CourseDAO;
import com.classroom.util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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

        JButton logoutButton = new JButton("Logout");
        UIUtils.styleButton(logoutButton, Color.WHITE);
        logoutButton.setForeground(ColorScheme.PRIMARY);
        logoutButton.addActionListener(e -> logout());

        userInfoPanel.add(roomLabel);
        userInfoPanel.add(Box.createHorizontalStrut(20));
        userInfoPanel.add(userLabel);
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

        List<Course> allCourses = CourseDAO.getAllCourses();
        for (Course course : allCourses) {
            if (!EnrollmentDAO.isStudentEnrolled(currentUser.getUserId(), course.getCourseId())) {
                JPanel coursePanel = new JPanel(new BorderLayout());
                coursePanel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(ColorScheme.LIGHT_ACCENT),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)));

                JPanel infoPanel = new JPanel(new GridLayout(2, 1));
                infoPanel.add(new JLabel(course.getCourseCode() + " - " + course.getCourseName()));
                infoPanel.add(new JLabel(course.getDescription()));

                JButton enrollButton = new JButton("Enroll");
                UIUtils.styleButton(enrollButton, ColorScheme.PRIMARY);
                enrollButton.addActionListener(e -> {
                    if (EnrollmentDAO.enrollStudent(currentUser.getUserId(), course.getCourseId())) {
                        JOptionPane.showMessageDialog(dialog,
                                "Successfully enrolled in " + course.getCourseName(),
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                        refreshCoursesPanel();
                    } else {
                        JOptionPane.showMessageDialog(dialog,
                                "Failed to enroll in the course",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });

                coursePanel.add(infoPanel, BorderLayout.CENTER);
                coursePanel.add(enrollButton, BorderLayout.EAST);
                contentPanel.add(coursePanel);
                contentPanel.add(Box.createVerticalStrut(10));
            }
        }

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void refreshCoursesPanel() {
        List<Course> availableCourses = new ArrayList<>();

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement("SELECT assigned_room FROM Users WHERE user_id = ?")) {
            stmt.setInt(1, currentUser.getUserId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String assignedRoom = rs.getString("assigned_room");
                if (assignedRoom != null) {
                    availableCourses = CourseDAO.getCoursesByRoom(assignedRoom);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        coursesPanel.removeAll();
        for (Course course : availableCourses) {
            coursesPanel.add(createCourseCard(course));
        }
        coursesPanel.revalidate();
        coursesPanel.repaint();
    }

    private JPanel createCourseCard(Course course) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ColorScheme.LIGHT_ACCENT, 1),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JLabel codeLabel = new JLabel(course.getCourseCode());
        codeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        codeLabel.setForeground(ColorScheme.PRIMARY);

        JLabel nameLabel = new JLabel(course.getCourseName());
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        JLabel descLabel = new JLabel(course.getDescription());
        descLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        descLabel.setForeground(Color.DARK_GRAY);

        JPanel infoPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        infoPanel.setOpaque(false);
        infoPanel.add(codeLabel);
        infoPanel.add(nameLabel);
        infoPanel.add(descLabel);

        card.add(infoPanel, BorderLayout.CENTER);

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
