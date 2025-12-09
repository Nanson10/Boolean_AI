package nanson;

import java.util.ArrayList;

import org.jetbrains.annotations.NotNull;

public class Simulator {
    private final Neuron[][] NEURONS;
    private double activationThresholdMultiplier;
    private double activationPercentage;
    private final int RUN_NEURONS_PER_CYCLE;
    private final int NUM_INCOMING_NEURONS;

    public Simulator(int width, int height, int runNeuronsPerCycle, int numIncomingNeurons) {
        NEURONS = new Neuron[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                NEURONS[i][j] = new Neuron(this);
            }
        }
        activationThresholdMultiplier = 0.0;
        activationPercentage = 0.0;
        RUN_NEURONS_PER_CYCLE = runNeuronsPerCycle;
        NUM_INCOMING_NEURONS = numIncomingNeurons;

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

    public void setRandomIncomingNeuronsAndWeights(Neuron neuron) {
        Neuron[] incomingNeurons = new Neuron[NUM_INCOMING_NEURONS];
        boolean[] weights = new boolean[NUM_INCOMING_NEURONS];
        for (int i = 0; i < NUM_INCOMING_NEURONS; i++) {
            int randRow = (int) (Math.random() * NEURONS.length);
            int randCol = (int) (Math.random() * NEURONS[0].length);
            incomingNeurons[i] = NEURONS[randRow][randCol];
            weights[i] = Math.random() < 0.5;
        }
        neuron.setIncomingNeurons(incomingNeurons);
        neuron.setWeights(weights);
    }

    public void updateDisplay() {
        BooleanMatrixDisplay.displayMatrix(getCurrentMatrixState(), 0, 0, activationPercentage, activationThresholdMultiplier, 0);
    }

    public void updateDisplay(int currentIteration, int totalIterations) {
        BooleanMatrixDisplay.displayMatrixPreserveHighlight(getCurrentMatrixState(), currentIteration, totalIterations, activationPercentage, activationThresholdMultiplier);
    }

    public void updateDisplayWithHighlight(int highlightCount) {
        BooleanMatrixDisplay.displayMatrix(getCurrentMatrixState(), 0, 0, activationPercentage, activationThresholdMultiplier, highlightCount);
    }

    public boolean[] runCycle(int lengthOfResults) {
        // Clear highlights at the start of the cycle
        updateDisplayWithHighlight(0);
        updateDisplay(0, RUN_NEURONS_PER_CYCLE);
        for (int i = 0; i < RUN_NEURONS_PER_CYCLE; i++) {
            int randRow = (int) (Math.random() * NEURONS.length);
            int randCol = (int) (Math.random() * NEURONS[0].length);
            NEURONS[randRow][randCol].computeActivation();
            updateDisplay(i + 1, RUN_NEURONS_PER_CYCLE);
        }
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
        activationThresholdMultiplier += QuadraticSlowingFactor(activationPercentage - 0.5, 1) * (activationPercentage - 0.5);
    }

    public double QuadraticSlowingFactor(double x, int domain) {
        double normalizedX = Math.min(Math.abs(x) / domain, 1.0);
        return Math.pow(normalizedX, 2);
    }

    public void stimulate(boolean goodIfTrue) {
        ArrayList<Neuron> neuronList = new ArrayList<>();
        for (Neuron[] neuronRow : NEURONS) {
            for (Neuron neuron : neuronRow) {
                neuronList.add(neuron);
            }
        }
        neuronList.sort(Neuron::compareTo);
        int neuronOnEnd = (int)(0.1 * neuronList.size());
        if (goodIfTrue) { // Neurons with low stake are punished
            for (int i = 0; i < neuronOnEnd; i++) {
                setRandomIncomingNeuronsAndWeights(neuronList.get(i));
            }
        } else { // Neurons with high stake are punished
            for (int i = neuronList.size() - neuronOnEnd; i < neuronList.size(); i++) {
                setRandomIncomingNeuronsAndWeights(neuronList.get(i));
            }
        }
        neuronList.forEach((n) -> n.clearStake());
    }

    private static class Neuron implements Comparable<Neuron>{
        private boolean activated;
        private Neuron[] incomingNeurons;
        private boolean[] weights;
        private Simulator simulator;
        private int stake;

        public Neuron(@NotNull Simulator simulator) {
            this.activated = false;
            this.simulator = simulator;
            stake = 0;
        }

        public void setIncomingNeurons(@NotNull Neuron[] incomingNeurons) {
            this.incomingNeurons = incomingNeurons;
        }

        public void setWeights(@NotNull boolean[] weights) {
            this.weights = weights;
        }

        public boolean computeActivation() {
            stake++;
            int threshold = (int)(simulator.getActivationThresholdMultiplier() * incomingNeurons.length);
            int activationSum = 0;
            for (int i = 0; i < incomingNeurons.length && i < weights.length; i++) {
                if (weights[i] && incomingNeurons[i].activated) {
                    activationSum++;
                }
                if (activationSum >= threshold) {
                    activated = true;
                    return true;
                }
            }
            activated = false;
            return false;
        }

        public boolean isActivated() {
            return activated;
        }

        public int getStake() {
            return stake;
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