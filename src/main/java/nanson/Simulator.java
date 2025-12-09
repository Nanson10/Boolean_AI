package nanson;

import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

public class Simulator {
    private final Neuron[][] NEURONS;
    private double activationPercentage;
    private final int RUN_NEURONS_PER_CYCLE;

    public Simulator(int width, int height, int runNeuronsPerCycle) {
        NEURONS = new Neuron[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                NEURONS[i][j] = new Neuron(this);
            }
        }
        activationPercentage = 0.0;
        RUN_NEURONS_PER_CYCLE = runNeuronsPerCycle;
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

    public void updateDisplay() {
        BooleanMatrixDisplay.displayMatrix(getCurrentMatrixState());
    }

    public Supplier<boolean[]> runCycle(int lengthOfResults) {
        return () -> {
            updateDisplay();
            for (int i = 0; i < RUN_NEURONS_PER_CYCLE; i++) {
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
        };
    }

    public double getActivationPercentage() {
        return activationPercentage;
    }

    public void updateActivationPercentage() {
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
            int threshold = (int)(simulator.getActivationPercentage() * incomingNeurons.length);
            int activationSum = 0;
            for (Neuron neuron : incomingNeurons) {
                if (neuron.isActivated() && activationSum++ >= threshold) {
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

        public void activate() {
            this.activated = true;
        }

        public void deactivate() {
            this.activated = false;
        }
    }
}