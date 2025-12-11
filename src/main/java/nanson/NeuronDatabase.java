package nanson;

import java.io.Serializable;

public class NeuronDatabase implements Serializable {
    private Neuron[][] neurons;
    public NeuronDatabase(int incomingConnections, int... layerSize) {
        neurons = new Neuron[layerSize.length][];
        for (int a = 0; a < layerSize.length; a++)
            neurons[a] = new Neuron[layerSize[a]];
        for (int a = 0; a < neurons.length; a++) {
            for (int b = 0; b < neurons[a].length; b++)
                if (a == 0)
                    neurons[a][b] = new DataNeuron(a, b);
                else
                    neurons[a][b] = new ActivationNeuron(this, a, b, incomingConnections);
        }
    }

    public Neuron[][] getNeurons() {
        return neurons;
    }

    public Neuron[] getNeuronLayer(int neuronLayerIndex) {
        return neurons[neuronLayerIndex];
    }

    public Neuron getNeuron(int neuronLayerIndex, int neuronIndex) {
        return neurons[neuronLayerIndex][neuronIndex];
    }
}
