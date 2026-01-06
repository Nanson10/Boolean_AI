package nanson;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Represents a neuron that does the main "logic" work.
 *
 * @author Nanson Chen
 * @version 2.0
 */
public class ActivationNeuron implements Neuron {
    /**
     * The index of the layer this neuron belongs to.
     */
    private final int neuronLayerIndex;

    /**
     * The index of this neuron within its layer.
     */
    private final int neuronIndex;

    /**
     * Reference to the containing neuron database.
     */
    private final NeuronDatabase neuronDatabase;

    /**
     * Boolean weights for each incoming connection (true = active).
     */
    private final boolean[] weights;

    /**
     * Indices of incoming neurons in the previous layer.
     */
    private final int[] incomingNeuronIndexes;

    /**
     * Transient stake value used by learning/punishment logic.
     */
    private int stake;

    /**
     * Index of the next neuron to be evaluated from the incoming list.
     */
    private int nextNeuronIndex;

    /**
     * Constructs an ActivationNeuron.
     *
     * @param neuronDatabase              is the database this neuron resides in
     *                                    (should only be constructed via database).
     * @param neuronLayerIndex            is the index of the neuron layer this
     *                                    neuron resides in.
     * @param neuronIndex                 is the index of the neuron of the neuron
     *                                    layer this neuron resides in.
     * @param numberOfIncomingConnections is the number of incoming connections this
     *                                    neuron should have.
     */
    public ActivationNeuron(@NotNull NeuronDatabase neuronDatabase, int neuronLayerIndex, int neuronIndex,
                            int numberOfIncomingConnections) {
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

    /**
     * Picks a random entry in the incomingNeuronIndexes array and replaces it
     * with a random neuron index from the previous neuron layer.
     */
    private void changeRandomIncomingNeuron() {
        Neuron[] previousNeuronLayer = getPotentialInputNeurons();
        int randomIndex = (int) (Math.random() * incomingNeuronIndexes.length);
        int newIncomingNeuronIndex = (int) (Math.random() * previousNeuronLayer.length);
        incomingNeuronIndexes[randomIndex] = previousNeuronLayer[newIncomingNeuronIndex].getNeuronIndex();
    }

    /**
     * Flips a single random weight in the weights array (true -> false, false ->
     * true).
     */
    private void flipRandomWeight() {
        if (weights.length > 0) {
            int randIndex = (int) (Math.random() * weights.length);
            weights[randIndex] = !weights[randIndex];
        }
    }

    /**
     * Changes the nextNeuronIndex to a different random index within the
     * range of available incoming neurons.
     */
    private void changeNextNeuronIndex() {
        int temp = nextNeuronIndex;
        do {
            nextNeuronIndex = (int) (Math.random() * incomingNeuronIndexes.length);
        } while (nextNeuronIndex == temp);
    }

    @Override
    public Neuron[] getPotentialInputNeurons() {
        return neuronDatabase.getNeuronLayer(neuronLayerIndex - 1);
    }

    @Override
    public Neuron[] getIncomingNeurons() {
        ArrayList<Neuron> neurons = new ArrayList<>();
        for (int incomingIndex : incomingNeuronIndexes)
            neurons.add(getPotentialInputNeurons()[incomingIndex]);
        return neurons.toArray(new Neuron[0]);
    }

    @Override
    public Neuron getNextNeuron() {
        if (nextNeuronIndex >= incomingNeuronIndexes.length) {
            nextNeuronIndex = 0;
        }
        return getPotentialInputNeurons()[nextNeuronIndex++];
    }

    @Override
    public boolean computeActivation(boolean bit) {
        updateStake(); // Increase the stake for this neuron and neurons that contribute to the state
        // of this neuron.
        return evaluateActivation(bit);
    }

    /**
     * Computes the threshold value used to determine activation. Current
     * implementation returns half of the number of incoming connections.
     *
     * @return threshold (half the number of inputs)
     */
    private int getThreshold() {
        return incomingNeuronIndexes.length / 2;
    }

    /**
     * Evaluate whether this neuron should activate based on incoming neurons
     * and the supplied bias bit.
     *
     * @param addOne if true, adds one to the activation sum (bias)
     * @return true if activation sum reaches or exceeds the threshold
     */
    private boolean evaluateActivation(boolean addOne) {
        int activationSum = (addOne ? 1 : 0);
        for (int i = 0; i < incomingNeuronIndexes.length && i < weights.length; i++) {
            if (weights[i] && getPotentialInputNeurons()[incomingNeuronIndexes[i]].computeActivation(false))
                activationSum++;
            if (activationSum >= getThreshold())
                return true;
        }
        return false;
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
            Neuron neuron = getPotentialInputNeurons()[neuronIndex];
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

    @Override
    public void punishByDepth(int denominatorOfProbability) {
        if (denominatorOfProbability <= 0 || denominatorOfProbability > 1000000) // If invalid probability or too
            // unlikely, stop punishment.
            return;
        double mutationChance = 1.0 / denominatorOfProbability;
        if (Math.random() < mutationChance) {
            changeOneThing();
        }
        int newProbabilityForIncomingNeurons = denominatorOfProbability * incomingNeuronIndexes.length; // Increase
        // denominator
        // to reduce
        // chance for
        // incoming
        // neurons.
        for (int neuronIndex : incomingNeuronIndexes) {
            Neuron neuron = getPotentialInputNeurons()[neuronIndex];
            neuron.punishByDepth(newProbabilityForIncomingNeurons);
        }
    }
}
