package com.classroom;

// Fix imports
import com.classroom.ui.LoginFrame;
import com.classroom.util.DatabaseUtil;

import javax.swing.*;

/**
 * Main entry point for the Classroom Management System application.
 */
public class Main {
    public static void main(String[] args) {
        // Initialize database before launching the application
        DatabaseUtil.initializeDatabase();
        
        // Launch the application
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}