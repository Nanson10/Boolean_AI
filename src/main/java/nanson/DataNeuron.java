package nanson;

import org.jetbrains.annotations.NotNull;

public class DataNeuron implements Neuron {
    private final int row;
    private final int col;
    private boolean activated;
    private final Simulator simulator;

    public DataNeuron(@NotNull Simulator simulator, int row, int col) {
        this.activated = false;
        this.simulator = simulator;
        this.row = row;
        this.col = col;
    }

    @Override
    public void changeOneThing() {
        // DataNeuron doesn't mutate - it's controlled externally
    }

    @Override
    public void computeActivation(boolean bit) {
        // DataNeuron's activation depends ONLY on the bit passed in
        boolean oldActivated = activated;
        activated = bit;

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
        return 0; // DataNeuron doesn't have stake
    }

    @Override
    public void updateStake() {
        // DataNeuron doesn't have stake
    }

    @Override
    public void clearStake() {
        // DataNeuron doesn't have stake
    }

    @Override
    public void setIncomingNeuronsAndWeights(Neuron[] incomingNeurons, boolean[] weights) {
        // DataNeuron doesn't have incoming neurons
    }

    @Override
    public Neuron[] getIncomingNeurons() {
        return new Neuron[0]; // DataNeuron doesn't have incoming neurons
    }

    @Override
    public boolean isDataNeuron() {
        return true;
    }

    @Override
    public int compareTo(Neuron o) {
        return 0; // DataNeurons are all equal in comparison
    }

    @Override
    public int getRow() {
        return row;
    }

    @Override
    public int getCol() {
        return col;
    }

    @Override
    public Neuron getNextNeuron() {
        return null;
    }
}
