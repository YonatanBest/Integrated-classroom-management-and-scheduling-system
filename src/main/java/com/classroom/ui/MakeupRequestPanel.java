package com.classroom.ui;

import com.classroom.dao.MakeupRequestDAO;
import com.classroom.dao.ScheduleDAO;
import com.classroom.model.MakeupRequest;
import com.classroom.model.Schedule;
import com.classroom.model.User;
import com.classroom.util.ColorScheme;
import com.classroom.util.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MakeupRequestPanel extends JPanel {
    private final User currentUser;
    private JTable requestsTable;
    private DefaultTableModel tableModel;
    private JButton newRequestButton;

    public MakeupRequestPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        // Initialize components
        initializeTable();
        initializeButtons();
        loadRequests();

        // Add components to panel
        JScrollPane scrollPane = new JScrollPane(requestsTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel("Makeup Class Requests");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(ColorScheme.PRIMARY);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(newRequestButton);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(buttonPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private void initializeTable() {
        String[] columnNames = { "Course", "Date", "Time", "Status", "Created At" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        requestsTable = new JTable(tableModel);
        requestsTable.setRowHeight(30);
        requestsTable.getTableHeader().setReorderingAllowed(false);
        requestsTable.getTableHeader().setBackground(ColorScheme.PRIMARY);
        requestsTable.getTableHeader().setForeground(Color.WHITE);
        requestsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        requestsTable.setShowGrid(true);
        requestsTable.setGridColor(Color.LIGHT_GRAY);
    }

    private void initializeButtons() {
        newRequestButton = new JButton("New Request");
        UIUtils.styleButton(newRequestButton, ColorScheme.PRIMARY);
        newRequestButton.addActionListener(e -> showNewRequestDialog());
    }

    private void loadRequests() {
        tableModel.setRowCount(0);
        List<MakeupRequest> requests = MakeupRequestDAO.getRequestsByRep(currentUser.getUserId());

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        for (MakeupRequest request : requests) {
            tableModel.addRow(new Object[] {
                    request.getCourseCode() + " - " + request.getCourseName(),
                    request.getRequestedDate().format(dateFormatter),
                    request.getRequestedTime(),
                    request.getStatus(),
                    request.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
            });
        }
    }

    private void showNewRequestDialog() {
        // Get courses for the room
        List<Schedule> schedules = ScheduleDAO.getSchedulesByRoom(currentUser.getAssignedRoom());
        if (schedules.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No courses found for this room",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create course selection combo box
        JComboBox<Schedule> courseCombo = new JComboBox<>(schedules.toArray(new Schedule[0]));
        courseCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                Schedule schedule = (Schedule) value;
                String text = schedule.getCourseCode() + " - " + schedule.getCourseName();
                return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
            }
        });

        // Create date picker
        JSpinner datePicker = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(datePicker, "MMM dd, yyyy");
        datePicker.setEditor(dateEditor);

        // Create time picker
        String[] times = { "08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00" };
        JComboBox<String> timeCombo = new JComboBox<>(times);

        // Show dialog
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        panel.add(new JLabel("Course:"), gbc);
        gbc.gridy++;
        panel.add(courseCombo, gbc);
        gbc.gridy++;
        panel.add(new JLabel("Date:"), gbc);
        gbc.gridy++;
        panel.add(datePicker, gbc);
        gbc.gridy++;
        panel.add(new JLabel("Time:"), gbc);
        gbc.gridy++;
        panel.add(timeCombo, gbc);

        int result = JOptionPane.showConfirmDialog(this,
                panel,
                "New Makeup Request",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            Schedule selectedSchedule = (Schedule) courseCombo.getSelectedItem();
            java.util.Date selectedDate = (java.util.Date) datePicker.getValue();
            String selectedTime = (String) timeCombo.getSelectedItem();

            MakeupRequest request = new MakeupRequest();
            request.setRoom(currentUser.getAssignedRoom());
            request.setCourseId(selectedSchedule.getCourseId());
            request.setRepId(currentUser.getUserId());
            request.setInstructorId(selectedSchedule.getInstructorId());
            request.setRequestedDate(LocalDate.ofInstant(selectedDate.toInstant(),
                    java.time.ZoneId.systemDefault()));
            request.setRequestedTime(selectedTime);

            if (MakeupRequestDAO.createMakeupRequest(request)) {
                loadRequests();
                JOptionPane.showMessageDialog(this,
                        "Makeup request created successfully",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to create makeup request",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}