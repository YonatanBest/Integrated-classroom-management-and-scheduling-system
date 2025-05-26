package com.classroom.ui;

import javax.swing.*;
import java.awt.*;
import com.classroom.model.User;
import com.classroom.util.ColorScheme;
import com.classroom.util.UIUtils;
import com.classroom.ui.components.CalendarPanel;
import com.classroom.ui.components.MakeupApprovalPanel;

public class InstructorDashboard extends JFrame {
    private User currentUser;
    private CalendarPanel calendarPanel;
    private JPanel profilePanel;

    public InstructorDashboard(User user) {
        this.currentUser = user;

        setTitle("Instructor Dashboard - " + user.getFullName());
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
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.setBackground(Color.WHITE);

        // Create calendar panel to show instructor's schedule
        calendarPanel = new CalendarPanel(currentUser);
        profilePanel = createProfilePanel();

        tabbedPane.addTab("My Schedule", new ImageIcon(), new JScrollPane(calendarPanel),
                "View your teaching schedule");
        tabbedPane.addTab("Makeup Requests", new ImageIcon(), new MakeupApprovalPanel(currentUser),
                "Manage makeup class requests");
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
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getFullName());
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE);

        JButton logoutButton = new JButton("Logout");
        UIUtils.styleButton(logoutButton, ColorScheme.SECONDARY);
        logoutButton.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            this.dispose();
        });

        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        headerPanel.add(logoutButton, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Add profile information display
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        panel.add(new JLabel("Name: " + currentUser.getFullName()), gbc);
        gbc.gridy++;
        panel.add(new JLabel("Email: " + currentUser.getEmail()), gbc);
        gbc.gridy++;
        panel.add(new JLabel("User Type: Instructor"), gbc);

        return panel;
    }
}