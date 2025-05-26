package com.classroom.ui.components;

import com.classroom.dao.UserDAO;
import com.classroom.dao.ScheduleDAO;
import com.classroom.model.User;
import com.classroom.util.ColorScheme;
import com.classroom.util.UIUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class RepresentativeManagementPanel extends JPanel {
    private JTable roomTable;
    private DefaultTableModel tableModel;
    private List<String> rooms;

    public RepresentativeManagementPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        // Initialize components
        initializeTable();
        loadRooms();

        // Add components to panel
        JScrollPane scrollPane = new JScrollPane(roomTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(createHeaderPanel(), BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Add double-click listener
        roomTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleRoomSelection();
                }
            }
        });
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel("Room Representatives");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(ColorScheme.PRIMARY);

        JButton refreshButton = new JButton("Refresh");
        UIUtils.styleButton(refreshButton, ColorScheme.SECONDARY);
        refreshButton.addActionListener(e -> loadRooms());

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(refreshButton, BorderLayout.EAST);

        return headerPanel;
    }

    private void initializeTable() {
        String[] columnNames = { "Room", "Representative" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        roomTable = new JTable(tableModel);
        roomTable.setRowHeight(30);
        roomTable.getTableHeader().setReorderingAllowed(false);
        roomTable.getTableHeader().setBackground(ColorScheme.PRIMARY);
        roomTable.getTableHeader().setForeground(Color.WHITE);
        roomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roomTable.setShowGrid(true);
        roomTable.setGridColor(Color.LIGHT_GRAY);
    }

    private void loadRooms() {
        tableModel.setRowCount(0);
        rooms = ScheduleDAO.getAllRooms();

        for (String room : rooms) {
            User rep = UserDAO.getRoomRep(room);
            String repName = rep != null ? rep.getFullName() : "No representative assigned";
            tableModel.addRow(new Object[] { room, repName });
        }
    }

    private void handleRoomSelection() {
        int selectedRow = roomTable.getSelectedRow();
        if (selectedRow >= 0) {
            String room = (String) tableModel.getValueAt(selectedRow, 0);
            showRepresentativeDialog(room);
        }
    }

    private void showRepresentativeDialog(String room) {
        // Get current rep
        User currentRep = UserDAO.getRoomRep(room);

        // Get all students in the room
        List<User> students = UserDAO.getStudentsByRoom(room);
        if (students.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No students assigned to this room",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create combo box with students
        JComboBox<User> studentCombo = new JComboBox<>(students.toArray(new User[0]));
        if (currentRep != null) {
            for (int i = 0; i < students.size(); i++) {
                if (students.get(i).getUserId() == currentRep.getUserId()) {
                    studentCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Show dialog
        int result = JOptionPane.showConfirmDialog(this,
                new Object[] {
                        "Select representative for room " + room + ":",
                        studentCombo
                },
                "Assign Representative",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            User selectedStudent = (User) studentCombo.getSelectedItem();
            if (selectedStudent != null) {
                if (UserDAO.assignRoomRep(selectedStudent.getUserId(), room)) {
                    loadRooms(); // Refresh the table
                    JOptionPane.showMessageDialog(this,
                            "Representative assigned successfully",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to assign representative",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
}