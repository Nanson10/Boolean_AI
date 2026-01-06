package nanson;

import java.io.Serializable;

/**
 * Stores the layers and neurons for a simulation.
 *
 * @author Nanson Chen
 * @version 2.0
 */
public class NeuronDatabase implements Serializable {
    /**
     * 2D array storing neurons by layer: neurons[layerIndex][neuronIndex].
     */
    private final Neuron[][] neurons;

    /**
     * Constructs a neuron database.
     *
     * @param incomingConnections are the number of connections each neuron should
     *                            have.
     * @param layerLength         is are the lengths of the neuron layers with the
     *                            number of arguments being the number of layers.
     */
    public NeuronDatabase(int incomingConnections, int... layerLength) {
        neurons = new Neuron[layerLength.length][];
        for (int row = 0; row < layerLength.length; row++)
            neurons[row] = new Neuron[layerLength[row]];
        for (int row = 0; row < neurons.length; row++) {
            for (int col = 0; col < neurons[row].length; col++)
                if (row == 0)
                    neurons[row][col] = new DataNeuron(row, col);
                else {
                    neurons[row][col] = new ActivationNeuron(this, row, col, incomingConnections);
                    for (int iter = 0; iter < neurons[row - 1].length + incomingConnections; iter++)
                        neurons[row][col].changeOneThing();
                }
        }
    }

    /**
     * Gets the neuron matrix.
     *
     * @return the neuron matrix.
     */
    public Neuron[][] getNeurons() {
        return neurons;
    }

    /**
     * Gets the number of neuron layers.
     *
     * @return the number of neuron layers.
     */
    public int getNumberOfLayers() {
        return neurons.length;
    }

    /**
     * Gets a neuron layer.
     *
     * @param neuronLayerIndex is the index of the layer.
     * @return the neuron layer at that index.
     */
    public Neuron[] getNeuronLayer(int neuronLayerIndex) {
        return neurons[neuronLayerIndex];
    }

    /**
     * Gets the neuron at that specific index.
     *
     * @param neuronLayerIndex is the index of the layer.
     * @param neuronIndex      is the index of the neuron in that layer.
     * @return the neuron specified.
     */
    public Neuron getNeuron(int neuronLayerIndex, int neuronIndex) {
        return neurons[neuronLayerIndex][neuronIndex];
    }

    public Neuron[] getOutputNeurons() {
        return neurons[getNumberOfLayers() - 1];
    }
}
