package nanson;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 * A utility class for displaying boolean matrices in a JFrame with GridLayout.
 * Green cells represent true values, white cells represent false values.
 */
public class BooleanMatrixDisplay {
    
    private static JFrame frame;
    private static JPanel gridPanel;
    private static JPanel[][] cells;
    private static int rows;
    private static int cols;
    private static Simulator simulator;
    private static JButton runCycleButton;
    private static JLabel statusLabel;
    private static JLabel activationPercentageLabel;
    private static JLabel thresholdMultiplierLabel;

    public static void main(String[] args) {
        simulator = new Simulator(10, 10, 10000, 5);
        // Initialize GUI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> initializeFrame());
    }
    
    /**
     * Displays a boolean matrix in a JFrame with GridLayout and shows iteration progress and activation metrics.
     * Green cells indicate true values, white cells indicate false values.
     * 
     * @param matrix a non-null boolean matrix to display
     * @param currentIteration the current iteration number
     * @param totalIterations the total number of iterations
     * @param activationPercentage the current activation percentage
     * @param thresholdMultiplier the current activation threshold multiplier
     * @param highlightCount the number of result cells to highlight (from bottom-right)
     * @throws NullPointerException if matrix is null
     * @throws IllegalArgumentException if matrix is empty or has irregular dimensions
     * @throws RuntimeException if an error occurs while updating the GUI on the EDT
     */
    public static void displayMatrix(@NotNull boolean[][] matrix, int currentIteration, int totalIterations,
                                    double activationPercentage, double thresholdMultiplier, int highlightCount) {
        Objects.requireNonNull(matrix, "Matrix cannot be null");
        
        if (matrix.length == 0) {
            throw new IllegalArgumentException("Matrix cannot be empty");
        }
        
        final int matrixRows = matrix.length;
        final int matrixCols = matrix[0].length;
        
        if (matrixCols == 0) {
            throw new IllegalArgumentException("Matrix rows cannot be empty");
        }
        
        // Validate that all rows have the same length
        for (int i = 1; i < matrixRows; i++) {
            if (matrix[i].length != matrixCols) {
                throw new IllegalArgumentException("Matrix must have regular dimensions");
            }
        }
        
        // Create a deep copy of the matrix to avoid race conditions
        final boolean[][] matrixCopy = new boolean[matrixRows][matrixCols];
        for (int i = 0; i < matrixRows; i++) {
            System.arraycopy(matrix[i], 0, matrixCopy[i], 0, matrixCols);
        }
        
        // All GUI operations must be performed on the EDT
        if (SwingUtilities.isEventDispatchThread()) {
            // Already on EDT, execute directly
            displayMatrixOnEDT(matrixCopy, matrixRows, matrixCols, currentIteration, totalIterations, 
                             activationPercentage, thresholdMultiplier, highlightCount);
        } else {
            // Not on EDT, use invokeAndWait to ensure completion before returning
            try {
                SwingUtilities.invokeAndWait(() -> displayMatrixOnEDT(matrixCopy, matrixRows, matrixCols, 
                                                                      currentIteration, totalIterations,
                                                                      activationPercentage, thresholdMultiplier, highlightCount));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted while updating display", e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException("Error occurred while updating display", e.getCause());
            }
        }
    }
    
    /**
     * Internal method to display the matrix on the EDT.
     * This method must only be called from the Event Dispatch Thread.
     * 
     * @param matrix the matrix to display
     * @param matrixRows the number of rows
     * @param matrixCols the number of columns
     * @param currentIteration the current iteration number
     * @param totalIterations the total number of iterations
     * @param activationPercentage the current activation percentage
     * @param thresholdMultiplier the current activation threshold multiplier
     * @param highlightCount the number of result cells to highlight (from bottom-right)
     */
    private static void displayMatrixOnEDT(boolean[][] matrix, int matrixRows, int matrixCols, 
                                          int currentIteration, int totalIterations,
                                          double activationPercentage, double thresholdMultiplier, int highlightCount) {
        rows = matrixRows;
        cols = matrixCols;
        
        // Create or update the frame
        if (frame == null) {
            initializeFrame();
        }
        
        // Clear existing grid panel
        if (gridPanel != null) {
            frame.remove(gridPanel);
        }
        
        // Create new grid panel with updated dimensions
        gridPanel = new JPanel(new GridLayout(rows, cols, 2, 2));
        gridPanel.setBackground(Color.DARK_GRAY);
        cells = new JPanel[rows][cols];
        
        // Populate the grid
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                cells[i][j] = new JPanel();
                cells[i][j].setBackground(matrix[i][j] ? Color.GREEN : Color.WHITE);
                
                // Check if this cell should be highlighted (iterating from bottom-right)
                boolean shouldHighlight = false;
                if (highlightCount > 0) {
                    int index = 0;
                    for (int hi = rows - 1; hi >= 0 && index < highlightCount; hi--) {
                        for (int hj = cols - 1; hj >= 0 && index < highlightCount; hj--) {
                            if (hi == i && hj == j) {
                                shouldHighlight = true;
                                break;
                            }
                            index++;
                        }
                        if (shouldHighlight) break;
                    }
                }
                
                // Apply appropriate border (highlighted cells get a thicker blue border)
                if (shouldHighlight) {
                    cells[i][j].setBorder(BorderFactory.createLineBorder(Color.BLUE, 3));
                } else {
                    cells[i][j].setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
                }
                
                gridPanel.add(cells[i][j]);
            }
        }
        
        frame.add(gridPanel, BorderLayout.CENTER);
        
        // Update status labels with iteration info and activation metrics
        if (totalIterations > 0) {
            if (statusLabel != null) {
                statusLabel.setText(String.format("Iteration: %d / %d", currentIteration, totalIterations));
            }
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
    
    /**
     * Initializes the JFrame with default settings.
     */
    private static void initializeFrame() {
        frame = new JFrame("Boolean Matrix Display");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());
        
        // Create and add the Run Cycle button
        runCycleButton = new JButton("Run Cycle");
        runCycleButton.addActionListener(e -> {
            // Run cycle in a separate thread to avoid blocking the EDT
            new Thread(() -> {
                if (simulator != null) {
                    simulator.runCycle(1); // Run one cycle
                }
            }).start();
        });
        
        // Create status labels
        statusLabel = new JLabel("Iteration: 0 / 0");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        activationPercentageLabel = new JLabel("Activation: 0.00%");
        activationPercentageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        thresholdMultiplierLabel = new JLabel("Threshold: 0.000");
        thresholdMultiplierLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel buttonPanel = new JPanel(new GridLayout(1, 4));
        buttonPanel.add(runCycleButton);
        buttonPanel.add(statusLabel);
        buttonPanel.add(activationPercentageLabel);
        buttonPanel.add(thresholdMultiplierLabel);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        
        displayMatrix(simulator.getCurrentMatrixState(), 0, 0, 0.0, 0.0, 0);
    }
    
    /**
     * Updates a specific cell in the matrix display.
     * 
     * @param row the row index
     * @param col the column index
     * @param value the boolean value (true = green, false = white)
     * @throws IllegalStateException if no matrix has been displayed yet
     * @throws IndexOutOfBoundsException if row or col is out of bounds
     */
    public static void updateCell(final int row, final int col, final boolean value) {
        if (cells == null) {
            throw new IllegalStateException("No matrix has been displayed yet");
        }
        if (row < 0 || row >= rows || col < 0 || col >= cols) {
            throw new IndexOutOfBoundsException("Cell coordinates out of bounds");
        }
        
        // Ensure GUI update happens on EDT
        SwingUtilities.invokeLater(() -> {
            if (cells != null && cells[row] != null && cells[row][col] != null) {
                cells[row][col].setBackground(value ? Color.GREEN : Color.WHITE);
                cells[row][col].repaint();
            }
        });
    }
    
    /**
     * Closes and disposes of the display frame.
     * This method is thread-safe and will execute on the EDT.
     */
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
}
