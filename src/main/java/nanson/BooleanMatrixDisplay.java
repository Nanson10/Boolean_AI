package nanson;

import org.jetbrains.annotations.NotNull;
import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

public class BooleanMatrixDisplay {
    
    private static JFrame frame;
    private static JPanel gridPanel;
    private static JPanel[][] cells;
    private static int rows, cols;
    private static Simulator simulator;
    private static JButton rewardButton, punishButton;
    private static JLabel statusLabel, activationPercentageLabel, thresholdMultiplierLabel, characterDisplayLabel;
    private static int currentHighlightCount = 0;
    private static volatile boolean cycleRunning = false;

    public static void main(String[] args) {
        simulator = new Simulator(10, 10, 1000, 5);
        SwingUtilities.invokeLater(() -> initializeFrame());
    }
    
    public static void displayMatrix(@NotNull boolean[][] matrix, int currentIteration, int totalIterations,
                                    double activationPercentage, double thresholdMultiplier, int highlightCount) {
        Objects.requireNonNull(matrix, "Matrix cannot be null");
        if (matrix.length == 0 || matrix[0].length == 0) throw new IllegalArgumentException("Matrix cannot be empty");
        
        for (int i = 1; i < matrix.length; i++) {
            if (matrix[i].length != matrix[0].length) {
                throw new IllegalArgumentException("Matrix must have regular dimensions");
            }
        }
        
        final boolean[][] matrixCopy = new boolean[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            System.arraycopy(matrix[i], 0, matrixCopy[i], 0, matrix[0].length);
        }
        
        if (SwingUtilities.isEventDispatchThread()) {
            displayMatrixOnEDT(matrixCopy, matrix.length, matrix[0].length, currentIteration, 
                             totalIterations, activationPercentage, thresholdMultiplier, highlightCount);
        } else {
            try {
                SwingUtilities.invokeAndWait(() -> displayMatrixOnEDT(matrixCopy, matrix.length, matrix[0].length,
                    currentIteration, totalIterations, activationPercentage, thresholdMultiplier, highlightCount));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted while updating display", e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException("Error occurred while updating display", e.getCause());
            }
        }
        currentHighlightCount = highlightCount;
    }
    
    public static void displayMatrixPreserveHighlight(@NotNull boolean[][] matrix, int currentIteration, 
                                                      int totalIterations, double activationPercentage, 
                                                      double thresholdMultiplier) {
        displayMatrix(matrix, currentIteration, totalIterations, activationPercentage, thresholdMultiplier, currentHighlightCount);
    }
    
    private static void displayMatrixOnEDT(boolean[][] matrix, int matrixRows, int matrixCols, 
                                          int currentIteration, int totalIterations,
                                          double activationPercentage, double thresholdMultiplier, int highlightCount) {
        if (frame == null) initializeFrame();
        
        if (rows != matrixRows || cols != matrixCols || gridPanel == null || cells == null) {
            rows = matrixRows;
            cols = matrixCols;
            
            if (gridPanel != null) {
                frame.remove(gridPanel);
                gridPanel.removeAll();
            }
            
            gridPanel = new JPanel(new GridLayout(rows, cols, 2, 2));
            gridPanel.setBackground(Color.DARK_GRAY);
            cells = new JPanel[rows][cols];
            
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    cells[i][j] = new JPanel();
                    gridPanel.add(cells[i][j]);
                }
            }
            frame.add(gridPanel, BorderLayout.CENTER);
        }
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                cells[i][j].setBackground(matrix[i][j] ? Color.GREEN : Color.WHITE);
                cells[i][j].setBorder(BorderFactory.createLineBorder(
                    isHighlighted(i, j, highlightCount) ? Color.BLUE : Color.BLACK,
                    isHighlighted(i, j, highlightCount) ? 3 : 1
                ));
            }
        }
        
        if (totalIterations > 0 && statusLabel != null) {
            statusLabel.setText(String.format("Iteration: %d / %d", currentIteration, totalIterations));
        }
        if (activationPercentageLabel != null) {
            activationPercentageLabel.setText(String.format("Activation: %.2f%%", activationPercentage * 100));
        }
        if (thresholdMultiplierLabel != null) {
            thresholdMultiplierLabel.setText(String.format("Threshold: %.3f", thresholdMultiplier));
        }
        
        frame.revalidate();
        frame.repaint();
        frame.setVisible(true);
    }
    
    private static boolean isHighlighted(int row, int col, int highlightCount) {
        if (highlightCount <= 0) return false;
        int index = 0;
        for (int i = rows - 1; i >= 0 && index < highlightCount; i--) {
            for (int j = cols - 1; j >= 0 && index < highlightCount; j--) {
                if (i == row && j == col) return true;
                index++;
            }
        }
        return false;
    }
    
    private static void initializeFrame() {
        frame = new JFrame("Boolean Matrix Display");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());
        
        JButton runCycleButton = new JButton("Run Cycle");
        runCycleButton.addActionListener(e -> new Thread(() -> {
            if (simulator != null) {
                cycleRunning = true;
                updateButtonStates();
                
                boolean[] result = simulator.runCycle(7);
                simulator.updateDisplayWithHighlight(7);
                displayCharacterFromBooleanArray(result);
                
                cycleRunning = false;
                updateButtonStates();
            }
        }).start());
        
        rewardButton = new JButton("Reward");
        rewardButton.addActionListener(e -> { if (simulator != null && !cycleRunning) simulator.stimulate(true); });
        
        punishButton = new JButton("Punish");
        punishButton.addActionListener(e -> { if (simulator != null && !cycleRunning) simulator.stimulate(false); });
        
        statusLabel = new JLabel("Iteration: 0 / 0");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        activationPercentageLabel = new JLabel("Activation: 0.00%");
        activationPercentageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        thresholdMultiplierLabel = new JLabel("Threshold: 0.000");
        thresholdMultiplierLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        characterDisplayLabel = new JLabel("Character: -");
        characterDisplayLabel.setHorizontalAlignment(SwingConstants.CENTER);
        characterDisplayLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        
        JPanel buttonPanel = new JPanel(new GridLayout(1, 7));
        buttonPanel.add(runCycleButton);
        buttonPanel.add(rewardButton);
        buttonPanel.add(punishButton);
        buttonPanel.add(statusLabel);
        buttonPanel.add(activationPercentageLabel);
        buttonPanel.add(thresholdMultiplierLabel);
        buttonPanel.add(characterDisplayLabel);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        
        displayMatrix(simulator.getCurrentMatrixState(), 0, 0, 0.0, 0.0, 0);
    }
    
    private static void updateButtonStates() {
        SwingUtilities.invokeLater(() -> {
            if (rewardButton != null) rewardButton.setEnabled(!cycleRunning);
            if (punishButton != null) punishButton.setEnabled(!cycleRunning);
        });
    }
    
    public static void updateCell(int row, int col, boolean value) {
        if (cells == null) throw new IllegalStateException("No matrix has been displayed yet");
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            throw new IndexOutOfBoundsException("Cell coordinates out of bounds");
        }
        SwingUtilities.invokeLater(() -> {
            if (cells != null && cells[row] != null && cells[row][col] != null) {
                cells[row][col].setBackground(value ? Color.GREEN : Color.WHITE);
                cells[row][col].repaint();
            }
        });
    }
    
    public static void closeDisplay() {
        SwingUtilities.invokeLater(() -> {
            if (frame != null) {
                frame.dispose();
                frame = null;
                gridPanel = null;
                cells = null;
            }
        });
    }
    
    private static char booleanArrayToChar(boolean[] arr) {
        if (arr.length == 0) throw new IllegalArgumentException("Boolean array cannot be empty");
        if (arr.length > 16) throw new IllegalArgumentException("Boolean array cannot exceed 16 bits");
        
        int value = 0;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i]) value |= (1 << (arr.length - 1 - i));
        }
        return (char) value;
    }
    
    private static void displayCharacterFromBooleanArray(boolean[] arr) {
        if (arr.length == 0 || arr.length > 16) {
            System.err.println("Warning: Invalid boolean array length: " + arr.length);
            return;
        }
        
        char character = booleanArrayToChar(arr);
        SwingUtilities.invokeLater(() -> {
            if (characterDisplayLabel != null) {
                characterDisplayLabel.setText(String.format("Character: '%c' (0x%04X, %d-bit)", 
                    character, (int) character, arr.length));
            }
        });
    }
}
