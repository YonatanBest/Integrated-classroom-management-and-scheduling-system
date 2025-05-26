package com.classroom.ui.components;

import com.classroom.dao.ScheduleRevisionRequestDAO;
import com.classroom.dao.ScheduleDAO;
import com.classroom.model.ScheduleRevisionRequest;
import com.classroom.model.Schedule;
import com.classroom.util.ColorScheme;
import com.classroom.util.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class RevisionRequestManagementPanel extends JPanel {
    private JTable requestsTable;
    private DefaultTableModel tableModel;
    private JButton approveButton;
    private JButton rejectButton;
    private JButton viewDetailsButton;
    private JButton finalizeButton;
    private JTable draftSchedulesTable;
    private DefaultTableModel draftTableModel;

    public RevisionRequestManagementPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initComponents();
        loadRevisionRequests();
        loadDraftSchedules();
    }

    private void initComponents() {
        // Create split pane for two sections
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(300);
        splitPane.setBackground(Color.WHITE);

        // Top section - Revision Requests
        JPanel revisionPanel = createRevisionRequestsPanel();

        // Bottom section - Draft Schedules
        JPanel draftPanel = createDraftSchedulesPanel();

        splitPane.setTopComponent(revisionPanel);
        splitPane.setBottomComponent(draftPanel);

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createRevisionRequestsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Schedule Revision Requests");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(ColorScheme.PRIMARY);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.setBackground(Color.WHITE);

        approveButton = new JButton("Approve Request");
        rejectButton = new JButton("Reject Request");
        viewDetailsButton = new JButton("View Details");

        UIUtils.styleButton(approveButton, ColorScheme.PRIMARY);
        UIUtils.styleButton(rejectButton, ColorScheme.ACCENT);
        UIUtils.styleButton(viewDetailsButton, ColorScheme.SECONDARY);

        approveButton.addActionListener(e -> handleRequest("approved"));
        rejectButton.addActionListener(e -> handleRequest("rejected"));
        viewDetailsButton.addActionListener(e -> viewRequestDetails());

        buttonsPanel.add(viewDetailsButton);
        buttonsPanel.add(approveButton);
        buttonsPanel.add(rejectButton);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(buttonsPanel, BorderLayout.EAST);

        // Table
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableModel.addColumn("Request ID");
        tableModel.addColumn("Course");
        tableModel.addColumn("Instructor");
        tableModel.addColumn("Reason");
        tableModel.addColumn("Status");
        tableModel.addColumn("Created At");

        requestsTable = new JTable(tableModel);
        requestsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        requestsTable.getTableHeader().setReorderingAllowed(false);
        requestsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        requestsTable.setRowHeight(25);
        requestsTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(requestsTable), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createDraftSchedulesPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Draft Schedules");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(ColorScheme.PRIMARY);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.setBackground(Color.WHITE);

        finalizeButton = new JButton("Finalize Selected");
        UIUtils.styleButton(finalizeButton, ColorScheme.PRIMARY);
        finalizeButton.addActionListener(e -> finalizeDraftSchedules());

        buttonsPanel.add(finalizeButton);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(buttonsPanel, BorderLayout.EAST);

        // Table
        draftTableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        draftTableModel.addColumn("Schedule ID");
        draftTableModel.addColumn("Course");
        draftTableModel.addColumn("Day");
        draftTableModel.addColumn("Time");
        draftTableModel.addColumn("Room");
        draftTableModel.addColumn("Status");

        draftSchedulesTable = new JTable(draftTableModel);
        draftSchedulesTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        draftSchedulesTable.getTableHeader().setReorderingAllowed(false);
        draftSchedulesTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        draftSchedulesTable.setRowHeight(25);
        draftSchedulesTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(draftSchedulesTable), BorderLayout.CENTER);

        return panel;
    }

    private void loadRevisionRequests() {
        tableModel.setRowCount(0);
        List<ScheduleRevisionRequest> requests = ScheduleRevisionRequestDAO.getAllPendingRevisionRequests();

        for (ScheduleRevisionRequest request : requests) {
            tableModel.addRow(new Object[] {
                    request.getRequestId(),
                    request.getCourseName(),
                    request.getInstructorName(),
                    request.getRequestReason(),
                    request.getStatus(),
                    request.getCreatedAt()
            });
        }
    }

    private void handleRequest(String status) {
        int selectedRow = requestsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a request to " + status,
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int requestId = (int) requestsTable.getValueAt(selectedRow, 0);
        String courseName = (String) requestsTable.getValueAt(selectedRow, 1);

        // Get coordinator's response
        String response = JOptionPane.showInputDialog(this,
                "Enter your response for " + status + " request:",
                status.substring(0, 1).toUpperCase() + status.substring(1) + " Request",
                JOptionPane.PLAIN_MESSAGE);

        if (response != null) {
            if (ScheduleRevisionRequestDAO.updateRequestStatus(requestId, status)) {
                JOptionPane.showMessageDialog(this,
                        "Request " + status + " successfully",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                loadRevisionRequests();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to " + status + " request",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void viewRequestDetails() {
        int selectedRow = requestsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a request to view",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int requestId = (int) requestsTable.getValueAt(selectedRow, 0);
        String courseName = (String) requestsTable.getValueAt(selectedRow, 1);
        String instructor = (String) requestsTable.getValueAt(selectedRow, 2);
        String reason = (String) requestsTable.getValueAt(selectedRow, 3);

        // Create details panel
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Add details
        detailsPanel.add(new JLabel("<html><b>Course:</b> " + courseName + "</html>"), gbc);
        gbc.gridy++;
        detailsPanel.add(new JLabel("<html><b>Instructor:</b> " + instructor + "</html>"), gbc);
        gbc.gridy++;
        detailsPanel.add(new JLabel("<html><b>Reason for Revision:</b></html>"), gbc);
        gbc.gridy++;

        JTextArea reasonArea = new JTextArea(reason);
        reasonArea.setEditable(false);
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        reasonArea.setPreferredSize(new Dimension(400, 100));
        detailsPanel.add(new JScrollPane(reasonArea), gbc);

        // Show details dialog
        JOptionPane.showMessageDialog(this,
                detailsPanel,
                "Revision Request Details",
                JOptionPane.PLAIN_MESSAGE);
    }

    private void loadDraftSchedules() {
        draftTableModel.setRowCount(0);
        List<Schedule> schedules = ScheduleDAO.getAllDraftSchedules();

        for (Schedule schedule : schedules) {
            draftTableModel.addRow(new Object[] {
                    schedule.getScheduleId(),
                    schedule.getCourseCode() + " - " + schedule.getCourseName(),
                    schedule.getDayOfWeek(),
                    schedule.getStartTime() + " - " + schedule.getEndTime(),
                    schedule.getRoom(),
                    schedule.getStatus()
            });
        }
    }

    private void finalizeDraftSchedules() {
        int[] selectedRows = draftSchedulesTable.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select schedules to finalize",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Show date picker for publish date
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateSpinner.setValue(java.util.Date.from(LocalDate.now().plusDays(7).atStartOfDay()
                .atZone(java.time.ZoneId.systemDefault())
                .toInstant()));

        JPanel datePanel = new JPanel(new FlowLayout());
        datePanel.add(new JLabel("Select publish date: "));
        datePanel.add(dateSpinner);

        int result = JOptionPane.showConfirmDialog(this,
                datePanel,
                "Set Publish Date",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            java.util.Date selectedDate = (java.util.Date) dateSpinner.getValue();
            LocalDate publishDate = selectedDate.toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();

            boolean success = true;
            for (int row : selectedRows) {
                int scheduleId = (int) draftSchedulesTable.getValueAt(row, 0);
                if (!ScheduleDAO.finalizeSchedule(scheduleId, publishDate)) {
                    success = false;
                }
            }

            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Selected schedules have been finalized",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                loadDraftSchedules();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Some schedules could not be finalized",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void refresh() {
        loadRevisionRequests();
        loadDraftSchedules();
    }
}