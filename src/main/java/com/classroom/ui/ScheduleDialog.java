package com.classroom.ui;

import com.classroom.dao.CourseDAO;
import com.classroom.dao.ScheduleDAO;
import com.classroom.dao.UserDAO;
import com.classroom.model.Course;
import com.classroom.model.Schedule;
import com.classroom.model.User;
import com.classroom.util.ColorScheme;
import com.classroom.util.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import com.classroom.util.TimeSlotUtil;

/**
 * Dialog for adding or editing a schedule.
 */
public class ScheduleDialog extends JDialog implements ActionListener {
    private Schedule schedule;
    private JComboBox<Course> courseCombo;
    private JComboBox<User> instructorCombo;
    private JComboBox<String> dayCombo;
    private JComboBox<String> startTimeCombo;
    private JComboBox<String> endTimeCombo;
    private JTextField roomField;
    private JComboBox<String> programTypeCombo;
    private JButton saveButton;
    private JButton cancelButton;
    private boolean confirmed = false;
    
    private static final String[] DAYS = {
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    };
    
    private static final String[] TIMES = {
            "08:00", "08:50", "09:40", "10:30", "11:20", "12:10", "13:00", 
            "13:50", "14:40", "15:30", "16:20", "17:10", "18:00", "18:50",
            "19:40"
    };
    
    public ScheduleDialog(JFrame parent, Schedule schedule) {
        super(parent, schedule == null ? "Add Schedule" : "Edit Schedule", true);
        this.schedule = schedule;
        
        setSize(500, 400);
        setLocationRelativeTo(parent);
        setResizable(false);
        
        initComponents();
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Course
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel courseLabel = new JLabel("Course:");
        formPanel.add(courseLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        courseCombo = new JComboBox<>();
        List<Course> courses = CourseDAO.getAllCourses();
        for (Course course : courses) {
            courseCombo.addItem(course);
        }
        UIUtils.styleComboBox(courseCombo);
        formPanel.add(courseCombo, gbc);
        
        // Instructor
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        JLabel instructorLabel = new JLabel("Instructor:");
        formPanel.add(instructorLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        instructorCombo = new JComboBox<>();
        List<User> instructors = UserDAO.getAllInstructors();
        for (User instructor : instructors) {
            instructorCombo.addItem(instructor);
        }
        UIUtils.styleComboBox(instructorCombo);
        formPanel.add(instructorCombo, gbc);
        
        // Day
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        JLabel dayLabel = new JLabel("Day:");
        formPanel.add(dayLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        dayCombo = new JComboBox<>(DAYS);
        UIUtils.styleComboBox(dayCombo);
        formPanel.add(dayCombo, gbc);
        
        // Start Time
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.0;
        JLabel startTimeLabel = new JLabel("Start Time:");
        formPanel.add(startTimeLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        startTimeCombo = new JComboBox<>(TIMES);
        UIUtils.styleComboBox(startTimeCombo);
        formPanel.add(startTimeCombo, gbc);
        
        // End Time
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.0;
        JLabel endTimeLabel = new JLabel("End Time:");
        formPanel.add(endTimeLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        endTimeCombo = new JComboBox<>(TIMES);
        UIUtils.styleComboBox(endTimeCombo);
        endTimeCombo.setSelectedIndex(1); // Default to 1 hour later
        formPanel.add(endTimeCombo, gbc);
        
        // Room
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0.0;
        JLabel roomLabel = new JLabel("Room:");
        formPanel.add(roomLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.weightx = 1.0;
        roomField = new JTextField(15);
        UIUtils.styleTextField(roomField);
        formPanel.add(roomField, gbc);
        
        // Program Type
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.weightx = 0.0;
        JLabel programTypeLabel = new JLabel("Program Type:");
        formPanel.add(programTypeLabel, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.weightx = 1.0;
        programTypeCombo = new JComboBox<>(new String[]{"Regular", "Evening"});
        UIUtils.styleComboBox(programTypeCombo);
        formPanel.add(programTypeCombo, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        
        saveButton = new JButton(schedule == null ? "Add" : "Save");
        cancelButton = new JButton("Cancel");
        
        UIUtils.styleButton(saveButton, ColorScheme.PRIMARY);
        UIUtils.styleButton(cancelButton, ColorScheme.SECONDARY);
        
        saveButton.addActionListener(this);
        cancelButton.addActionListener(this);
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        // Add components to main panel
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Set content pane
        setContentPane(mainPanel);
        
        // If editing, populate fields
        if (schedule != null) {
            populateFields();
        }
    }
    
    private void populateFields() {
        // Set course
        for (int i = 0; i < courseCombo.getItemCount(); i++) {
            Course course = courseCombo.getItemAt(i);
            if (course.getCourseId() == schedule.getCourseId()) {
                courseCombo.setSelectedIndex(i);
                break;
            }
        }
        
        // Set instructor
        for (int i = 0; i < instructorCombo.getItemCount(); i++) {
            User instructor = instructorCombo.getItemAt(i);
            if (instructor.getUserId() == schedule.getInstructorId()) {
                instructorCombo.setSelectedIndex(i);
                break;
            }
        }
        
        // Set day
        for (int i = 0; i < dayCombo.getItemCount(); i++) {
            if (dayCombo.getItemAt(i).equals(schedule.getDayOfWeek())) {
                dayCombo.setSelectedIndex(i);
                break;
            }
        }
        
        // Set start time
        for (int i = 0; i < startTimeCombo.getItemCount(); i++) {
            if (startTimeCombo.getItemAt(i).equals(schedule.getStartTime())) {
                startTimeCombo.setSelectedIndex(i);
                break;
            }
        }
        
        // Set end time
        for (int i = 0; i < endTimeCombo.getItemCount(); i++) {
            if (endTimeCombo.getItemAt(i).equals(schedule.getEndTime())) {
                endTimeCombo.setSelectedIndex(i);
                break;
            }
        }
        
        // Set room
        roomField.setText(schedule.getRoom());
        
        // Set program type
        if ("evening".equalsIgnoreCase(schedule.getProgramType())) {
            programTypeCombo.setSelectedIndex(1);
        } else {
            programTypeCombo.setSelectedIndex(0);
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == saveButton) {
            saveSchedule();
        } else if (e.getSource() == cancelButton) {
            dispose();
        }
    }
    
    private void saveSchedule() {
        // Validate input
        if (courseCombo.getSelectedItem() == null || instructorCombo.getSelectedItem() == null || 
                roomField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "Please fill in all fields", 
                    "Missing Information", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Validate time
        int startIndex = startTimeCombo.getSelectedIndex();
        int endIndex = endTimeCombo.getSelectedIndex();
        
        if (endIndex <= startIndex) {
            JOptionPane.showMessageDialog(this, 
                    "End time must be after start time", 
                    "Invalid Time", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get selected values
        Course selectedCourse = (Course) courseCombo.getSelectedItem();
        User selectedInstructor = (User) instructorCombo.getSelectedItem();
        String selectedDay = (String) dayCombo.getSelectedItem();
        String selectedStartTime = (String) startTimeCombo.getSelectedItem();
        String selectedEndTime = (String) endTimeCombo.getSelectedItem();
        String room = roomField.getText().trim();
        String programType = ((String) programTypeCombo.getSelectedItem()).toLowerCase();
        
        // Validate schedule based on program type and day
        if (!isValidTimeSlot(selectedDay, selectedStartTime, selectedEndTime, programType)) {
            JOptionPane.showMessageDialog(this, 
                    "The selected time slot is not valid for the program type and day", 
                    "Invalid Schedule", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Create or update schedule
        if (schedule == null) {
            // Create new schedule
            schedule = new Schedule();
            schedule.setCourseId(selectedCourse.getCourseId());
            schedule.setInstructorId(selectedInstructor.getUserId());
            schedule.setDayOfWeek(selectedDay);
            schedule.setStartTime(selectedStartTime);
            schedule.setEndTime(selectedEndTime);
            schedule.setRoom(room);
            schedule.setProgramType(programType);
            
            boolean success = ScheduleDAO.addSchedule(schedule);
            
            if (success) {
                confirmed = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, 
                        "Failed to add schedule", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            // Update existing schedule
            schedule.setCourseId(selectedCourse.getCourseId());
            schedule.setInstructorId(selectedInstructor.getUserId());
            schedule.setDayOfWeek(selectedDay);
            schedule.setStartTime(selectedStartTime);
            schedule.setEndTime(selectedEndTime);
            schedule.setRoom(room);
            schedule.setProgramType(programType);
            
            boolean success = ScheduleDAO.updateSchedule(schedule);
            
            if (success) {
                confirmed = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, 
                        "Failed to update schedule", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private boolean isValidTimeSlot(String day, String startTime, String endTime, String programType) {
        // First check if the period duration is valid (50 minutes)
        if (!TimeSlotUtil.isValidPeriodDuration(startTime, endTime)) {
            JOptionPane.showMessageDialog(this,
                "Each period must be exactly 50 minutes long",
                "Invalid Time Slot",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        // Then check program-specific time constraints
        boolean isValid = "regular".equals(programType) ?
            TimeSlotUtil.isValidRegularProgramTime(day, startTime, endTime) :
            TimeSlotUtil.isValidEveningProgramTime(day, startTime, endTime);
            
        if (!isValid) {
            String message = "regular".equals(programType) ?
                "Regular programs run Mon-Fri, 8:00-17:00 (excluding 12:00-13:00)" :
                "Evening programs run Mon-Fri 18:00-20:00, Sat 13:00-17:00, Sun 8:00-12:00";
            
            JOptionPane.showMessageDialog(this,
                message,
                "Invalid Time Slot",
                JOptionPane.WARNING_MESSAGE);
        }
        
        return isValid;
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }
}