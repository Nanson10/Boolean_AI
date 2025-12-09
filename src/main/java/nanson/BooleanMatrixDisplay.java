package nanson;

import org.jetbrains.annotations.NotNull;
import javax.swing.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

public class BooleanMatrixDisplay {
    
    private static volatile JFrame frame;
    private static volatile JPanel gridPanel;
    private static volatile JPanel[][] cells;
    private static volatile int rows, cols;
    private static volatile Simulator simulator;
    private static volatile AutoGrader autoGrader;
    private static volatile JButton rewardButton, punishButton, runCycleButton;
    private static volatile JLabel statusLabel, activationPercentageLabel, thresholdMultiplierLabel, characterDisplayLabel, progressLabel;
    private static volatile int currentHighlightCount = 0;
    private static volatile boolean cycleRunning = false;
    private static volatile boolean autoCycling = false;
    private static volatile Thread autoCycleThread;
    private static final Object CYCLE_LOCK = new Object();
    private static final Object INIT_LOCK = new Object();

    public BooleanMatrixDisplay(@NotNull Simulator sim) {
        simulator = sim;
        autoGrader = null;
        simulator.setDisplay(this);
        initializeFrame();
    }

    public BooleanMatrixDisplay(@NotNull AutoGrader grader) {
        autoGrader = grader;
        simulator = grader; // AutoGrader extends Simulator
        simulator.setDisplay(this);
        initializeFrame();
        startAutoCycling();
    }

    /**
     * Update the display by pulling data from the simulator.
     * This is called by the Simulator when it needs to refresh the display.
     * @param changesString A string containing changed cells in format "row,col,activation;row,col,activation"
     *                      If empty, pulls all data from simulator for a full update.
     */
    public void update(String changesString) {
        if (changesString == null || changesString.isEmpty()) {
            // Full update - pull all data from simulator
            boolean[][] matrix = simulator.getCurrentMatrixState();
            int currentIteration = simulator.getCurrentIteration();
            int totalIterations = simulator.getTotalIterations();
            double activationPercentage = simulator.getActivationPercentage();
            double thresholdMultiplier = simulator.getActivationThresholdMultiplier();
            int highlightCount = simulator.getHighlightCount();
            
            displayMatrix(matrix, currentIteration, totalIterations, activationPercentage, 
                         thresholdMultiplier, highlightCount);
        } else {
            // Incremental update - only update changed cells
            updateChangedCells(changesString);
            updateMetadata();
        }
    }

    /**
     * Parse and apply incremental cell changes from the changes string.
     * Format: "row,col,activation;row,col,activation"
     */
    private void updateChangedCells(String changesString) {
        String[] cellChanges = changesString.split(Constants.CELL_DELIMITER);
        int highlightCount = simulator.getHighlightCount();
        
        for (String cellChange : cellChanges) {
            if (cellChange.isEmpty()) continue;
            
            String[] parts = cellChange.split(Constants.FIELD_DELIMITER);
            if (parts.length == 3) {
                try {
                    int row = Integer.parseInt(parts[0]);
                    int col = Integer.parseInt(parts[1]);
                    boolean activated = parts[2].equals("1");
                    
                    // Update the cell in the UI with highlighting info
                    updateCellWithHighlight(row, col, activated, highlightCount);
                } catch (NumberFormatException e) {
                    System.err.println("Warning: Invalid cell change format: " + cellChange);
                }
            }
        }
    }

    /**
     * Update a single cell with proper highlighting.
     */
    private void updateCellWithHighlight(int row, int col, boolean value, int highlightCount) {
        if (cells == null || row < 0 || row >= rows || col < 0 || col >= cols) {
            return; // Silently ignore if cells not initialized or out of bounds
        }
        
        boolean isHighlighted = isHighlighted(row, col, highlightCount);
        
        SwingUtilities.invokeLater(() -> {
            if (cells != null && cells[row] != null && cells[row][col] != null) {
                cells[row][col].setBackground(value ? Color.GREEN : Color.WHITE);
                cells[row][col].setBorder(BorderFactory.createLineBorder(
                    isHighlighted ? Color.BLUE : Color.BLACK,
                    isHighlighted ? 3 : 1
                ));
                cells[row][col].repaint();
            }
        });
    }

    /**
     * Update only the metadata labels (iteration, activation %, threshold) without redrawing cells.
     */
    private void updateMetadata() {
        int currentIteration = simulator.getCurrentIteration();
        int totalIterations = simulator.getTotalIterations();
        double activationPercentage = simulator.getActivationPercentage();
        double thresholdMultiplier = simulator.getActivationThresholdMultiplier();
        
        SwingUtilities.invokeLater(() -> {
            if (totalIterations > 0 && statusLabel != null) {
                statusLabel.setText(String.format("Iteration: %d / %d", currentIteration, totalIterations));
            }
            if (activationPercentageLabel != null) {
                activationPercentageLabel.setText(String.format("Activation: %.2f%%", activationPercentage * 100));
            }
            if (thresholdMultiplierLabel != null) {
                thresholdMultiplierLabel.setText(String.format("Threshold: %.3f", thresholdMultiplier));
            }
            
            // Update progress label if AutoGrader is active
            if (autoGrader != null && progressLabel != null) {
                String currentProgress = autoGrader.getCurrentProgress();
                String furthestProgress = autoGrader.getFurthestProgress();
                progressLabel.setText(String.format("Current: %s | Furthest: %s", 
                    currentProgress, furthestProgress));
            }
        });
    }
    
    public void displayMatrix(@NotNull boolean[][] matrix, int currentIteration, int totalIterations,
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
        // Update currentHighlightCount on EDT to avoid race condition
        SwingUtilities.invokeLater(() -> currentHighlightCount = highlightCount);
    }
    
    public void displayMatrixPreserveHighlight(@NotNull boolean[][] matrix, int currentIteration, 
                                                      int totalIterations, double activationPercentage, 
                                                      double thresholdMultiplier) {
        displayMatrix(matrix, currentIteration, totalIterations, activationPercentage, thresholdMultiplier, currentHighlightCount);
    }
    
    private void displayMatrixOnEDT(boolean[][] matrix, int matrixRows, int matrixCols, 
                                          int currentIteration, int totalIterations,
                                          double activationPercentage, double thresholdMultiplier, int highlightCount) {
        synchronized (INIT_LOCK) {
            if (frame == null) initializeFrame();
        }
        
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
        
        // Update progress label if AutoGrader is active
        if (autoGrader != null && progressLabel != null) {
            String currentProgress = autoGrader.getCurrentProgress();
            String furthestProgress = autoGrader.getFurthestProgress();
            progressLabel.setText(String.format("Current: %s | Furthest: %s", 
                currentProgress, furthestProgress));
        }
        
        frame.revalidate();
        frame.repaint();
        frame.setVisible(true);
    }
    
    private boolean isHighlighted(int row, int col, int highlightCount) {
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
    
    private void initializeFrame() {
        frame = new JFrame("Boolean Matrix Display");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());
        
        runCycleButton = new JButton("Run Cycle");
        runCycleButton.addActionListener(e -> new Thread(() -> {
            synchronized (CYCLE_LOCK) {
                if (simulator != null && !cycleRunning && !autoCycling) {
                    cycleRunning = true;
                    updateButtonStates();
                    
                    try {
                        boolean[] result = simulator.runCycle(7);
                        displayCharacterFromBooleanArray(result);
                    } finally {
                        cycleRunning = false;
                        updateButtonStates();
                    }
                }
            }
        }).start());
        
        rewardButton = new JButton("Reward");
        rewardButton.addActionListener(e -> { 
            if (simulator != null && !cycleRunning && !autoCycling) {
                synchronized (CYCLE_LOCK) {
                    if (!cycleRunning && !autoCycling) {
                        simulator.stimulate(true);
                    }
                }
            }
        });
        
        punishButton = new JButton("Punish");
        punishButton.addActionListener(e -> { 
            if (simulator != null && !cycleRunning && !autoCycling) {
                synchronized (CYCLE_LOCK) {
                    if (!cycleRunning && !autoCycling) {
                        simulator.stimulate(false);
                    }
                }
            }
        });
        
        statusLabel = new JLabel("Iteration: 0 / 0");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        activationPercentageLabel = new JLabel("Activation: 0.00%");
        activationPercentageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        thresholdMultiplierLabel = new JLabel("Threshold: 0.000");
        thresholdMultiplierLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        characterDisplayLabel = new JLabel("Character: -");
        characterDisplayLabel.setHorizontalAlignment(SwingConstants.CENTER);
        characterDisplayLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        
        progressLabel = new JLabel("Progress: - | Furthest: -");
        progressLabel.setHorizontalAlignment(SwingConstants.CENTER);
        progressLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
        
        // Create control panel with FlowLayout for buttons and labels
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        controlPanel.add(runCycleButton);
        controlPanel.add(rewardButton);
        controlPanel.add(punishButton);
        controlPanel.add(statusLabel);
        controlPanel.add(activationPercentageLabel);
        controlPanel.add(thresholdMultiplierLabel);
        controlPanel.add(characterDisplayLabel);
        
        // Only show progress label if AutoGrader is active
        if (autoGrader != null) {
            controlPanel.add(progressLabel);
        }
        
        frame.add(controlPanel, BorderLayout.SOUTH);
        
        updateButtonStates();
        update(""); // Use the new update method with empty string for full initial update
    }
    
    private void updateButtonStates() {
        SwingUtilities.invokeLater(() -> {
            boolean shouldEnable = !cycleRunning && autoGrader == null;
            if (runCycleButton != null) runCycleButton.setEnabled(shouldEnable);
            if (rewardButton != null) rewardButton.setEnabled(shouldEnable);
            if (punishButton != null) punishButton.setEnabled(shouldEnable);
        });
    }
    
    public void updateCell(int row, int col, boolean value) {
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
    
    public void closeDisplay() {
        stopAutoCycling();
        SwingUtilities.invokeLater(() -> {
            if (frame != null) {
                frame.dispose();
                frame = null;
                gridPanel = null;
                cells = null;
            }
        });
    }
    
    private void startAutoCycling() {
        if (simulator == null) {
            return; // Can't auto-cycle without a simulator
        }
        
        autoCycling = true;
        autoCycleThread = new Thread(() -> {
            while (autoCycling) {
                synchronized (CYCLE_LOCK) {
                    if (!autoCycling) break; // Double-check after acquiring lock
                    
                    cycleRunning = true;
                    updateButtonStates();
                    
                    try {
                        boolean[] result = simulator.runCycle(7);
                        displayCharacterFromBooleanArray(result);
                    } finally {
                        cycleRunning = false;
                        updateButtonStates();
                    }
                }
                
                try {
                    Thread.sleep(100); // Small delay between cycles to prevent overwhelming the system
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "AutoCycle-Thread");
        autoCycleThread.start();
    }
    
    private void stopAutoCycling() {
        autoCycling = false;
        Thread threadToJoin = autoCycleThread; // Local copy to avoid race
        if (threadToJoin != null && threadToJoin.isAlive()) {
            threadToJoin.interrupt(); // Signal the thread to stop immediately
            try {
                threadToJoin.join(2000); // Wait up to 2 seconds for thread to finish
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static void displayCharacterFromBooleanArray(boolean[] arr) {
        if (arr.length == 0 || arr.length > 16) {
            System.err.println("Warning: Invalid boolean array length: " + arr.length);
            return;
        }
        
        char character = Utilities.booleanArrayToChar(arr);
        SwingUtilities.invokeLater(() -> {
            if (characterDisplayLabel != null) {
                characterDisplayLabel.setText(String.format("Character: '%c' (0x%04X, %d-bit)", 
                    character, (int) character, arr.length));
            }
        });
    }
}
