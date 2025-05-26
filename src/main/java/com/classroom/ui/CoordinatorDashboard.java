package com.classroom.ui;

import com.classroom.dao.CourseDAO;
import com.classroom.dao.ScheduleDAO;
import com.classroom.dao.UserDAO;
import com.classroom.model.Course;
import com.classroom.model.Schedule;
import com.classroom.model.User;
import com.classroom.ui.components.CalendarPanel;
import com.classroom.ui.components.RepresentativeManagementPanel;
import com.classroom.util.ColorScheme;
import com.classroom.util.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Dashboard for coordinator/instructor users.
 */
public class CoordinatorDashboard extends JFrame implements ActionListener {
    private User currentUser;
    private JTabbedPane tabbedPane;
    private CalendarPanel calendarPanel;
    private JPanel coursesPanel;
    private JPanel studentsPanel;
    private JPanel schedulePanel;
    private JPanel profilePanel;

    // Schedule management components
    private JTable schedulesTable;
    private DefaultTableModel schedulesTableModel;
    private JButton addScheduleButton;
    private JButton editScheduleButton;
    private JButton deleteScheduleButton;

    // Course management components
    private JTable coursesTable;
    private DefaultTableModel coursesTableModel;
    private JButton addCourseButton;
    private JButton refreshCoursesButton;

    // Student management components
    private JTable studentsTable;
    private DefaultTableModel studentsTableModel;
    private JComboBox<Course> courseFilterCombo;

    public CoordinatorDashboard(User user) {
        this.currentUser = user;

        setTitle("Coordinator Dashboard - " + user.getFullName());
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        loadSchedulesData();
        loadCoursesData();
        loadStudentsData();
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
        schedulePanel = createScheduleManagementPanel();
        coursesPanel = createCoursesManagementPanel();
        studentsPanel = createStudentsManagementPanel();
        profilePanel = createProfilePanel();

        tabbedPane.addTab("Schedule Overview", new ImageIcon(), new JScrollPane(calendarPanel),
                "View overall schedule");
        tabbedPane.addTab("Manage Schedules", new ImageIcon(), schedulePanel, "Add, edit or delete schedules");
        tabbedPane.addTab("Manage Courses", new ImageIcon(), coursesPanel, "Add and view courses");
        tabbedPane.addTab("Students", new ImageIcon(), studentsPanel, "View students by course");
        tabbedPane.addTab("Representatives", new ImageIcon(), new RepresentativeManagementPanel(),
                "Manage room representatives");
        tabbedPane.addTab("Profile", new ImageIcon(), profilePanel, "View and edit your profile");

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

        JLabel userLabel = new JLabel(currentUser.getFullName() + " (" + currentUser.getUserType() + ")");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userLabel.setForeground(Color.WHITE);

        JButton logoutButton = new JButton("Logout");
        UIUtils.styleButton(logoutButton, Color.WHITE);
        logoutButton.setForeground(ColorScheme.PRIMARY);
        logoutButton.addActionListener(e -> logout());

        userInfoPanel.add(userLabel);
        userInfoPanel.add(Box.createHorizontalStrut(10));
        userInfoPanel.add(logoutButton);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(userInfoPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createScheduleManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Schedule Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(ColorScheme.PRIMARY);

        // Create table model
        schedulesTableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        schedulesTableModel.addColumn("ID");
        schedulesTableModel.addColumn("Course");
        schedulesTableModel.addColumn("Day");
        schedulesTableModel.addColumn("Start Time");
        schedulesTableModel.addColumn("End Time");
        schedulesTableModel.addColumn("Room");
        schedulesTableModel.addColumn("Program Type");
        schedulesTableModel.addColumn("Instructor");

        // Create table
        schedulesTable = new JTable(schedulesTableModel);
        schedulesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        schedulesTable.getTableHeader().setReorderingAllowed(false);
        schedulesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        schedulesTable.setRowHeight(25);
        schedulesTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Column widths
        schedulesTable.getColumnModel().getColumn(0).setMaxWidth(50);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonsPanel.setBackground(Color.WHITE);

        addScheduleButton = new JButton("Add Schedule");
        editScheduleButton = new JButton("Edit Schedule");
        deleteScheduleButton = new JButton("Delete Schedule");

        UIUtils.styleButton(addScheduleButton, ColorScheme.PRIMARY);
        UIUtils.styleButton(editScheduleButton, ColorScheme.SECONDARY);
        UIUtils.styleButton(deleteScheduleButton, ColorScheme.ACCENT);

        addScheduleButton.addActionListener(this);
        editScheduleButton.addActionListener(this);
        deleteScheduleButton.addActionListener(this);

        buttonsPanel.add(addScheduleButton);
        buttonsPanel.add(editScheduleButton);
        buttonsPanel.add(deleteScheduleButton);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(new JScrollPane(schedulesTable), BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createCoursesManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Course Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(ColorScheme.PRIMARY);

        // Create table model
        coursesTableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        coursesTableModel.addColumn("ID");
        coursesTableModel.addColumn("Course Code");
        coursesTableModel.addColumn("Course Name");
        coursesTableModel.addColumn("Description");
        coursesTableModel.addColumn("Credit Hours");

        // Create table
        coursesTable = new JTable(coursesTableModel);
        coursesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        coursesTable.getTableHeader().setReorderingAllowed(false);
        coursesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        coursesTable.setRowHeight(25);
        coursesTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Column widths
        coursesTable.getColumnModel().getColumn(0).setMaxWidth(50);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonsPanel.setBackground(Color.WHITE);

        addCourseButton = new JButton("Add Course");
        refreshCoursesButton = new JButton("Refresh");

        UIUtils.styleButton(addCourseButton, ColorScheme.PRIMARY);
        UIUtils.styleButton(refreshCoursesButton, ColorScheme.SECONDARY);

        addCourseButton.addActionListener(this);
        refreshCoursesButton.addActionListener(this);

        buttonsPanel.add(addCourseButton);
        buttonsPanel.add(refreshCoursesButton);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(new JScrollPane(coursesTable), BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createStudentsManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Students by Course");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(ColorScheme.PRIMARY);

        // Create course filter
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterPanel.setBackground(Color.WHITE);

        JLabel filterLabel = new JLabel("Filter by Course:");
        courseFilterCombo = new JComboBox<>();
        courseFilterCombo.addItem(new Course(0, "All Courses", "", "", 0));
        List<Course> courses = CourseDAO.getAllCourses();
        for (Course course : courses) {
            courseFilterCombo.addItem(course);
        }

        courseFilterCombo.addActionListener(e -> {
            Course selectedCourse = (Course) courseFilterCombo.getSelectedItem();
            if (selectedCourse != null) {
                if (selectedCourse.getCourseId() == 0) {
                    loadStudentsData(); // Load all students
                } else {
                    loadStudentsByCourse(selectedCourse.getCourseId());
                }
            }
        });

        // Add Assign Room button
        JButton assignRoomButton = new JButton("Assign Room");
        UIUtils.styleButton(assignRoomButton, ColorScheme.PRIMARY);
        assignRoomButton.addActionListener(e -> {
            int selectedRow = studentsTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this,
                        "Please select a student to assign a room",
                        "No Selection",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            int studentId = (int) studentsTable.getValueAt(selectedRow, 0);
            String currentRoom = (String) studentsTable.getValueAt(selectedRow, 4);
            if (currentRoom.equals("Not Assigned")) {
                currentRoom = "";
            }

            // Get list of available rooms
            List<String> rooms = ScheduleDAO.getAllRooms();
            JComboBox<String> roomCombo = new JComboBox<>(rooms.toArray(new String[0]));
            if (!currentRoom.isEmpty()) {
                roomCombo.setSelectedItem(currentRoom);
            }

            int result = JOptionPane.showConfirmDialog(this,
                    new Object[] {
                            "Select room for student:",
                            roomCombo
                    },
                    "Assign Room",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                String selectedRoom = (String) roomCombo.getSelectedItem();
                if (selectedRoom != null && !selectedRoom.isEmpty()) {
                    assignRoomToStudent(studentId, selectedRoom);
                }
            }
        });

        filterPanel.add(filterLabel);
        filterPanel.add(courseFilterCombo);
        filterPanel.add(Box.createHorizontalStrut(10));
        filterPanel.add(assignRoomButton);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(filterPanel, BorderLayout.EAST);

        // Create table model
        studentsTableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        studentsTableModel.addColumn("ID");
        studentsTableModel.addColumn("Name");
        studentsTableModel.addColumn("Email");
        studentsTableModel.addColumn("Program Type");
        studentsTableModel.addColumn("Assigned Room");

        // Create table
        studentsTable = new JTable(studentsTableModel);
        studentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentsTable.getTableHeader().setReorderingAllowed(false);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(studentsTable), BorderLayout.CENTER);

        return panel;
    }

    private void loadStudentsData() {
        studentsTableModel.setRowCount(0);
        List<User> students = UserDAO.getAllStudents();

        for (User student : students) {
            studentsTableModel.addRow(new Object[] {
                    student.getUserId(),
                    student.getFullName(),
                    student.getEmail(),
                    student.getProgramType(),
                    student.getAssignedRoom() != null ? student.getAssignedRoom() : "Not Assigned"
            });
        }
    }

    private void loadStudentsByCourse(int courseId) {
        studentsTableModel.setRowCount(0);
        List<User> students = UserDAO.getStudentsByCourse(courseId);

        for (User student : students) {
            studentsTableModel.addRow(new Object[] {
                    student.getUserId(),
                    student.getFullName(),
                    student.getEmail(),
                    student.getProgramType(),
                    student.getAssignedRoom() != null ? student.getAssignedRoom() : "Not Assigned"
            });
        }
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

        // Change Password section
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 8, 8, 8);
        JLabel passwordSectionLabel = new JLabel("Change Password");
        passwordSectionLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formPanel.add(passwordSectionLabel, gbc);

        gbc.insets = new Insets(8, 8, 8, 8);

        // Current Password
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 1;
        JLabel currentPasswordLabel = new JLabel("Current Password:");
        formPanel.add(currentPasswordLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 7;
        JPasswordField currentPasswordField = new JPasswordField(15);
        UIUtils.styleTextField(currentPasswordField);
        formPanel.add(currentPasswordField, gbc);

        // New Password
        gbc.gridx = 0;
        gbc.gridy = 8;
        JLabel newPasswordLabel = new JLabel("New Password:");
        formPanel.add(newPasswordLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 8;
        JPasswordField newPasswordField = new JPasswordField(15);
        UIUtils.styleTextField(newPasswordField);
        formPanel.add(newPasswordField, gbc);

        // Confirm New Password
        gbc.gridx = 0;
        gbc.gridy = 9;
        JLabel confirmPasswordLabel = new JLabel("Confirm New Password:");
        formPanel.add(confirmPasswordLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 9;
        JPasswordField confirmPasswordField = new JPasswordField(15);
        UIUtils.styleTextField(confirmPasswordField);
        formPanel.add(confirmPasswordField, gbc);

        // Update Button
        gbc.gridx = 1;
        gbc.gridy = 10;
        gbc.anchor = GridBagConstraints.EAST;
        JButton updateButton = new JButton("Update Password");
        UIUtils.styleButton(updateButton, ColorScheme.PRIMARY);
        formPanel.add(updateButton, gbc);

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);

        return panel;
    }

    private void assignRoomToStudent(int studentId, String room) {
        if (UserDAO.assignRoom(studentId, room)) {
            // Update the student in memory
            for (int i = 0; i < studentsTableModel.getRowCount(); i++) {
                if ((int) studentsTableModel.getValueAt(i, 0) == studentId) {
                    studentsTableModel.setValueAt(room, i, 4); // Update room column
                    break;
                }
            }

            // Update the user object if it exists
            User student = UserDAO.getUserById(studentId);
            if (student != null) {
                student.setAssignedRoom(room);
            }

            // Refresh the calendar view
            calendarPanel.refreshCalendar();

            JOptionPane.showMessageDialog(this,
                    "Room assigned successfully",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Failed to assign room",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSchedulesData() {
        // Clear table
        schedulesTableModel.setRowCount(0);

        // Get all schedules
        List<Schedule> schedules = ScheduleDAO.getAllSchedulesWithDetails();

        // Add to table
        for (Schedule schedule : schedules) {
            schedulesTableModel.addRow(new Object[] {
                    schedule.getScheduleId(),
                    schedule.getCourseCode() + " - " + schedule.getCourseName(),
                    schedule.getDayOfWeek(),
                    schedule.getStartTime(),
                    schedule.getEndTime(),
                    schedule.getRoom(),
                    schedule.getProgramType(),
                    schedule.getInstructorName()
            });
        }
    }

    private void loadCoursesData() {
        coursesTableModel.setRowCount(0);
        List<Course> courses = CourseDAO.getAllCourses();

        for (Course course : courses) {
            coursesTableModel.addRow(new Object[] {
                    course.getCourseId(),
                    course.getCourseCode(),
                    course.getCourseName(),
                    course.getDescription(),
                    course.getCreditHours()
            });
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addScheduleButton) {
            showAddScheduleDialog();
        } else if (e.getSource() == editScheduleButton) {
            editSelectedSchedule();
        } else if (e.getSource() == deleteScheduleButton) {
            deleteSelectedSchedule();
        } else if (e.getSource() == addCourseButton) {
            showAddCourseDialog();
        } else if (e.getSource() == refreshCoursesButton) {
            loadCoursesData();
        }
    }

    private void showAddScheduleDialog() {
        ScheduleDialog dialog = new ScheduleDialog(this, null);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            loadSchedulesData();
            calendarPanel.refreshCalendar();
        }
    }

    private void editSelectedSchedule() {
        int selectedRow = schedulesTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a schedule to edit",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int scheduleId = (int) schedulesTable.getValueAt(selectedRow, 0);

        // Get schedule details - would need to enhance ScheduleDAO to get by ID
        List<Schedule> schedules = ScheduleDAO.getAllSchedulesWithDetails();
        Schedule selectedSchedule = null;

        for (Schedule schedule : schedules) {
            if (schedule.getScheduleId() == scheduleId) {
                selectedSchedule = schedule;
                break;
            }
        }

        if (selectedSchedule != null) {
            ScheduleDialog dialog = new ScheduleDialog(this, selectedSchedule);
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                loadSchedulesData();
                calendarPanel.refreshCalendar();
            }
        }
    }

    private void deleteSelectedSchedule() {
        int selectedRow = schedulesTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a schedule to delete",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int scheduleId = (int) schedulesTable.getValueAt(selectedRow, 0);

        int option = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this schedule?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            boolean success = ScheduleDAO.deleteSchedule(scheduleId);

            if (success) {
                loadSchedulesData();
                calendarPanel.refreshCalendar();
                JOptionPane.showMessageDialog(this,
                        "Schedule deleted successfully",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to delete schedule",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAddCourseDialog() {
        CourseDialog dialog = new CourseDialog(this);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            loadCoursesData();
            // Refresh course filter in student panel
            courseFilterCombo.removeAllItems();
            // Change this line
            courseFilterCombo.addItem(new Course(0, "All Courses", "", "", 0));
            List<Course> courses = CourseDAO.getAllCourses();
            for (Course course : courses) {
                courseFilterCombo.addItem(course);
            }
        }
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
}