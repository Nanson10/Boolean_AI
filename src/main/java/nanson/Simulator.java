package nanson;

import java.util.ArrayList;
import java.util.Collections;

public class Simulator {
    protected final Neuron[][] NEURONS;
    protected final int NUM_INCOMING_NEURONS;
    protected int runNeuronsPerCycle;
    protected BooleanMatrixDisplay display;
    protected int currentIteration;
    protected Neuron curNeuron;
    private double activationThresholdMultiplier;
    private double activationPercentage;
    private int highlightCount;
    private final StringBuilder changedCells;

    public Simulator() {
        this(Constants.DEFAULT_MATRIX_WIDTH, Constants.DEFAULT_MATRIX_HEIGHT, Constants.DEFAULT_RUN_NEURONS_PER_CYCLE,
            Constants.DEFAULT_NUM_INCOMING_NEURONS);
    }

    public Simulator(Neuron[][] neurons, int runNeuronsPerCycle, int numIncomingNeurons) {
        NEURONS = neurons;
        this.runNeuronsPerCycle = runNeuronsPerCycle;
        NUM_INCOMING_NEURONS = numIncomingNeurons;
        activationThresholdMultiplier = 0.5;
        activationPercentage = 0.0;
        changedCells = new StringBuilder();
    }

    public Simulator(int width, int height, int runNeuronsPerCycle, int numIncomingNeurons) {
        NEURONS = new Neuron[height][width];
        initializeNeurons();

        activationThresholdMultiplier = 0.5;
        activationPercentage = 0.0;
        this.runNeuronsPerCycle = runNeuronsPerCycle;
        NUM_INCOMING_NEURONS = numIncomingNeurons;
        changedCells = new StringBuilder();

        initializeNeuronConnections();
    }

    private void initializeNeurons() {
        for (int i = 0; i < NEURONS.length; i++) {
            for (int j = 0; j < NEURONS[0].length; j++) {
                if (i == 1 && j < 7 && this instanceof AutoGrader) {
                    NEURONS[i][j] = new DataNeuron(this, i, j);
                } else
                    NEURONS[i][j] = new ActivationNeuron(this, i, j);
            }
        }
    }

    private void initializeNeuronConnections() {
        for (Neuron[] neuronRow : NEURONS) {
            for (Neuron neuron : neuronRow) {
                setRandomIncomingNeuronsAndWeights(neuron);
            }
        }
    }

    public boolean[][] getCurrentMatrixState() {
        boolean[][] matrixState = new boolean[NEURONS.length][NEURONS[0].length];
        for (int i = 0; i < NEURONS.length; i++) {
            for (int j = 0; j < NEURONS[0].length; j++) {
                matrixState[i][j] = NEURONS[i][j].isActivated();
            }
        }
        return matrixState;
    }

    public Neuron getNeuronAt(int row, int col) {
        if (row >= 0 && row < NEURONS.length && col >= 0 && col < NEURONS[0].length) {
            return NEURONS[row][col];
        }
        return null;
    }

    public double getActivationPercentage() {
        return activationPercentage;
    }

    public int getCurrentIteration() {
        return currentIteration;
    }

    public int getTotalIterations() {
        return (int) Math.pow(NEURONS.length * NEURONS[0].length, 3);
    }

    public int getHighlightCount() {
        return highlightCount;
    }

    public void setDisplay(BooleanMatrixDisplay display) {
        this.display = display;
    }

    void recordCellChange(int row, int col, boolean activated) {
        if (changedCells.length() > 0) {
            changedCells.append(Constants.CELL_DELIMITER);
        }
        changedCells.append(row)
            .append(Constants.FIELD_DELIMITER)
            .append(col)
            .append(Constants.FIELD_DELIMITER)
            .append(activated ? "1" : "0");
    }

    private void clearChanges() {
        changedCells.setLength(0);
    }

    private String getChangesString() {
        return changedCells.toString();
    }

    public void setRandomIncomingNeuronsAndWeights(Neuron neuron) {
        Neuron[] incomingNeurons = new Neuron[NUM_INCOMING_NEURONS];
        boolean[] weights = new boolean[NUM_INCOMING_NEURONS];
        for (int i = 0; i < NUM_INCOMING_NEURONS; i++) {
            int randRow = (int) (Math.random() * NEURONS.length);
            int randCol = (int) (Math.random() * NEURONS[0].length);
            incomingNeurons[i] = NEURONS[randRow][randCol];
            weights[i] = Math.random() < 0.5;
        }
        neuron.setIncomingNeuronsAndWeights(incomingNeurons, weights);
    }

    private void updateDisplay() {
        if (display != null) {
            display.update(getChangesString());
            clearChanges();
        }
    }

    public boolean[] runCycle(int lengthOfResults) {
        initializeCycle();
        executeNeuronComputations(lengthOfResults);
        highlightResults(lengthOfResults);
        return collectResults(lengthOfResults);
    }

    private void initializeCycle() {
        highlightCount = 0;
        currentIteration = 0;
        updateDisplay();
    }

    protected void executeNeuronComputations(int lengthOfResults) {
        for (currentIteration = 0; currentIteration < getTotalIterations(); currentIteration++) {
            if (curNeuron == null)
                curNeuron = NEURONS[0][0];
            if ((int) (Math.random() * 10000) == 0)
                curNeuron.changeOneThing();
            curNeuron.computeActivation(false);
        }
    }

    private void highlightResults(int lengthOfResults) {
        highlightCount = lengthOfResults;
        updateDisplay();
    }

    private boolean[] collectResults(int lengthOfResults) {
        boolean[] results = new boolean[lengthOfResults];
        int index = 0;
        for (int i = 0; i < NEURONS.length && index < lengthOfResults; i++) {
            for (int j = 0; j < NEURONS[0].length && index < lengthOfResults; j++) {
                results[index++] = NEURONS[i][j].isActivated();
            }
        }
        return results;
    }

    public double getActivationThresholdMultiplier() {
        updateActivationThresholdMultiplier();
        return activationThresholdMultiplier;
    }

    public void updateActivationThresholdMultiplier() {
        calculateActivationPercentage();
        adjustThresholdMultiplier();
    }

    private void calculateActivationPercentage() {
        activationPercentage = 0;
        for (Neuron[] neuronRow : NEURONS) {
            for (Neuron neuron : neuronRow) {
                if (neuron.isActivated()) {
                    activationPercentage += 1.0;
                }
            }
        }
        activationPercentage /= (NEURONS.length * NEURONS[0].length);
    }

    private void adjustThresholdMultiplier() {
        activationThresholdMultiplier += logarithmicSlowingFactor(activationPercentage - 0.5, 1, 10)
            * (activationPercentage - 0.5) * 0.00001;
        activationThresholdMultiplier += (0.5 - activationThresholdMultiplier) * 0.0000005;
        // activationThresholdMultiplier += powerSlowingFactor(activationPercentage -
        // 0.5, 5,
        // 1.5) * (activationPercentage - 0.5);
        // activationThresholdMultiplier += (activationPercentage - 0.5) * 0.00001;
    }

    public double powerSlowingFactor(double x, double domain, double power) {
        double normalizedX = Math.abs(x) / domain;
        return Math.min(1, Math.pow(normalizedX, power));
    }

    public double logarithmicSlowingFactor(double x, int domain, double base) {
        double normalizedX = Math.abs(x) / domain + 1;
        return Math.log(normalizedX) / Math.log(base);
    }

    public void stimulate(boolean goodIfTrue) {
        ArrayList<Neuron> sortedNeurons = getSortedNeuronsByStake();

        if (goodIfTrue) {
            punishLowStakeNeurons(sortedNeurons);
        } else {
            punishHighStakeNeurons(sortedNeurons);
        }
    }

    private ArrayList<Neuron> getSortedNeuronsByStake() {
        ArrayList<Neuron> neuronList = new ArrayList<>();
        for (Neuron[] neuronRow : NEURONS) {
            Collections.addAll(neuronList, neuronRow);
        }
        neuronList.sort(Neuron::compareTo);
        return neuronList;
    }

    private void punishLowStakeNeurons(ArrayList<Neuron> sortedNeurons) {
        int lowestStake = sortedNeurons.get(0).getStake();
        for (Neuron neuron : sortedNeurons) {
            if (neuron.getStake() == lowestStake) {
                neuron.changeOneThing();
            } else {
                break;
            }
        }
    }

    private void punishHighStakeNeurons(ArrayList<Neuron> sortedNeurons) {
        int highestStake = sortedNeurons.get(sortedNeurons.size() - 1).getStake();
        for (int i = sortedNeurons.size() - 1; i >= 0; i--) {
            if (sortedNeurons.get(i).getStake() == highestStake) {
                sortedNeurons.get(i).changeOneThing();
            } else {
                break;
            }
        }
        sortedNeurons.forEach(Neuron::clearStake);
    }
}