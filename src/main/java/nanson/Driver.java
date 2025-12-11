package nanson;

import javax.swing.*;
import java.awt.*;

/**
 * Main entry point for the Boolean AI application.
 * Provides a GUI to choose between Manual Mode (BooleanMatrixDisplay)
 * and Auto Mode (AutoGrader).
 */
public class Driver {

    static void main(String[] args) {
        // Set look and feel to system default for better appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Fall back to default if system look and feel fails
        }

        SwingUtilities.invokeLater(() -> showLaunchDialog());
    }

    /**
     * Shows a dialog allowing the user to choose between Manual and Auto modes.
     */
    private static void showLaunchDialog() {
        JFrame launchFrame = new JFrame("Boolean AI - Launch");
        launchFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        launchFrame.setSize(450, 250);
        launchFrame.setLocationRelativeTo(null);
        launchFrame.setLayout(new BorderLayout(10, 10));

        // Title panel
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Boolean AI Neural Network Simulator");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titlePanel.add(titleLabel);
        launchFrame.add(titlePanel, BorderLayout.NORTH);

        // Description panel
        JPanel descPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        descPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel desc1 = new JLabel("Choose a mode to begin:");
        desc1.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JLabel desc2 = new JLabel("• Manual Mode: Interactive neural network with manual controls");
        desc2.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JLabel desc3 = new JLabel("• Auto Mode: Automated learning to generate alphabet sequence");
        desc3.setFont(new Font("SansSerif", Font.PLAIN, 12));

        descPanel.add(desc1);
        descPanel.add(desc2);
        descPanel.add(desc3);
        launchFrame.add(descPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton manualButton = new JButton("Manual Mode");
        manualButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        manualButton.setPreferredSize(new Dimension(150, 40));
        manualButton.addActionListener(e -> {
            launchFrame.dispose();
            launchManualMode();
        });

        JButton autoButton = new JButton("Auto Mode");
        autoButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        autoButton.setPreferredSize(new Dimension(150, 40));
        autoButton.addActionListener(e -> {
            launchFrame.dispose();
            launchAutoMode();
        });

        buttonPanel.add(manualButton);
        buttonPanel.add(autoButton);
        launchFrame.add(buttonPanel, BorderLayout.SOUTH);

        launchFrame.setVisible(true);
    }

    /**
     * Launches the manual mode (BooleanMatrixDisplay).
     */
    private static void launchManualMode() {
        SwingUtilities.invokeLater(() -> {
            Simulator simulator = new Simulator();
            new BooleanMatrixDisplay(simulator);
        });
    }

    /**
     * Launches the auto mode (AutoGrader).
     */
    private static void launchAutoMode() {
        SwingUtilities.invokeLater(() -> {
            AutoGrader autoGrader = new AutoGrader();
            new BooleanMatrixDisplay(autoGrader);
        });
    }
}