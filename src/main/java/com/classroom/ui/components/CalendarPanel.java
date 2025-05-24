package com.classroom.ui.components;

import com.classroom.dao.ScheduleDAO;
import com.classroom.model.Schedule;
import com.classroom.model.User;
import com.classroom.util.ColorScheme;

import javax.swing.*;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Component for displaying the schedule in a calendar format.
 */
public class CalendarPanel extends JPanel {
    private User currentUser;
    private JPanel calendarGrid;
    private JLabel titleLabel;
    private JComboBox<String> programTypeFilter;
    private JComboBox<String> roomFilter;
    private String selectedRoom; // Add this field to store the selected room value

    // Time slots
    private static final String[] REGULAR_TIMES = {
            "08:00", "09:00", "10:00", "11:00", "12:00",
            "13:00", "14:00", "15:00", "16:00", "17:00"
    };

    private static final String[] EVENING_TIMES = {
            "18:00", "19:00", "20:00"
    };

    private static final String[] WEEKEND_TIMES = {
            "08:00", "09:00", "10:00", "11:00", "12:00",
            "13:00", "14:00", "15:00", "16:00", "17:00"
    };

    private static final String[] DAYS_OF_WEEK = {
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    };

    private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public CalendarPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(0, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initComponents();
        refreshCalendar();
    }

    private void initComponents() {
        // Title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);

        titleLabel = new JLabel("Weekly Schedule");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(ColorScheme.PRIMARY);

        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterPanel.setBackground(Color.WHITE);

        // Program type filter
        JLabel filterLabel = new JLabel("Program Type:");
        programTypeFilter = new JComboBox<>(new String[] { "All", "Regular", "Evening" });
        programTypeFilter.addActionListener(e -> refreshCalendar());

        // Room filter (only for coordinator)
        if (!currentUser.isStudent()) {
            JLabel roomLabel = new JLabel("Room:");
            roomFilter = new JComboBox<>(); // Fix: Properly initialize JComboBox
            // Add "All Rooms" option
            roomFilter.addItem("All Rooms");
            // Add all available rooms
            List<String> rooms = ScheduleDAO.getAllRooms();
            for (String room : rooms) {
                roomFilter.addItem(room);
            }
            roomFilter.addActionListener(e -> refreshCalendar());
            
            filterPanel.add(roomLabel);
            filterPanel.add(roomFilter);
        }

        filterPanel.add(filterLabel);
        filterPanel.add(programTypeFilter);

        titlePanel.add(titleLabel, BorderLayout.WEST);
        titlePanel.add(filterPanel, BorderLayout.EAST);

        // Calendar grid
        calendarGrid = new JPanel(new GridBagLayout());
        calendarGrid.setBackground(Color.WHITE);

        add(titlePanel, BorderLayout.NORTH);
        add(new JScrollPane(calendarGrid), BorderLayout.CENTER);
    }

    public void setRoomFilter(String room) {
        this.selectedRoom = room; // Store the room value in the new field
        if (roomFilter != null && room != null) {
            roomFilter.setSelectedItem(room);
        }
    }

    public void refreshCalendar() {
        calendarGrid.removeAll();
        calendarGrid.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(1, 1, 1, 1);

        // Get selected program type
        String programType = (String) programTypeFilter.getSelectedItem();

        // Add header row with days
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.weightx = 0.15;

        JLabel cornerLabel = new JLabel("Time/Day");
        cornerLabel.setHorizontalAlignment(JLabel.CENTER);
        cornerLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cornerLabel.setBackground(ColorScheme.PRIMARY);
        cornerLabel.setForeground(Color.WHITE);
        cornerLabel.setOpaque(true);
        cornerLabel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        calendarGrid.add(cornerLabel, gbc);

        for (int i = 0; i < DAYS_OF_WEEK.length; i++) {
            gbc.gridx = i + 1;
            gbc.weightx = 1.0;

            JLabel dayLabel = new JLabel(DAYS_OF_WEEK[i]);
            dayLabel.setHorizontalAlignment(JLabel.CENTER);
            dayLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            dayLabel.setBackground(ColorScheme.PRIMARY);
            dayLabel.setForeground(Color.WHITE);
            dayLabel.setOpaque(true);
            dayLabel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
            calendarGrid.add(dayLabel, gbc);
        }

        // Get schedules
        List<Schedule> schedules;
        if (currentUser.isStudent()) {
            if (selectedRoom != null && !selectedRoom.trim().isEmpty()) {
                // Get schedules for student's assigned room
                schedules = ScheduleDAO.getSchedulesByRoom(selectedRoom);
            } else {
                // Fallback to student's enrolled courses if no room assigned
                schedules = ScheduleDAO.getSchedulesByStudentId(currentUser.getUserId());
            }
        } else {
            // For coordinator, show filtered room's schedule
            String selected = roomFilter != null ? (String) roomFilter.getSelectedItem() : "All Rooms";
            if ("All Rooms".equals(selected)) {
                schedules = ScheduleDAO.getAllSchedulesWithDetails();
            } else {
                schedules = ScheduleDAO.getSchedulesByRoom(selected);
            }
        }

        // Filter by program type if needed
        if (!"All".equals(programType)) {
            schedules.removeIf(s -> !s.getProgramType().equalsIgnoreCase(programType));
        }

        // Create a map to store schedules by day and time
        Map<String, Map<String, Schedule>> scheduleMap = new HashMap<>();

        for (Schedule schedule : schedules) {
            String day = schedule.getDayOfWeek();
            String startTime = schedule.getStartTime();

            if (!scheduleMap.containsKey(day)) {
                scheduleMap.put(day, new HashMap<>());
            }

            scheduleMap.get(day).put(startTime, schedule);
        }

        // Add time slots and schedule cells
        int rowIndex = 1;

        // Regular times (weekdays)
        if ("All".equals(programType) || "Regular".equals(programType)) {
            for (String time : REGULAR_TIMES) {
                if (time.equals("12:00") && "Regular".equals(programType)) {
                    continue; // Skip lunch time for regular program
                }

                gbc.gridy = rowIndex++;
                gbc.gridx = 0;
                gbc.weightx = 0.15;

                JLabel timeLabel = new JLabel(time);
                timeLabel.setHorizontalAlignment(JLabel.CENTER);
                timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                timeLabel.setBackground(ColorScheme.LIGHT_ACCENT);
                timeLabel.setForeground(Color.BLACK);
                timeLabel.setOpaque(true);
                timeLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                calendarGrid.add(timeLabel, gbc);

                // Add cells for each day
                for (int dayIndex = 0; dayIndex < 5; dayIndex++) { // Only weekdays
                    gbc.gridx = dayIndex + 1;
                    gbc.weightx = 1.0;

                    String day = DAYS_OF_WEEK[dayIndex];
                    Schedule schedule = null;

                    if (scheduleMap.containsKey(day) && scheduleMap.get(day).containsKey(time)) {
                        schedule = scheduleMap.get(day).get(time);
                    }

                    JPanel cellPanel;
                    if (schedule != null) {
                        cellPanel = createScheduleCell(schedule);
                    } else {
                        cellPanel = createEmptyCell();
                    }

                    calendarGrid.add(cellPanel, gbc);
                }

                // Add empty cells for weekend if showing regular program
                for (int dayIndex = 5; dayIndex < 7; dayIndex++) {
                    gbc.gridx = dayIndex + 1;
                    gbc.weightx = 1.0;

                    JPanel emptyCell = new JPanel();
                    emptyCell.setBackground(new Color(240, 240, 240));
                    emptyCell.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                    calendarGrid.add(emptyCell, gbc);
                }
            }
        }

        // Evening times (weekdays)
        if ("All".equals(programType) || "Evening".equals(programType)) {
            for (String time : EVENING_TIMES) {
                gbc.gridy = rowIndex++;
                gbc.gridx = 0;
                gbc.weightx = 0.15;

                JLabel timeLabel = new JLabel(time);
                timeLabel.setHorizontalAlignment(JLabel.CENTER);
                timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                timeLabel.setBackground(ColorScheme.LIGHT_ACCENT);
                timeLabel.setForeground(Color.BLACK);
                timeLabel.setOpaque(true);
                timeLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                calendarGrid.add(timeLabel, gbc);

                // Add cells for each day
                for (int dayIndex = 0; dayIndex < 5; dayIndex++) { // Only weekdays
                    gbc.gridx = dayIndex + 1;
                    gbc.weightx = 1.0;

                    String day = DAYS_OF_WEEK[dayIndex];
                    Schedule schedule = null;

                    if (scheduleMap.containsKey(day) && scheduleMap.get(day).containsKey(time)) {
                        schedule = scheduleMap.get(day).get(time);
                    }

                    JPanel cellPanel;
                    if (schedule != null) {
                        cellPanel = createScheduleCell(schedule);
                    } else {
                        cellPanel = createEmptyCell();
                    }

                    calendarGrid.add(cellPanel, gbc);
                }

                // Add empty cells for weekend
                for (int dayIndex = 5; dayIndex < 7; dayIndex++) {
                    gbc.gridx = dayIndex + 1;
                    gbc.weightx = 1.0;

                    JPanel emptyCell = new JPanel();
                    emptyCell.setBackground(new Color(240, 240, 240));
                    emptyCell.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                    calendarGrid.add(emptyCell, gbc);
                }
            }
        }

        // Weekend times
        if ("All".equals(programType) || "Evening".equals(programType)) {
            for (String time : WEEKEND_TIMES) {
                // For each weekend day
                for (int dayIndex = 5; dayIndex < 7; dayIndex++) {
                    String day = DAYS_OF_WEEK[dayIndex];

                    // Skip irrelevant times for Saturday (only 13:00-17:00)
                    if (day.equals("Saturday") && (time.compareTo("13:00") < 0 || time.compareTo("17:00") >= 0)) {
                        continue;
                    }

                    // Skip irrelevant times for Sunday (only 08:00-12:00)
                    if (day.equals("Sunday") && (time.compareTo("08:00") < 0 || time.compareTo("12:00") >= 0)) {
                        continue;
                    }

                    // Process relevant weekend schedules
                    Schedule schedule = null;
                    if (scheduleMap.containsKey(day) && scheduleMap.get(day).containsKey(time)) {
                        schedule = scheduleMap.get(day).get(time);
                    }

                    JPanel cellPanel = createScheduleCell(schedule);
                    calendarGrid.add(cellPanel, gbc);

                    rowIndex++;
                }
            }
        }

        revalidate();
        repaint();
    }

    private JPanel createScheduleCell(Schedule schedule) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        // If no schedule exists for this time slot, return an empty panel
        if (schedule == null) {
            panel.setBackground(Color.WHITE);
            return panel;
        }

        // Choose color based on program type
        if ("regular".equalsIgnoreCase(schedule.getProgramType())) {
            panel.setBackground(new Color(220, 237, 200)); // Light green
        } else {
            panel.setBackground(new Color(213, 232, 212)); // Slightly darker green
        }

        JLabel courseLabel = new JLabel(schedule.getCourseCode());
        courseLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        courseLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 0, 5));

        JLabel timeLabel = new JLabel(schedule.getStartTime() + " - " + schedule.getEndTime());
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        timeLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        JLabel roomLabel = new JLabel("Room: " + schedule.getRoom());
        roomLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        roomLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 2, 5));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.add(courseLabel);
        infoPanel.add(timeLabel);
        infoPanel.add(roomLabel);

        // Make the cell a button to show details
        JButton detailsButton = new JButton();
        detailsButton.setLayout(new BorderLayout());
        detailsButton.add(infoPanel, BorderLayout.CENTER);
        detailsButton.setBorderPainted(false);
        detailsButton.setContentAreaFilled(false);
        detailsButton.setFocusPainted(false);

        detailsButton.addActionListener(e -> {
            String details = String.format(
                    "<html><b>%s - %s</b><br>" +
                            "Time: %s - %s<br>" +
                            "Room: %s<br>" +
                            "Instructor: %s<br>" +
                            "Program: %s</html>",
                    schedule.getCourseCode(),
                    schedule.getCourseName(),
                    schedule.getStartTime(),
                    schedule.getEndTime(),
                    schedule.getRoom(),
                    schedule.getInstructorName(),
                    schedule.getProgramType().substring(0, 1).toUpperCase() + schedule.getProgramType().substring(1));

            JOptionPane.showMessageDialog(
                    this,
                    details,
                    "Course Details",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        panel.add(detailsButton, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createEmptyCell() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        return panel;
    }
}