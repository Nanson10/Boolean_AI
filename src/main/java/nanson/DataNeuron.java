package nanson;

/**
 * A data neuron is a "neuron" that can be activated directly.
 *
 * @author Nanson Chen
 * @version December 11th, 2025
 */
public class DataNeuron implements Neuron {
    private final int neuronLayerIndex;
    private final int neuronIndex;
    private boolean activated;

    /**
     * Constructs a DataNeuron.
     *
     * @param neuronLayerIndex is the index of the neuron layer this neuron resides in.
     * @param neuronIndex      is the index of the neuron in the neuron layer that this neuron resides in.
     */
    public DataNeuron(int neuronLayerIndex, int neuronIndex) {
        this.activated = false;
        this.neuronLayerIndex = neuronLayerIndex;
        this.neuronIndex = neuronIndex;
    }

    @Override
    public void changeOneThing() {
        // DataNeuron doesn't mutate - it's controlled externally
    }

    @Override
    public void computeActivation(boolean bit) {
        // DataNeuron's activation depends ONLY on the bit passed in
        activated = bit;
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
    public Neuron[] getPreviousNeuronLayer() {
        return new Neuron[0]; // DataNeuron doesn't have incoming neurons
    }

    @Override
    public Neuron[] getIncomingNeurons() {
        return new Neuron[0];
    }

    @Override
    public boolean isDataNeuron() {
        return true;
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
    public Neuron getNextNeuron() {
        return null;
    }
}
