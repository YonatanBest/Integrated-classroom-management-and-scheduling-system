package com.classroom.ui;

import com.classroom.dao.MakeupRequestDAO;
import com.classroom.model.MakeupRequest;
import com.classroom.model.User;
import com.classroom.util.ColorScheme;
import com.classroom.util.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MakeupApprovalPanel extends JPanel {
    private final User currentUser;
    private JTable requestsTable;
    private DefaultTableModel tableModel;
    private JButton refreshButton;

    public MakeupApprovalPanel(User user) {
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

        // Add double-click listener
        requestsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleRequestSelection();
                }
            }
        });
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel("Pending Makeup Requests");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(ColorScheme.PRIMARY);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(refreshButton);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(buttonPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private void initializeTable() {
        String[] columnNames = { "Course", "Room", "Representative", "Date", "Time", "Status", "Created At" };
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
        refreshButton = new JButton("Refresh");
        UIUtils.styleButton(refreshButton, ColorScheme.SECONDARY);
        refreshButton.addActionListener(e -> loadRequests());
    }

    private void loadRequests() {
        tableModel.setRowCount(0);
        List<MakeupRequest> requests = MakeupRequestDAO.getRequestsByInstructor(currentUser.getUserId());

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        for (MakeupRequest request : requests) {
            tableModel.addRow(new Object[] {
                    request.getCourseCode() + " - " + request.getCourseName(),
                    request.getRoom(),
                    request.getRepName(),
                    request.getRequestedDate().format(dateFormatter),
                    request.getRequestedTime(),
                    request.getStatus(),
                    request.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
            });
        }
    }

    private void handleRequestSelection() {
        int selectedRow = requestsTable.getSelectedRow();
        if (selectedRow >= 0) {
            List<MakeupRequest> requests = MakeupRequestDAO.getRequestsByInstructor(currentUser.getUserId());
            MakeupRequest request = requests.get(selectedRow);

            if (!"Pending".equals(request.getStatus())) {
                JOptionPane.showMessageDialog(this,
                        "This request has already been " + request.getStatus().toLowerCase(),
                        "Information",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String[] options = { "Approve", "Disapprove", "Cancel" };
            int choice = JOptionPane.showOptionDialog(this,
                    String.format("""
                            Course: %s
                            Room: %s
                            Representative: %s
                            Date: %s
                            Time: %s
                            """,
                            request.getCourseCode() + " - " + request.getCourseName(),
                            request.getRoom(),
                            request.getRepName(),
                            request.getRequestedDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                            request.getRequestedTime()),
                    "Makeup Request Details",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (choice == JOptionPane.YES_OPTION) {
                if (MakeupRequestDAO.updateRequestStatus(request.getRequestId(), "Approved")) {
                    loadRequests();
                    JOptionPane.showMessageDialog(this,
                            "Request approved successfully",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            } else if (choice == JOptionPane.NO_OPTION) {
                if (MakeupRequestDAO.updateRequestStatus(request.getRequestId(), "Disapproved")) {
                    loadRequests();
                    JOptionPane.showMessageDialog(this,
                            "Request disapproved",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
    }
}