package com.classroom.ui;

import javax.swing.*;
import java.awt.*;
import com.classroom.model.User;
import com.classroom.dao.UserDAO;
import com.classroom.util.ColorScheme;
import com.classroom.util.UIUtils;

public class SignupFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField fullNameField;
    private JTextField emailField;
    private JComboBox<String> userTypeCombo;
    private JComboBox<String> programTypeCombo;
    private LoginFrame loginFrame;

    public SignupFrame(LoginFrame loginFrame) {
        this.loginFrame = loginFrame;
        setTitle("Sign Up");
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 10, 5, 10);

        // Initialize components
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        fullNameField = new JTextField(20);
        emailField = new JTextField(20);
        userTypeCombo = new JComboBox<>(new String[]{"student", "instructor"});
        programTypeCombo = new JComboBox<>(new String[]{"regular", "evening"});
        JButton signupButton = new JButton("Sign Up");

        // Style components
        UIUtils.styleTextField(usernameField);
        UIUtils.styleTextField(passwordField);
        UIUtils.styleTextField(fullNameField);
        UIUtils.styleTextField(emailField);
        UIUtils.styleComboBox(userTypeCombo);
        UIUtils.styleComboBox(programTypeCombo);
        UIUtils.styleButton(signupButton, ColorScheme.PRIMARY);

        // Add components
        addComponent("Username:", usernameField, gbc, 0);
        addComponent("Password:", passwordField, gbc, 1);
        addComponent("Full Name:", fullNameField, gbc, 2);
        addComponent("Email:", emailField, gbc, 3);
        addComponent("User Type:", userTypeCombo, gbc, 4);
        addComponent("Program Type:", programTypeCombo, gbc, 5);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 5, 10);
        add(signupButton, gbc);

        signupButton.addActionListener(e -> handleSignup());
    }

    private void addComponent(String label, JComponent component, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 1;
        add(component, gbc);
    }

    private void handleSignup() {
        // Validate input fields
        if (usernameField.getText().trim().isEmpty() ||
            new String(passwordField.getPassword()).trim().isEmpty() ||
            fullNameField.getText().trim().isEmpty() ||
            emailField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate email format
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (!emailField.getText().matches(emailRegex)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User user = new User();
        user.setUsername(usernameField.getText());
        user.setPassword(new String(passwordField.getPassword()));
        user.setFullName(fullNameField.getText());
        user.setEmail(emailField.getText());
        user.setUserType(userTypeCombo.getSelectedItem().toString());
        user.setProgramType(programTypeCombo.getSelectedItem().toString());

        if (UserDAO.registerUser(user)) {
            JOptionPane.showMessageDialog(this, "Registration successful!");
            dispose();
            loginFrame.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Registration failed. Please try again.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}