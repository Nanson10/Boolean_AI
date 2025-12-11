package nanson;

import java.io.Serializable;

/**
 * Stores the layers and neurons for a simulation.
 *
 * @author Nanson Chen
 * @version December 11th, 2025
 */
public class NeuronDatabase implements Serializable {
    private final Neuron[][] neurons;

    /**
     * Constructs a neuron database.
     *
     * @param incomingConnections are the number of connections each neuron should have.
     * @param layerLength         is are the lengths of the neuron layers with the number of arguments being the number of layers.
     */
    public NeuronDatabase(int incomingConnections, int... layerLength) {
        neurons = new Neuron[layerLength.length][];
        for (int a = 0; a < layerLength.length; a++)
            neurons[a] = new Neuron[layerLength[a]];
        for (int a = 0; a < neurons.length; a++) {
            for (int b = 0; b < neurons[a].length; b++)
                if (a == 0)
                    neurons[a][b] = new DataNeuron(a, b);
                else
                    neurons[a][b] = new ActivationNeuron(this, a, b, incomingConnections);
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
}
