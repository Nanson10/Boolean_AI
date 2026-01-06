package nanson;

import java.io.Serializable;

/**
 * Represents a neuron and methods it should have.
 *
 * @author Nanson Chen
 * @version 2.0
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
     * @return whether the neuron is activated or not.
     */
    boolean computeActivation(boolean bit);

    /**
     * Clears the stake of the neuron.
     *
     * @deprecated opt for punishByDepth if an output neuron has the wrong answer.
     */
    @Deprecated(since = "2.0", forRemoval = true)
    void clearStake();

    /**
     * Gets the stake of the neuron.
     *
     * @return the stake of the neuron.
     * @deprecated opt for punishByDepth if an output neuron has the wrong answer.
     */
    @Deprecated(since = "2.0", forRemoval = true)
    int getStake();

    /**
     * Updates the stake of the neuron.
     *
     * @deprecated opt for punishByDepth if an output neuron has the wrong answer.
     */
    @Deprecated(since = "2.0", forRemoval = true)
    void updateStake();

    /**
     * Gets the next neuron that should be evaluated.
     *
     * @return the next neuron to be evaluated.
     */
    Neuron getNextNeuron();

    /**
     * Gets the neurons that this neuron can pull input neurons from.
     *
     * @return the previous neuron layer.
     */
    Neuron[] getPotentialInputNeurons();

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

    /**
     * Punishes the neuron by depth with a chance in mutating one of its parameters
     * and its incoming neurons.
     *
     * @param denominatorOfProbability is the denominator of the probability of
     *                                 mutation.
     */
    void punishByDepth(int denominatorOfProbability);
}