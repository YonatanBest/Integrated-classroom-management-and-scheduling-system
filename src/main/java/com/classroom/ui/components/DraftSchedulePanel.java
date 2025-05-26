package com.classroom.ui.components;

import com.classroom.dao.ScheduleDAO;
import com.classroom.dao.ScheduleRevisionRequestDAO;
import com.classroom.model.Schedule;
import com.classroom.model.ScheduleRevisionRequest;
import com.classroom.model.User;
import com.classroom.util.ColorScheme;
import com.classroom.util.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class DraftSchedulePanel extends JPanel {
    private User currentUser;
    private JTable draftTable;
    private DefaultTableModel draftTableModel;
    private JButton requestRevisionButton;
    private JButton viewRequestsButton;

    public DraftSchedulePanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        initComponents();
        loadDraftSchedules();
    }

    private void initComponents() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("Draft Schedules");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(ColorScheme.PRIMARY);

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonsPanel.setBackground(Color.WHITE);

        requestRevisionButton = new JButton("Request Revision");
        viewRequestsButton = new JButton("View My Requests");

        UIUtils.styleButton(requestRevisionButton, ColorScheme.PRIMARY);
        UIUtils.styleButton(viewRequestsButton, ColorScheme.SECONDARY);

        requestRevisionButton.addActionListener(e -> requestRevision());
        viewRequestsButton.addActionListener(e -> viewRequests());

        buttonsPanel.add(requestRevisionButton);
        buttonsPanel.add(viewRequestsButton);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(buttonsPanel, BorderLayout.EAST);

        // Table
        draftTableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        draftTableModel.addColumn("ID");
        draftTableModel.addColumn("Course");
        draftTableModel.addColumn("Day");
        draftTableModel.addColumn("Time");
        draftTableModel.addColumn("Room");
        draftTableModel.addColumn("Status");
        draftTableModel.addColumn("Publish Date");

        draftTable = new JTable(draftTableModel);
        draftTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        draftTable.getTableHeader().setReorderingAllowed(false);
        draftTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        draftTable.setRowHeight(25);
        draftTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        // Add components
        add(headerPanel, BorderLayout.NORTH);
        add(new JScrollPane(draftTable), BorderLayout.CENTER);
    }

    private void loadDraftSchedules() {
        draftTableModel.setRowCount(0);
        List<Schedule> schedules = ScheduleDAO.getSchedulesByInstructorId(currentUser.getUserId());

        LocalDate oneWeekFromNow = LocalDate.now().plusDays(7);

        for (Schedule schedule : schedules) {
            if (schedule.isDraft() || schedule.isRevisionRequested()) {
                draftTableModel.addRow(new Object[] {
                        schedule.getScheduleId(),
                        schedule.getCourseCode() + " - " + schedule.getCourseName(),
                        schedule.getDayOfWeek(),
                        schedule.getStartTime() + " - " + schedule.getEndTime(),
                        schedule.getRoom(),
                        schedule.getStatus(),
                        schedule.getPublishDate() != null ? schedule.getPublishDate() : "Not set"
                });
            }
        }
    }

    private void requestRevision() {
        int selectedRow = draftTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a schedule to request revision",
                    "No Selection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int scheduleId = (int) draftTable.getValueAt(selectedRow, 0);
        String courseName = (String) draftTable.getValueAt(selectedRow, 1);

        // Create input panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        JLabel reasonLabel = new JLabel("Reason for revision:");
        JTextArea reasonArea = new JTextArea(3, 30);
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);

        JLabel changesLabel = new JLabel("Requested changes:");
        JTextArea changesArea = new JTextArea(5, 30);
        changesArea.setLineWrap(true);
        changesArea.setWrapStyleWord(true);

        inputPanel.add(reasonLabel, gbc);
        gbc.gridy++;
        inputPanel.add(new JScrollPane(reasonArea), gbc);
        gbc.gridy++;
        inputPanel.add(changesLabel, gbc);
        gbc.gridy++;
        inputPanel.add(new JScrollPane(changesArea), gbc);

        int result = JOptionPane.showConfirmDialog(this,
                inputPanel,
                "Request Revision for " + courseName,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String reason = reasonArea.getText().trim();
            String changes = changesArea.getText().trim();

            if (reason.isEmpty() || changes.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please provide both reason and requested changes",
                        "Missing Information",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            ScheduleRevisionRequest request = new ScheduleRevisionRequest(
                    scheduleId,
                    currentUser.getUserId(),
                    reason,
                    changes);

            if (ScheduleRevisionRequestDAO.createRevisionRequest(request)) {
                JOptionPane.showMessageDialog(this,
                        "Revision request submitted successfully",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                loadDraftSchedules();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to submit revision request",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void viewRequests() {
        List<ScheduleRevisionRequest> requests = ScheduleRevisionRequestDAO
                .getRevisionRequestsByInstructor(currentUser.getUserId());

        if (requests.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "You have no revision requests",
                    "No Requests",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Create requests table
        DefaultTableModel requestsModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        requestsModel.addColumn("Request ID");
        requestsModel.addColumn("Course");
        requestsModel.addColumn("Reason");
        requestsModel.addColumn("Status");
        requestsModel.addColumn("Created At");

        for (ScheduleRevisionRequest request : requests) {
            requestsModel.addRow(new Object[] {
                    request.getRequestId(),
                    request.getCourseName(),
                    request.getRequestReason(),
                    request.getStatus(),
                    request.getCreatedAt()
            });
        }

        JTable requestsTable = new JTable(requestsModel);
        requestsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        requestsTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(requestsTable);
        scrollPane.setPreferredSize(new Dimension(800, 300));

        JOptionPane.showMessageDialog(this,
                scrollPane,
                "My Revision Requests",
                JOptionPane.PLAIN_MESSAGE);
    }

    public void refresh() {
        loadDraftSchedules();
    }
}