package nanson;

public interface Neuron extends Comparable<Neuron> {
    void changeOneThing();

    void computeActivation(boolean bit);

    boolean isActivated();

    void clearStake();

    int getStake();

    void updateStake();

    void setIncomingNeuronsAndWeights(Neuron[] incomingNeurons, boolean[] weights);

    Neuron getNextNeuron();

    Neuron[] getIncomingNeurons();

    boolean isDataNeuron();

    int getRow();

    int getCol();
}