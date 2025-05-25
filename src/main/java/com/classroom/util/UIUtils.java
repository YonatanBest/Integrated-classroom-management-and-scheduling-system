package com.classroom.util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Utility methods for UI components.
 */
public class UIUtils {

    /**
     * Style a button with the given color.
     */
    public static void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setOpaque(true);
        button.setBorderPainted(false);

        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(getDarkerColor(color));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
    }

    /**
     * Style a text field.
     */
    public static void styleTextField(JTextField textField) {
        Border line = BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        Border empty = new EmptyBorder(8, 8, 8, 8);
        CompoundBorder border = new CompoundBorder(line, empty);
        textField.setBorder(border);
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    }

    /**
     * Style a text area.
     */
    public static void styleTextArea(JTextArea textArea) {
        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    }

    /**
     * Style a combo box.
     */
    public static void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        comboBox.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
    }

    /**
     * Style a spinner.
     */
    public static void styleSpinner(JSpinner spinner) {
        spinner.setPreferredSize(new Dimension(60, 25));
        spinner.setFont(new Font("Arial", Font.PLAIN, 12));
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JSpinner.DefaultEditor spinnerEditor = (JSpinner.DefaultEditor) editor;
            spinnerEditor.getTextField().setHorizontalAlignment(JTextField.CENTER);
        }
    }

    /**
     * Get a darker shade of the given color.
     */
    private static Color getDarkerColor(Color color) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return Color.getHSBColor(hsb[0], hsb[1], Math.max(0, hsb[2] - 0.1f));
    }
}