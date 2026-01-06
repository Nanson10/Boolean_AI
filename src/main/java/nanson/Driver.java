package nanson;

import java.util.Arrays;

public class Driver {
    static void main(String[] args) {
        NeuronDatabase database = new NeuronDatabase(2, 8, 4);
        NeuronSimulator sim = new NeuronSimulator(database, new NeuronSimulator.NeuronSimulatorConstants(
            5,
            () -> Utilities.charToBooleanArray('a', 8)
        ));
        Boolean[] result = sim.runSimulation();
        System.out.println(Arrays.asList(result));
    }
}