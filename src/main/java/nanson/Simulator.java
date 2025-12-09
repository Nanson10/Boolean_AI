package nanson;

import java.util.function.Supplier;

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
        BooleanMatrixDisplay.displayMatrix(getCurrentMatrixState());
    }

    public boolean[] runCycle(int lengthOfResults) {
        updateDisplay();
        for (int i = 0; i < RUN_NEURONS_PER_CYCLE; i++) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            int randRow = (int) (Math.random() * NEURONS.length);
            int randCol = (int) (Math.random() * NEURONS[0].length);
            NEURONS[randRow][randCol].computeActivation();
            updateDisplay();
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
        activationThresholdMultiplier += activationPercentage - 0.5;
        if (activationPercentage < 0.4) {
            activationThresholdMultiplier -= 0.1;
        } else if (activationPercentage > 0.6) {
            activationThresholdMultiplier += 0.1;
        }
    }

    private static class Neuron {
        private boolean activated;
        private Neuron[] incomingNeurons;
        private boolean[] weights;
        private Simulator simulator;

        public Neuron(@NotNull Simulator simulator) {
            this.activated = false;
            this.simulator = simulator;
        }

        public void setIncomingNeurons(@NotNull Neuron[] incomingNeurons) {
            this.incomingNeurons = incomingNeurons;
        }

        public void setWeights(@NotNull boolean[] weights) {
            this.weights = weights;
        }

        public boolean computeActivation() {
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
    }
}