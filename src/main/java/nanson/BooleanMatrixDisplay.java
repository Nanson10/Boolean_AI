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
    private static volatile JLabel statusLabel, activationPercentageLabel, thresholdMultiplierLabel,
            characterDisplayLabel, progressLabel, distanceLabel, resizeCountdownLabel;
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
     * 
     * @param changesString A string containing changed cells in format
     *                      "row,col,activation;row,col,activation"
     *                      If empty, pulls all data from simulator for a full
     *                      update.
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
            if (cellChange.isEmpty())
                continue;

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

        boolean isBlueHighlighted = isHighlighted(row, col, highlightCount);
        boolean isWrongBit = isIncorrectBit(row, col, value, highlightCount);

        SwingUtilities.invokeLater(() -> {
            if (cells != null && cells[row] != null && cells[row][col] != null) {
                // Check if this is a DataNeuron
                Simulator.Neuron neuron = simulator.getNeuronAt(row, col);
                boolean isDataNeuron = neuron != null && neuron.isDataNeuron();

                // Set background color: orange for active DataNeuron, red for incorrect bits,
                // green for activated, white for inactive
                Color backgroundColor;
                if (isDataNeuron && value) {
                    backgroundColor = Color.ORANGE;
                } else if (isWrongBit) {
                    backgroundColor = Color.RED;
                } else {
                    backgroundColor = value ? Color.GREEN : Color.WHITE;
                }
                cells[row][col].setBackground(backgroundColor);

                // Blue border for output region
                Color borderColor = isBlueHighlighted ? Color.BLUE : Color.BLACK;
                int borderWidth = isBlueHighlighted ? 3 : 1;

                cells[row][col].setBorder(BorderFactory.createLineBorder(borderColor, borderWidth));
                cells[row][col].repaint();
            }
        });
    }

    /**
     * Update only the metadata labels (iteration, activation %, threshold) without
     * redrawing cells.
     */
    private void updateMetadata() {
        int totalIterations = simulator.getTotalIterations();
        double activationPercentage = simulator.getActivationPercentage();
        double thresholdMultiplier = simulator.getActivationThresholdMultiplier();

        SwingUtilities.invokeLater(() -> {
            if (totalIterations > 0 && statusLabel != null) {
                statusLabel.setText(String.format("Iterations per update: %d", totalIterations));
            }
            if (activationPercentageLabel != null) {
                activationPercentageLabel.setText(String.format("Activation: %.2f%%", activationPercentage * 100));
            }
            if (thresholdMultiplierLabel != null) {
                thresholdMultiplierLabel.setText(String.format("Threshold: %.5f", thresholdMultiplier));
            }

            // Update progress label if AutoGrader is active
            if (autoGrader != null && progressLabel != null) {
                String currentProgress = autoGrader.getCurrentProgress();
                String furthestProgress = autoGrader.getFurthestProgress();
                progressLabel.setText(String.format("Current: %s | Furthest: %s",
                        currentProgress, furthestProgress));
            }

            // Update distance label if AutoGrader is active
            if (autoGrader != null && distanceLabel != null) {
                int currentDistance = autoGrader.getCurrentDistance();
                char goal = autoGrader.getGoal();
                int previousDistance = autoGrader.getLastAnswerDistance();
                distanceLabel
                        .setText(String.format("Goal: '%c' | Current Distance: %d | Previous Distance: %d | Target: 0",
                                goal, currentDistance, previousDistance));
            }

            // Update resize countdown label if AutoGrader is active
            if (autoGrader != null && resizeCountdownLabel != null) {
                int checksUntilResize = autoGrader.getChecksUntilResize();
                resizeCountdownLabel.setText(String.format("Checks Until Resize: %d", checksUntilResize));
            }
        });
    }

    public void displayMatrix(@NotNull boolean[][] matrix, int currentIteration, int totalIterations,
            double activationPercentage, double thresholdMultiplier, int highlightCount) {
        Objects.requireNonNull(matrix, "Matrix cannot be null");
        if (matrix.length == 0 || matrix[0].length == 0)
            throw new IllegalArgumentException("Matrix cannot be empty");

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
        displayMatrix(matrix, currentIteration, totalIterations, activationPercentage, thresholdMultiplier,
                currentHighlightCount);
    }

    private void displayMatrixOnEDT(boolean[][] matrix, int matrixRows, int matrixCols,
            int currentIteration, int totalIterations,
            double activationPercentage, double thresholdMultiplier, int highlightCount) {
        synchronized (INIT_LOCK) {
            if (frame == null)
                initializeFrame();
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
                boolean currentValue = matrix[i][j];
                boolean isWrongBit = isIncorrectBit(i, j, currentValue, highlightCount);
                boolean isBlueHighlighted = isHighlighted(i, j, highlightCount);

                // Check if this is a DataNeuron
                Simulator.Neuron neuron = simulator.getNeuronAt(i, j);
                boolean isDataNeuron = neuron != null && neuron.isDataNeuron();

                // Set background color: orange for active DataNeuron, red for incorrect bits,
                // green for activated, white for inactive
                Color backgroundColor;
                if (isDataNeuron && currentValue) {
                    backgroundColor = Color.ORANGE;
                } else if (isWrongBit) {
                    backgroundColor = Color.RED;
                } else {
                    backgroundColor = currentValue ? Color.GREEN : Color.WHITE;
                }
                cells[i][j].setBackground(backgroundColor);

                // Blue border for output region
                Color borderColor = isBlueHighlighted ? Color.BLUE : Color.BLACK;
                int borderWidth = isBlueHighlighted ? 3 : 1;

                cells[i][j].setBorder(BorderFactory.createLineBorder(borderColor, borderWidth));
            }
        }

        if (totalIterations > 0 && statusLabel != null) {
            statusLabel.setText(String.format("Iterations per update: %d", totalIterations));
        }
        if (activationPercentageLabel != null) {
            activationPercentageLabel.setText(String.format("Activation: %.2f%%", activationPercentage * 100));
        }
        if (thresholdMultiplierLabel != null) {
            thresholdMultiplierLabel.setText(String.format("Threshold: %.5f", thresholdMultiplier));
        }

        // Update progress label if AutoGrader is active
        if (autoGrader != null && progressLabel != null) {
            String currentProgress = autoGrader.getCurrentProgress();
            String furthestProgress = autoGrader.getFurthestProgress();
            progressLabel.setText(String.format("Current: %s | Furthest: %s",
                    currentProgress, furthestProgress));
        }

        // Update distance label if AutoGrader is active
        if (autoGrader != null && distanceLabel != null) {
            int currentDistance = autoGrader.getCurrentDistance();
            char goal = autoGrader.getGoal();
            int previousDistance = autoGrader.getLastAnswerDistance();
            distanceLabel.setText(String.format("Goal: '%c' | Current Distance: %d | Previous Distance: %d | Target: 0",
                    goal, currentDistance, previousDistance));
        }

        // Update resize countdown label if AutoGrader is active
        if (autoGrader != null && resizeCountdownLabel != null) {
            int checksUntilResize = autoGrader.getChecksUntilResize();
            resizeCountdownLabel.setText(String.format("Checks Until Resize: %d", checksUntilResize));
        }

        frame.revalidate();
        frame.repaint();
        frame.setVisible(true);
    }

    private boolean isHighlighted(int row, int col, int highlightCount) {
        if (highlightCount <= 0)
            return false;
        int index = 0;
        for (int i = 0; i < rows && index < highlightCount; i++) {
            for (int j = 0; j < cols && index < highlightCount; j++) {
                if (i == row && j == col)
                    return true;
                index++;
            }
        }
        return false;
    }

    /**
     * Check if a cell has the incorrect bit value compared to the target.
     * Returns true if the cell is in the output region and its value doesn't match
     * the target.
     */
    private boolean isIncorrectBit(int row, int col, boolean currentValue, int highlightCount) {
        if (autoGrader == null || highlightCount <= 0)
            return false;

        // Get the target character's boolean representation
        char goal = autoGrader.getGoal();
        boolean[] targetBits = Utilities.charToBooleanArray(goal, highlightCount);

        // Calculate the index of this cell in the highlighted region
        int index = 0;
        for (int i = 0; i < rows && index < highlightCount; i++) {
            for (int j = 0; j < cols && index < highlightCount; j++) {
                if (i == row && j == col) {
                    // This cell is in the highlighted region at position 'index'
                    if (index < targetBits.length) {
                        return currentValue != targetBits[index];
                    }
                    return false;
                }
                index++;
            }
        }
        return false;
    }

    private void initializeFrame() {
        frame = new JFrame("Boolean Matrix Display");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

        statusLabel = new JLabel("Iterations per update: 0");
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

        distanceLabel = new JLabel("Goal: - | Current Distance: - | Previous Distance: - | Target: 0");
        distanceLabel.setHorizontalAlignment(SwingConstants.CENTER);
        distanceLabel.setFont(new Font("Monospaced", Font.BOLD, 14));

        resizeCountdownLabel = new JLabel("Checks Until Resize: -");
        resizeCountdownLabel.setHorizontalAlignment(SwingConstants.CENTER);
        resizeCountdownLabel.setFont(new Font("Monospaced", Font.BOLD, 14));

        // Create control panel with stacked layout
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Button row
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.add(runCycleButton);
        buttonPanel.add(rewardButton);
        buttonPanel.add(punishButton);

        // Status row
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 2));
        statusPanel.add(statusLabel);
        statusPanel.add(activationPercentageLabel);
        statusPanel.add(thresholdMultiplierLabel);

        // Output row
        JPanel outputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 2));
        outputPanel.add(characterDisplayLabel);

        // Add panels to control panel
        controlPanel.add(buttonPanel);
        controlPanel.add(statusPanel);
        controlPanel.add(outputPanel);

        // Only show progress label if AutoGrader is active
        if (autoGrader != null) {
            JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 2));
            progressPanel.add(progressLabel);
            controlPanel.add(progressPanel);

            JPanel distancePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 2));
            distancePanel.add(distanceLabel);
            controlPanel.add(distancePanel);

            JPanel resizePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 2));
            resizePanel.add(resizeCountdownLabel);
            controlPanel.add(resizePanel);
        }

        frame.add(controlPanel, BorderLayout.SOUTH);

        updateButtonStates();
        update(""); // Use the new update method with empty string for full initial update

        // Maximize the window to fit the entire screen
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLocationRelativeTo(null); // Center on screen
    }

    private void updateButtonStates() {
        SwingUtilities.invokeLater(() -> {
            boolean shouldEnable = !cycleRunning && autoGrader == null;
            if (runCycleButton != null)
                runCycleButton.setEnabled(shouldEnable);
            if (rewardButton != null)
                rewardButton.setEnabled(shouldEnable);
            if (punishButton != null)
                punishButton.setEnabled(shouldEnable);
        });
    }

    /**
     * Reinitialize the display with a new simulator.
     * This clears the current display and sets up for the new simulator.
     * 
     * @param newSimulator The new simulator to display
     */
    public void reinitialize(@NotNull Simulator newSimulator) {
        // Stop any ongoing auto-cycling
        if (autoCycling) {
            stopAutoCycling();
        }

        // Update simulator reference
        simulator = newSimulator;
        if (newSimulator instanceof AutoGrader) {
            autoGrader = (AutoGrader) newSimulator;
        } else {
            autoGrader = null;
        }

        // Set this display on the new simulator
        simulator.setDisplay(this);

        // Reset grid to force recreation with new dimensions
        SwingUtilities.invokeLater(() -> {
            if (gridPanel != null) {
                frame.remove(gridPanel);
                gridPanel = null;
                cells = null;
                rows = 0;
                cols = 0;
            }

            // Trigger a full update to recreate the grid
            update("");

            // Maximize the window to fit the entire screen
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setLocationRelativeTo(null);

            // Restart auto-cycling if it's an AutoGrader
            if (autoGrader != null) {
                startAutoCycling();
            }
        });
    }

    public void updateCell(int row, int col, boolean value) {
        if (cells == null)
            throw new IllegalStateException("No matrix has been displayed yet");
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
                    if (!autoCycling)
                        break; // Double-check after acquiring lock

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
