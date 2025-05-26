package com.classroom.ui.components;

import com.classroom.dao.ResourceDAO;
import com.classroom.model.Resource;
import com.classroom.util.ColorScheme;
import com.classroom.util.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ResourceAvailabilityPanel extends JPanel {
    private JLabel projectorLabel;
    private JLabel connectorLabel;
    private JButton refreshButton;
    private int totalProjectors;
    private int totalConnectors;

    public ResourceAvailabilityPanel() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.SECONDARY),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)));

        // Get total quantities
        totalProjectors = ResourceDAO.getTotalResourceQuantity("Projector");
        totalConnectors = ResourceDAO.getTotalResourceQuantity("Connector");

        // Create resource labels
        projectorLabel = new JLabel();
        projectorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        projectorLabel.setIcon(UIManager.getIcon("OptionPane.informationIcon"));

        connectorLabel = new JLabel();
        connectorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        connectorLabel.setIcon(UIManager.getIcon("OptionPane.informationIcon"));

        // Create refresh button
        refreshButton = new JButton("Refresh");
        UIUtils.styleButton(refreshButton, ColorScheme.SECONDARY);
        refreshButton.addActionListener(e -> refreshAvailability());

        // Add components
        add(new JLabel("Resource Availability:"));
        add(projectorLabel);
        add(connectorLabel);
        add(refreshButton);

        // Initial refresh
        refreshAvailability();
    }

    public void refreshAvailability() {
        // Get total quantities
        totalProjectors = ResourceDAO.getTotalResourceQuantity("Projector");
        totalConnectors = ResourceDAO.getTotalResourceQuantity("Connector");

        // Get available quantities
        int availableProjectors = ResourceDAO.getAvailableResourceQuantity("Projector");
        int availableConnectors = ResourceDAO.getAvailableResourceQuantity("Connector");

        projectorLabel.setText(String.format("Projectors: %d/%d", availableProjectors, totalProjectors));
        connectorLabel.setText(String.format("Connectors: %d/%d", availableConnectors, totalConnectors));

        // Update colors based on availability percentages
        updateLabelColor(projectorLabel, availableProjectors, totalProjectors);
        updateLabelColor(connectorLabel, availableConnectors, totalConnectors);
    }

    private void updateLabelColor(JLabel label, int available, int total) {
        if (total == 0) {
            label.setForeground(Color.GRAY);
            return;
        }

        double percentage = (double) available / total * 100;

        if (percentage <= 10) { // 10% or less
            label.setForeground(Color.RED);
        } else if (percentage <= 30) { // 30% or less
            label.setForeground(new Color(255, 165, 0)); // Orange
        } else {
            label.setForeground(new Color(0, 128, 0)); // Dark Green
        }
    }
}