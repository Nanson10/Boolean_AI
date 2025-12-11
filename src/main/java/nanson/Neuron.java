package nanson;

import java.io.Serializable;

/**
 * Represents a neuron and methods it should have.
 *
 * @author Nanson Chen
 * @version December 11th, 2025
 */
public interface Neuron extends Serializable {
    /**
     * Changes one thing about the neuron (small mutation)
     */
    void changeOneThing();

    /**
     * Computes the activation of the neuron.
     *
     * @param bit should increase the chance that this neuron activates.
     */
    void computeActivation(boolean bit);

    /**
     * Soft check for the activation of the neuron.
     *
     * @return true if the neuron is activated, false otherwise.
     */
    boolean isActivated();

    /**
     * Clears the stake of the neuron.
     *
     * @deprecated
     */
    void clearStake();

    /**
     * Gets the stake of the neuron.
     *
     * @return the stake of the neuron.
     * @deprecated
     */
    int getStake();

    /**
     * Updates the stake of the neuron.
     *
     * @deprecated
     */
    void updateStake();

    /**
     * Gets the next neuron that should be evaluated.
     *
     * @return the next neuron to be evaluated.
     */
    Neuron getNextNeuron();

    /**
     * Gets the previous neuron layer associated with this neuron.
     *
     * @return the previous neuron layer.
     */
    Neuron[] getPreviousNeuronLayer();

    /**
     * Gets the incoming neurons that this neuron is using.
     *
     * @return the incoming neurons that this neuron is using.
     */
    Neuron[] getIncomingNeurons();

    /**
     * Check if the neuron is a data neuron.
     *
     * @return true if this instance is a data neuron, false otherwise.
     */
    boolean isDataNeuron();

    /**
     * Gets the index of the neuron layer where this neuron resides.
     *
     * @return the index of the neuron layer where this neuron resides.
     */
    int getNeuronLayerIndex();

    /**
     * Gets the index of the neuron in the neuron layer where it resides.
     *
     * @return the index of the neuron in the neuron layer where it resides.
     */
    int getNeuronIndex();
}