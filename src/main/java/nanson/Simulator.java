package nanson;

import java.util.ArrayList;

import org.jetbrains.annotations.NotNull;

public class Simulator {
    private final Neuron[][] NEURONS;
    private double activationThresholdMultiplier;
    private double activationPercentage;
    private final int RUN_NEURONS_PER_CYCLE;
    private final int NUM_INCOMING_NEURONS;
    private BooleanMatrixDisplay display;
    private int currentIteration;
    private int totalIterations;
    private int highlightCount;
    private StringBuilder changedCells;
    private Neuron nextNeuron;

    public Simulator() {
        this(Constants.DEFAULT_MATRIX_WIDTH, Constants.DEFAULT_MATRIX_HEIGHT, Constants.DEFAULT_RUN_NEURONS_PER_CYCLE,
                Constants.DEFAULT_NUM_INCOMING_NEURONS);
    }

    public Simulator(int width, int height, int runNeuronsPerCycle, int numIncomingNeurons) {
        NEURONS = new Neuron[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                NEURONS[i][j] = new Neuron(this, i, j);
            }
        }
        activationThresholdMultiplier = 0.5;
        activationPercentage = 0.0;
        RUN_NEURONS_PER_CYCLE = runNeuronsPerCycle;
        NUM_INCOMING_NEURONS = numIncomingNeurons;
        changedCells = new StringBuilder();

        for (Neuron[] neuronRow : NEURONS) {
            for (Neuron neuron : neuronRow) {
                setRandomIncomingNeuronsAndWeights(neuron);
            }
        }
    }

    public boolean[][] getCurrentMatrixState() {
        boolean[][] matrixState = new boolean[NEURONS.length][NEURONS[0].length];
        for (int i = 0; i < NEURONS.length; i++) {
            for (int j = 0; j < NEURONS[0].length; j++) {
                matrixState[i][j] = NEURONS[i][j].isActivated();
            }
        }
        return matrixState;
    }

    public double getActivationPercentage() {
        return activationPercentage;
    }

    public int getCurrentIteration() {
        return currentIteration;
    }

    public int getTotalIterations() {
        return totalIterations;
    }

    public int getHighlightCount() {
        return highlightCount;
    }

    public void setDisplay(BooleanMatrixDisplay display) {
        this.display = display;
    }

    private void recordCellChange(int row, int col, boolean activated) {
        if (changedCells.length() > 0) {
            changedCells.append(Constants.CELL_DELIMITER);
        }
        changedCells.append(row)
                .append(Constants.FIELD_DELIMITER)
                .append(col)
                .append(Constants.FIELD_DELIMITER)
                .append(activated ? "1" : "0");
    }

    private void clearChanges() {
        changedCells.setLength(0);
    }

    private String getChangesString() {
        return changedCells.toString();
    }

    public void setRandomIncomingNeuronsAndWeights(Neuron neuron) {
        Neuron[] incomingNeurons = new Neuron[NUM_INCOMING_NEURONS];
        boolean[] weights = new boolean[NUM_INCOMING_NEURONS];
        for (int i = 0; i < NUM_INCOMING_NEURONS; i++) {
            int randRow = (int) (Math.random() * NEURONS.length);
            int randCol = (int) (Math.random() * NEURONS[0].length);
            incomingNeurons[i] = NEURONS[randRow][randCol];
            weights[i] = Math.random() < 0.5;
        }
        neuron.setIncomingNeuronsAndWeights(incomingNeurons, weights);
    }

    private void updateDisplay() {
        if (display != null) {
            display.update(getChangesString());
            clearChanges();
        }
    }

    public boolean[] runCycle(int lengthOfResults) {
        // Clear highlights at the start of the cycle
        highlightCount = 0;
        currentIteration = 0;
        totalIterations = RUN_NEURONS_PER_CYCLE;
        updateDisplay();

        for (int i = 0; i < RUN_NEURONS_PER_CYCLE; i++) {
            if (nextNeuron == null)
                nextNeuron = NEURONS[0][0];
            nextNeuron.computeActivation();
            nextNeuron = nextNeuron.getNextNeuron();
            currentIteration++;
            updateDisplay();
        }

        // Highlight the result cells
        highlightCount = lengthOfResults;
        updateDisplay();

        boolean[] results = new boolean[lengthOfResults];
        int index = 0;
        for (int i = NEURONS.length - 1; i >= 0 && index < lengthOfResults; i--) {
            for (int j = NEURONS[0].length - 1; j >= 0 && index < lengthOfResults; j--) {
                results[index++] = NEURONS[i][j].isActivated();
            }
        }
        return results;
    }

    public double getActivationThresholdMultiplier() {
        updateActivationThresholdMultiplier();
        return activationThresholdMultiplier;
    }

    public void updateActivationThresholdMultiplier() {
        activationPercentage = 0;
        for (Neuron[] neuronRow : NEURONS) {
            for (Neuron neuron : neuronRow) {
                if (neuron.isActivated()) {
                    activationPercentage += 1.0;
                }
            }
        }
        activationPercentage /= (NEURONS.length * NEURONS[0].length);
        // activationThresholdMultiplier +=
        // LogarithmicSlowingFactor(activationPercentage - 0.5, 1, 10) *
        // (activationPercentage - 0.5);
        activationThresholdMultiplier = activationPercentage;
    }

    public double LogarithmicSlowingFactor(double x, int domain, double base) {
        double normalizedX = Math.abs(x) / domain + 1;
        return Math.log(normalizedX) / Math.log(base);
    }

    public void stimulate(boolean goodIfTrue) {
        ArrayList<Neuron> neuronList = new ArrayList<>();
        for (Neuron[] neuronRow : NEURONS) {
            for (Neuron neuron : neuronRow) {
                neuronList.add(neuron);
            }
        }

        neuronList.sort(Neuron::compareTo);
        if (goodIfTrue) { // Neurons with low stake are punished
            int lowestStake = neuronList.get(0).stake;
            for (int i = 0; i < neuronList.size(); i--)
                if (neuronList.get(i).stake == lowestStake) {
                    neuronList.get(i).changeOneThing();
                } else
                    break;
        } else { // Neurons with high stake are punished
            int highestStake = neuronList.get(neuronList.size() - 1).stake;
            for (int i = neuronList.size() - 1; i >= neuronList.size(); i--)
                if (neuronList.get(i).stake == highestStake) {
                    neuronList.get(i).changeOneThing();
                } else {
                    break;
                }
            neuronList.forEach((n) -> n.clearStake());
        }
    }

    private static class Neuron implements Comparable<Neuron> {
        private boolean activated;
        private Neuron[] incomingNeurons;
        private boolean[] weights;
        private Simulator simulator;
        private int stake;
        private final int row;
        private final int col;
        private int nextNeuronIndex;

        public Neuron(@NotNull Simulator simulator, int row, int col) {
            this.activated = false;
            this.simulator = simulator;
            this.row = row;
            this.col = col;
            stake = 0;
            nextNeuronIndex = 0;
        }

        public void changeOneThing() {
            switch ((int) (Math.random() * 3)) {
                case 0:
                    // Change a random incoming neuron
                    if (incomingNeurons.length > 0) {
                        int randIndex = (int) (Math.random() * incomingNeurons.length);
                        int randRow = (int) (Math.random() * simulator.NEURONS.length);
                        int randCol = (int) (Math.random() * simulator.NEURONS[0].length);
                        incomingNeurons[randIndex] = simulator.NEURONS[randRow][randCol];
                    }
                    break;
                case 1:
                    // Flip a random weight
                    if (weights.length > 0) {
                        int randIndex = (int) (Math.random() * weights.length);
                        weights[randIndex] = !weights[randIndex];
                    }
                    break;
                case 2:
                    // Change nextNuronIndex to a random position
                    if (incomingNeurons.length > 0) {
                        int temp = nextNeuronIndex;
                        do {
                            nextNeuronIndex = (int) (Math.random() * incomingNeurons.length);
                        } while (nextNeuronIndex == temp);
                    }
            }
        }

        public void setIncomingNeuronsAndWeights(@NotNull Neuron[] incomingNeurons, @NotNull boolean[] weights) {
            this.incomingNeurons = incomingNeurons;
            this.weights = weights;
            nextNeuronIndex = 0;
        }

        public Neuron getNextNeuron() {
            if (nextNeuronIndex >= incomingNeurons.length) {
                nextNeuronIndex = 0;
            }
            return incomingNeurons[nextNeuronIndex++];
        }

        public boolean computeActivation() {
            stake++;
            int threshold = (int) (simulator.getActivationThresholdMultiplier() * incomingNeurons.length);
            int activationSum = 0;
            boolean oldActivated = activated;

            for (int i = 0; i < incomingNeurons.length && i < weights.length; i++) {
                if (weights[i] && incomingNeurons[i].activated) {
                    activationSum++;
                }
                if (activationSum >= threshold) {
                    activated = true;
                    if (oldActivated != activated) {
                        simulator.recordCellChange(row, col, activated);
                    }
                    return true;
                }
            }
            activated = false;
            if (oldActivated != activated) {
                simulator.recordCellChange(row, col, activated);
            }
            return false;
        }

        public boolean isActivated() {
            return activated;
        }

        public void clearStake() {
            stake = 0;
        }

        @Override
        public int compareTo(Neuron o) {
            return Integer.compare(stake, o.stake);
        }
    }
}