package nanson;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Represents a neuron that does the main "logic" work.
 *
 * @author Nanson Chen
 * @version December 11th, 2025
 */
public class ActivationNeuron implements Neuron {
    private final int neuronLayerIndex;
    private final int neuronIndex;
    private final NeuronDatabase neuronDatabase;
    private final boolean[] weights;
    private final int[] incomingNeuronIndexes;
    private boolean activated;
    private int stake;
    private int nextNeuronIndex;

    /**
     * Constructs an ActivationNeuron.
     *
     * @param neuronDatabase              is the database this neuron resides in (should only be constructed via database).
     * @param neuronLayerIndex            is the index of the neuron layer this neuron resides in.
     * @param neuronIndex                 is the index of the neuron of the neuron layer this neuron resides in.
     * @param numberOfIncomingConnections is the number of incoming connections this neuron should have.
     */
    public ActivationNeuron(@NotNull NeuronDatabase neuronDatabase, int neuronLayerIndex, int neuronIndex,
                            int numberOfIncomingConnections) {
        this.activated = false;
        this.neuronDatabase = neuronDatabase;
        this.neuronLayerIndex = neuronLayerIndex;
        this.neuronIndex = neuronIndex;
        stake = 0;
        nextNeuronIndex = 0;
        incomingNeuronIndexes = new int[numberOfIncomingConnections];
        weights = new boolean[numberOfIncomingConnections];
    }

    @Override
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
        Neuron[] previousNeuronLayer = getPreviousNeuronLayer();
        int randomIndex = (int) (Math.random() * incomingNeuronIndexes.length);
        int newIncomingNeuronIndex = (int) (Math.random() * previousNeuronLayer.length);
        incomingNeuronIndexes[randomIndex] = previousNeuronLayer[newIncomingNeuronIndex].getNeuronIndex();
    }

    private void flipRandomWeight() {
        if (weights.length > 0) {
            int randIndex = (int) (Math.random() * weights.length);
            weights[randIndex] = !weights[randIndex];
        }
    }

    private void changeNextNeuronIndex() {
        int temp = nextNeuronIndex;
        do {
            nextNeuronIndex = (int) (Math.random() * incomingNeuronIndexes.length);
        } while (nextNeuronIndex == temp);
    }

    @Override
    public Neuron[] getPreviousNeuronLayer() {
        return neuronDatabase.getNeuronLayer(neuronLayerIndex - 1);
    }

    @Override
    public Neuron[] getIncomingNeurons() {
        ArrayList<Neuron> neurons = new ArrayList<>();
        for (int incomingIndex : incomingNeuronIndexes)
            neurons.add(getPreviousNeuronLayer()[incomingIndex]);
        return neurons.toArray(new Neuron[0]);
    }

    @Override
    public Neuron getNextNeuron() {
        if (nextNeuronIndex >= incomingNeuronIndexes.length) {
            nextNeuronIndex = 0;
        }
        return getPreviousNeuronLayer()[nextNeuronIndex++];
    }

    @Override
    public void computeActivation(boolean bit) {
        updateStake(); // Increase the stake for this neuron and neurons that contribute to the state of this neuron.
        updateActivationState(evaluateActivation(bit));
    }

    private int getThreshold() {
        return incomingNeuronIndexes.length / 2;
    }

    private boolean evaluateActivation(boolean addOne) {
        int activationSum = (addOne ? 1 : 0);
        for (int i = 0; i < incomingNeuronIndexes.length && i < weights.length; i++) {
            if (weights[i] && getPreviousNeuronLayer()[incomingNeuronIndexes[i]].isActivated())
                activationSum++;
            if (activationSum >= getThreshold())
                return true;
        }
        return false;
    }

    private void updateActivationState(boolean shouldActivate) {
        boolean oldActivated = activated;
        activated = shouldActivate;

        /* if (oldActivated != activated) {
            neuronDatabase.recordCellChange(neuronLayerIndex, neuronIndex, activated);
        } */

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
        for (int neuronIndex : incomingNeuronIndexes) {
            Neuron neuron = getPreviousNeuronLayer()[neuronIndex];
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
    public int getNeuronLayerIndex() {
        return neuronLayerIndex;
    }

    @Override
    public int getNeuronIndex() {
        return neuronIndex;
    }
}
