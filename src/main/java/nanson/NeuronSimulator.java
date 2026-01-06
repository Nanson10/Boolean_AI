package nanson;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.function.Supplier;

public class NeuronSimulator {
    private final NeuronDatabase neuronDatabase;
    private final NeuronSimulatorConstants con;

    public NeuronSimulator(@NotNull NeuronDatabase neuronDatabase, @NotNull NeuronSimulatorConstants con) {
        this.neuronDatabase = neuronDatabase;
        this.con = con;
    }

    public Boolean[] runSimulation() {
        Boolean[] input = con.data().get();
        for (int i = 0; i < input.length && i < neuronDatabase.getNeuronLayer(0).length; i++)
            ((DataNeuron) (neuronDatabase.getNeuronLayer(0)[i])).setActivated(input[i]);
        int repeat = 0;
        ArrayList<Boolean> lastResult = new ArrayList<>();
        while (repeat < con.repeatThreshold) {
            if (lastResult.isEmpty()) {
                for (Neuron outputNeuron : neuronDatabase.getOutputNeurons())
                    lastResult.add(outputNeuron.computeActivation(false));
                continue;
            }
            boolean isARepeatedResult = true;
            for (int i = 0; i < lastResult.size(); i++) {
                boolean neuronResult = neuronDatabase.getOutputNeurons()[i].computeActivation(false);
                if (neuronResult != lastResult.get(i))
                    isARepeatedResult = false;
                lastResult.set(i, neuronResult);
            }
            if (isARepeatedResult)
                repeat++;
            else
                repeat = 0;
        }
        return lastResult.toArray(new Boolean[0]);
    }

    public record NeuronSimulatorConstants(int repeatThreshold, Supplier<Boolean[]> data) {
    }
}
