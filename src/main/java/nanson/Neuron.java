package nanson;

import java.io.Serializable;

public interface Neuron extends Comparable<Neuron>, Serializable {
    void changeOneThing();

    void computeActivation(boolean bit);

    boolean isActivated();

    void clearStake();

    int getStake();

    void updateStake();

    Neuron getNextNeuron();

    Neuron[] getPreviousNeuronLayer();

    boolean isDataNeuron();

    int getNeuronLayerIndex();

    int getNeuronIndex();
}