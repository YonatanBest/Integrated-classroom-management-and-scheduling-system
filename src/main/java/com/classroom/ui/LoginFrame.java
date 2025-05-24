package com.classroom.ui;

import com.classroom.dao.UserDAO;
import com.classroom.model.User;
import com.classroom.util.ColorScheme;
import com.classroom.util.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Login screen for the application.
 */
public class LoginFrame extends JFrame implements ActionListener {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> userTypeCombo;
    private JButton loginButton;
    private JButton signupButton;
    private JLabel errorLabel;

    public LoginFrame() {
        setTitle("Classroom Management System - Login");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        initComponents();
    }

    private void initComponents() {
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Logo/Title panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);

        JLabel logoLabel = new JLabel("Classroom Management System", JLabel.CENTER);
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        logoLabel.setForeground(ColorScheme.PRIMARY);

        JLabel subtitleLabel = new JLabel("Login to access your account", JLabel.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.DARK_GRAY);

        titlePanel.add(logoLabel, BorderLayout.CENTER);
        titlePanel.add(subtitleLabel, BorderLayout.SOUTH);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel usernameLabel = new JLabel("Username:");
        formPanel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        usernameField = new JTextField(20);
        UIUtils.styleTextField(usernameField);
        formPanel.add(usernameField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        JLabel passwordLabel = new JLabel("Password:");
        formPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        passwordField = new JPasswordField(20);
        UIUtils.styleTextField(passwordField);
        formPanel.add(passwordField, gbc);

        // User Type
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        JLabel userTypeLabel = new JLabel("I am a:");
        formPanel.add(userTypeLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        userTypeCombo = new JComboBox<>(new String[] { "Student", "Coordinator", "Instructor" });
        UIUtils.styleComboBox(userTypeCombo);
        formPanel.add(userTypeCombo, gbc);

        // Error label
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        errorLabel = new JLabel("");
        errorLabel.setForeground(Color.RED);
        formPanel.add(errorLabel, gbc);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        loginButton = new JButton("Login");
        signupButton = new JButton("Sign Up");

        UIUtils.styleButton(loginButton, ColorScheme.PRIMARY);
        UIUtils.styleButton(signupButton, ColorScheme.SECONDARY);

        loginButton.addActionListener(this);
        signupButton.addActionListener(this);

        buttonPanel.add(loginButton);
        buttonPanel.add(signupButton);

        // Add components to main panel
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Set content pane
        setContentPane(mainPanel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            login();
        } else if (e.getSource() == signupButton) {
            openSignupFrame();
        }
    }

    private void login() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String userType = userTypeCombo.getSelectedItem().toString().toLowerCase();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please fill in all fields");
            return;
        }

        User user = UserDAO.authenticateUser(username, password);

        if (user == null) {
            errorLabel.setText("Invalid username or password");
            return;
        }

        // Check if user type matches selected type
        if (userType.equals("student") && !user.isStudent()) {
            errorLabel.setText("Invalid account type. Please select 'Coordinator' instead.");
            return;
        }

        if (userType.equals("coordinator") && !user.isCoordinator() && !user.isInstructor()) {
            errorLabel.setText("Invalid account type. Please select 'Student' instead.");
            return;
        }

        // Login successful, open appropriate dashboard
        if (user.isStudent()) {
            StudentDashboard dashboard = new StudentDashboard(user);
            dashboard.setVisible(true);
        } else if (user.isInstructor()) {
            InstructorDashboard dashboard = new InstructorDashboard(user);
            dashboard.setVisible(true);
        } else {
            CoordinatorDashboard dashboard = new CoordinatorDashboard(user);
            dashboard.setVisible(true);
        }

        this.dispose();
    }

    private void openSignupFrame() {
        SignupFrame signupFrame = new SignupFrame(this);
        signupFrame.setVisible(true);
        this.setVisible(false);
    }
}