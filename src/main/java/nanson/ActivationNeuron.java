package nanson;

import org.jetbrains.annotations.NotNull;

public class ActivationNeuron implements Neuron {
    private final int row;
    private final int col;
    private boolean activated;
    private Neuron[] incomingNeurons;
    private boolean[] weights;
    private final Simulator simulator;
    private int stake;
    private int nextNeuronIndex;

    public ActivationNeuron(@NotNull Simulator simulator, int row, int col) {
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

    @Override
    public void setIncomingNeuronsAndWeights(@NotNull Neuron[] incomingNeurons,
                                             boolean @NotNull [] weights) {
        this.incomingNeurons = incomingNeurons;
        this.weights = weights;
        nextNeuronIndex = 0;
    }

    @Override
    public Neuron[] getIncomingNeurons() {
        return incomingNeurons;
    }

    @Override
    public Neuron getNextNeuron() {
        if (nextNeuronIndex >= incomingNeurons.length) {
            nextNeuronIndex = 0;
        }
        return incomingNeurons[nextNeuronIndex++];
    }

    @Override
    public void computeActivation(boolean bit) {
        updateStake(); // Increase the stake for this neuron and neurons that contribute to the state
        // of this neuron.
        int threshold = calculateThreshold();
        int activationSum = calculateActivationSum(threshold);
        updateActivationState(activationSum + (bit ? 1 : 0) >= threshold);
    }

    private int calculateThreshold() {
        return (int) Math.round(simulator.getActivationThresholdMultiplier() * incomingNeurons.length);
    }

    private int calculateActivationSum(int threshold) {
        int activationSum = 0;
        for (int i = 0; i < incomingNeurons.length && i < weights.length; i++) {
            if (weights[i] && incomingNeurons[i].isActivated()) {
                activationSum++;
            }
            if (activationSum >= threshold) {
                break;
            }
        }
        return activationSum;
    }

    private void updateActivationState(boolean shouldActivate) {
        boolean oldActivated = activated;
        activated = shouldActivate;

        if (oldActivated != activated) {
            simulator.recordCellChange(row, col, activated);
        }

    }

    @Override
    public boolean isActivated() {
        return activated;
    }

    @Override
    public int getStake() {
        return stake;
    }

    @Override
    public void updateStake() {
        updateStake(5, 0);
    }

    private void updateStake(int maxDepth, int currDepth) {
        if (currDepth > maxDepth)
            return;
        stake += (int) Math.pow(2, maxDepth - currDepth);
        for (Neuron neuron : incomingNeurons) {
            if (neuron instanceof ActivationNeuron) {
                ((ActivationNeuron) neuron).updateStake(maxDepth, currDepth + 1);
            }
        }
    }

    @Override
    public void clearStake() {
        stake = 0;
    }

    @Override
    public boolean isDataNeuron() {
        return false;
    }

    @Override
    public int compareTo(Neuron o) {
        return Integer.compare(stake, o.getStake());
    }

    @Override
    public int getRow() {
        return row;
    }

    @Override
    public int getCol() {
        return col;
    }
}
