package com.classroom.ui;

import com.classroom.dao.CourseDAO;
import com.classroom.model.Course;
import com.classroom.util.ColorScheme;
import com.classroom.util.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Dialog for adding a new course.
 */
public class CourseDialog extends JDialog implements ActionListener {
    private JTextField codeField;
    private JTextField nameField;
    private JTextField creditHoursField; // Add this field
    private JTextArea descriptionArea;
    private JButton saveButton;
    private JButton cancelButton;
    private boolean confirmed = false;

    public CourseDialog(JFrame parent) {
        super(parent, "Add Course", true);

        setSize(450, 350);
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

        // Course Code
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel codeLabel = new JLabel("Course Code:");
        formPanel.add(codeLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        codeField = new JTextField(15);
        UIUtils.styleTextField(codeField);
        formPanel.add(codeField, gbc);

        // Course Name
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        JLabel nameLabel = new JLabel("Course Name:");
        formPanel.add(nameLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        nameField = new JTextField(15);
        UIUtils.styleTextField(nameField);
        formPanel.add(nameField, gbc);

        // Credit Hours
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JLabel creditHoursLabel = new JLabel("Credit Hours:");
        formPanel.add(creditHoursLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        creditHoursField = new JTextField(15);
        UIUtils.styleTextField(creditHoursField);
        formPanel.add(creditHoursField, gbc);

        // Description
        gbc.gridx = 0;
        gbc.gridy = 3; // Changed from 2 to 3
        gbc.weightx = 0.0;
        JLabel descriptionLabel = new JLabel("Description:");
        formPanel.add(descriptionLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3; // Changed from 2 to 3
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        descriptionArea = new JTextArea(5, 15);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        formPanel.add(scrollPane, gbc);

        UIUtils.styleTextArea(descriptionArea);
        formPanel.add(scrollPane, gbc);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);

        saveButton = new JButton("Add Course");
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
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == saveButton) {
            saveCourse();
        } else if (e.getSource() == cancelButton) {
            dispose();
        }
    }

    private void saveCourse() {
        // Validate input
        String code = codeField.getText().trim();
        String name = nameField.getText().trim();
        String creditHours = creditHoursField.getText().trim();
        String description = descriptionArea.getText().trim();

        if (code.isEmpty() || name.isEmpty() || creditHours.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Course code, name, and credit hours are required",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int credits = Integer.parseInt(creditHours);
            if (credits <= 0) {
                JOptionPane.showMessageDialog(this,
                        "Credit hours must be a positive number",
                        "Invalid Input",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Create course object
            Course course = new Course();
            course.setCourseCode(code);
            course.setCourseName(name);
            course.setCreditHours(credits);
            course.setDescription(description);

            // Save to database
            boolean success = CourseDAO.addCourse(course);

            if (success) {
                confirmed = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to add course. Course code may already be in use.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Credit hours must be a valid number",
                    "Invalid Input",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}