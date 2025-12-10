package nanson;

import java.util.ArrayList;

import org.jetbrains.annotations.NotNull;

public class Simulator {
    protected final Neuron[][] NEURONS;
    private double activationThresholdMultiplier;
    private double activationPercentage;
    protected int runNeuronsPerCycle;
    private final int NUM_INCOMING_NEURONS;
    protected BooleanMatrixDisplay display;
    protected int currentIteration;
    private int highlightCount;
    private StringBuilder changedCells;

    public Simulator() {
        this(Constants.DEFAULT_MATRIX_WIDTH, Constants.DEFAULT_MATRIX_HEIGHT, Constants.DEFAULT_RUN_NEURONS_PER_CYCLE,
                Constants.DEFAULT_NUM_INCOMING_NEURONS);
    }

    public Simulator(Neuron[][] neurons, int runNeuronsPerCycle, int numIncomingNeurons) {
        NEURONS = neurons;
        this.runNeuronsPerCycle = runNeuronsPerCycle;
        NUM_INCOMING_NEURONS = numIncomingNeurons;
        activationThresholdMultiplier = 0.5;
        activationPercentage = 0.0;
        changedCells = new StringBuilder();
    }

    public Simulator(int width, int height, int runNeuronsPerCycle, int numIncomingNeurons) {
        NEURONS = new Neuron[height][width];
        initializeNeurons();

        activationThresholdMultiplier = 0.5;
        activationPercentage = 0.0;
        this.runNeuronsPerCycle = runNeuronsPerCycle;
        NUM_INCOMING_NEURONS = numIncomingNeurons;
        changedCells = new StringBuilder();

        initializeNeuronConnections();
    }

    private void initializeNeurons() {
        for (int i = 0; i < NEURONS.length; i++) {
            for (int j = 0; j < NEURONS[0].length; j++) {
                NEURONS[i][j] = new Neuron(this, i, j);
            }
        }
    }

    private void initializeNeuronConnections() {
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
        return NEURONS.length * NEURONS.length * NEURONS.length * NEURONS[0].length;
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
        initializeCycle();
        executeNeuronComputations(lengthOfResults);
        highlightResults(lengthOfResults);
        return collectResults(lengthOfResults);
    }

    private void initializeCycle() {
        highlightCount = 0;
        currentIteration = 0;
        updateDisplay();
    }

    protected void executeNeuronComputations(int lengthOfResults) {
        for (int n = 0; n < NEURONS.length * NEURONS.length; n++) {
            for (int i = 0; i < NEURONS.length; i++) {
                for (int j = 0; j < NEURONS[0].length; j++) {
                    if ((int) (Math.random() * 10000) == 0)
                        NEURONS[i][j].changeOneThing();
                    NEURONS[i][j].computeActivation(false);
                    currentIteration++;
                }
            }
        }
    }

    private void highlightResults(int lengthOfResults) {
        highlightCount = lengthOfResults;
        updateDisplay();
    }

    private boolean[] collectResults(int lengthOfResults) {
        boolean[] results = new boolean[lengthOfResults];
        int index = 0;
        for (int i = 0; i < NEURONS.length && index < lengthOfResults; i++) {
            for (int j = 0; j < NEURONS[0].length && index < lengthOfResults; j++) {
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
        calculateActivationPercentage();
        adjustThresholdMultiplier();
    }

    private void calculateActivationPercentage() {
        activationPercentage = 0;
        for (Neuron[] neuronRow : NEURONS) {
            for (Neuron neuron : neuronRow) {
                if (neuron.isActivated()) {
                    activationPercentage += 1.0;
                }
            }
        }
        activationPercentage /= (NEURONS.length * NEURONS[0].length);
    }

    private void adjustThresholdMultiplier() {
        // activationThresholdMultiplier +=
        // LogarithmicSlowingFactor(activationPercentage - 0.5, 1, 10)
        // * (activationPercentage - 0.5);
        activationThresholdMultiplier += powerSlowingFactor(activationPercentage - 0.5, 5,
                1.5) * (activationPercentage - 0.5);
    }

    public double powerSlowingFactor(double x, double domain, double power) {
        double normalizedX = Math.abs(x) / domain;
        return Math.min(1, Math.pow(normalizedX, power));
    }

    public double logarithmicSlowingFactor(double x, int domain, double base) {
        double normalizedX = Math.abs(x) / domain + 1;
        return Math.log(normalizedX) / Math.log(base);
    }

    public void stimulate(boolean goodIfTrue) {
        ArrayList<Neuron> sortedNeurons = getSortedNeuronsByStake();

        if (goodIfTrue) {
            punishLowStakeNeurons(sortedNeurons);
        } else {
            punishHighStakeNeurons(sortedNeurons);
        }
    }

    private ArrayList<Neuron> getSortedNeuronsByStake() {
        ArrayList<Neuron> neuronList = new ArrayList<>();
        for (Neuron[] neuronRow : NEURONS) {
            for (Neuron neuron : neuronRow) {
                neuronList.add(neuron);
            }
        }
        neuronList.sort(Neuron::compareTo);
        return neuronList;
    }

    private void punishLowStakeNeurons(ArrayList<Neuron> sortedNeurons) {
        int lowestStake = sortedNeurons.get(0).stake;
        for (Neuron neuron : sortedNeurons) {
            if (neuron.stake == lowestStake) {
                neuron.changeOneThing();
            } else {
                break;
            }
        }
    }

    private void punishHighStakeNeurons(ArrayList<Neuron> sortedNeurons) {
        int highestStake = sortedNeurons.get(sortedNeurons.size() - 1).stake;
        for (int i = sortedNeurons.size() - 1; i >= 0; i--) {
            if (sortedNeurons.get(i).stake == highestStake) {
                sortedNeurons.get(i).changeOneThing();
            } else {
                break;
            }
        }
        sortedNeurons.forEach(Neuron::clearStake);
    }

    public static class Neuron implements Comparable<Neuron> {
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
            int changeType = (int) (Math.random() * 3);
            switch (changeType) {
                case 0:
                    changeRandomIncomingNeuron();
                    break;
                case 1:
                    flipRandomWeight();
                    break;
                case 2:
                    changeNextNeuronIndex();
                    break;
            }
        }

        private void changeRandomIncomingNeuron() {
            if (incomingNeurons.length > 0) {
                int randIndex = (int) (Math.random() * incomingNeurons.length);
                int randRow = (int) (Math.random() * simulator.NEURONS.length);
                int randCol = (int) (Math.random() * simulator.NEURONS[0].length);
                incomingNeurons[randIndex] = simulator.NEURONS[randRow][randCol];
            }
        }

        private void flipRandomWeight() {
            if (weights.length > 0) {
                int randIndex = (int) (Math.random() * weights.length);
                weights[randIndex] = !weights[randIndex];
            }
        }

        private void changeNextNeuronIndex() {
            if (incomingNeurons.length > 0) {
                int temp = nextNeuronIndex;
                do {
                    nextNeuronIndex = (int) (Math.random() * incomingNeurons.length);
                } while (nextNeuronIndex == temp);
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

        public boolean computeActivation(boolean bit) {
            for (Neuron neuron : incomingNeurons)
                neuron.stake++; // Increase the stake for neurons that contribute to the state of this neuron.
            int threshold = calculateThreshold();
            int activationSum = calculateActivationSum(threshold);
            return updateActivationState(activationSum + (bit ? 1 : 0) >= threshold);
        }

        private int calculateThreshold() {
            return (int) (simulator.getActivationThresholdMultiplier() * incomingNeurons.length);
        }

        private int calculateActivationSum(int threshold) {
            int activationSum = 0;
            for (int i = 0; i < incomingNeurons.length && i < weights.length; i++) {
                if (weights[i] && incomingNeurons[i].activated) {
                    activationSum++;
                }
                if (activationSum >= threshold) {
                    break;
                }
            }
            return activationSum;
        }

        private boolean updateActivationState(boolean shouldActivate) {
            boolean oldActivated = activated;
            activated = shouldActivate;

            if (oldActivated != activated) {
                simulator.recordCellChange(row, col, activated);
            }

            return activated;
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