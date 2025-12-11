package nanson;

public class AutoGrader extends Simulator {
    private String furthestProgress = "";
    private String currentProgress = "";

    private int index = 0;
    private int timeSinceMeetingHighScore = 0;
    private int lastAnswerDistance = Integer.MAX_VALUE; // Distance from target in previous attempt
    private char lastAnswer;

    public AutoGrader() {
        super();
    }

    public AutoGrader(int width, int height, int runNeuronsPerCycle, int numIncomingNeurons) {
        super(width, height, runNeuronsPerCycle, numIncomingNeurons);
    }

    public AutoGrader(Neuron[][] neurons, int numIncomingNeurons) {
        super(neurons, neurons.length * neurons[0].length * numIncomingNeurons, numIncomingNeurons);
    }

    @Override
    protected void executeNeuronComputations(int lengthOfResults) {
        // Get the target character and convert it to bits
        char target = (char) ('A' + index);
        boolean[] targetBits = Utilities.charToBooleanArray(target, lengthOfResults);
        currentIteration = 0;

        while (currentIteration++ < getTotalIterations()) {
            for (int totalNeurons = 0; totalNeurons < NEURONS.length
                * NEURONS[0].length; totalNeurons++, currentIteration++) {
                if (curNeuron == null) {
                    curNeuron = NEURONS[0][0];
                }

                boolean bit =
                    curNeuron.getNeuronLayerIndex() == 1 && curNeuron.getNeuronIndex() < targetBits.length && targetBits[curNeuron.getNeuronIndex()];
                curNeuron.computeActivation(bit);

                Neuron nextNeuron = curNeuron.getNextNeuron();
                if (nextNeuron == null) {
                    curNeuron = NEURONS[curNeuron.getNeuronLayerIndex() - 1][0];
                } else {
                    curNeuron = nextNeuron;
                }
            }
            // After each iteration of n, punish incorrect output neurons
            punishIncorrectOutputNeurons(lengthOfResults, targetBits);
        }
    }

    /**
     * Punishes output neurons that don't match the target pattern.
     * For each incorrect neuron, applies punishByDepth with depth 5.
     */
    private void punishIncorrectOutputNeurons(int lengthOfResults, boolean[] targetBits) {
        int index = 0;
        for (int j = 0; j < NEURONS[0].length && index < lengthOfResults; j++) {
            boolean currentActivation = NEURONS[0][j].isActivated();
            boolean expectedActivation = targetBits[index];

            // If this output neuron is incorrect, punish it by depth
            if (currentActivation != expectedActivation) {
                punishByDepth(NEURONS[0][j], 5, 0);
            }

            index++;
        }
    }

    @Override
    public boolean[] runCycle(int lengthOfResults) {
        boolean[] results = super.runCycle(lengthOfResults);
        char answer = Utilities.booleanArrayToChar(results);
        lastAnswer = answer;
        char target = (char) ('A' + index);

        if (isCorrectAnswer(answer)) {
            handleCorrectAnswer(answer);
            lastAnswerDistance = 0; // Reset distance on correct answer
        } else {
            handleIncorrectAnswer(answer, target);
        }

        return results;
    }

    private boolean isCorrectAnswer(char answer) {
        return answer == (char) ('A' + index);
    }

    private void handleCorrectAnswer(char answer) {
        stimulate(true);
        updateProgress(answer);
        advanceToNextLetter();
        optimizeRunCycles();
    }

    private void updateProgress(char answer) {
        currentProgress += answer;
        if (currentProgress.length() >= furthestProgress.length())
            timeSinceMeetingHighScore = 0;
        if (currentProgress.length() > furthestProgress.length())
            furthestProgress = currentProgress;
    }

    private void advanceToNextLetter() {
        index++;
        if ('A' + index > 'Z') {
            index = 0;
        }
    }

    private void optimizeRunCycles() {
        runNeuronsPerCycle = Math.max(NEURONS.length * NEURONS[0].length, runNeuronsPerCycle - 1);
    }

    private void handleIncorrectAnswer(char answer, char target) {
        int currentDistance = calculateDistance(answer, target);

        // Reward if getting closer, punish if getting farther or staying same
        if (currentDistance < lastAnswerDistance) {
            stimulate(true); // Reward for improvement
        } else if (currentDistance > lastAnswerDistance) {
            stimulate(false); // Punish for getting worse
        }
        lastAnswerDistance = currentDistance;
        resetProgress();
        timeSinceMeetingHighScore++;

        if (shouldExpandNetwork()) {
            expandNetwork();
        }
    }

    /**
     * Calculate the distance between the answer and target character.
     * Uses Hamming distance - the number of bit flips needed to transform
     * the answer character into the target character.
     */
    private int calculateDistance(char answer, char target) {
        // XOR the two characters to find differing bits
        int xor = answer ^ target;

        // Count the number of 1s in the XOR result (number of differing bits)
        int distance = 0;
        while (xor != 0) {
            distance += xor & 1; // Add 1 if the least significant bit is 1
            xor >>>= 1; // Unsigned right shift to check next bit
        }

        return distance;
    }

    public int getLastAnswerDistance() {
        return lastAnswerDistance;
    }

    public int getCurrentDistance() {
        return calculateDistance(lastAnswer, getGoal());
    }

    public char getGoal() {
        return (char) ('A' + index);
    }

    public int getChecksUntilResize() {
        return Math.max(0, 100000 - timeSinceMeetingHighScore);
    }

    private void resetProgress() {
        currentProgress = "";
        index = 0;
    }

    private boolean shouldExpandNetwork() {
        return timeSinceMeetingHighScore >= 100000;
    }

    private void expandNetwork() {
        int[] newDimensions = calculateNewDimensions();
        AutoGrader newGrader = createExpandedGrader(newDimensions[0], newDimensions[1]);
        transferStateToNewGrader(newGrader);
        reinitializeDisplay(newGrader);
    }

    private int[] calculateNewDimensions() {
        int currentWidth = NEURONS[0].length;
        int currentHeight = NEURONS.length;
        int newWidth;
        int newHeight;

        // First expand vertically to make it square
        if (currentHeight < currentWidth) {
            newWidth = currentWidth;
            newHeight = currentHeight + 1;
        } else {
            // Once square, expand in both directions
            newWidth = currentWidth + 1;
            newHeight = currentHeight + 1;
        }

        return new int[]{newWidth, newHeight};
    }

    private AutoGrader createExpandedGrader(int width, int height) {
        int runNeuronsPerCycle = width * height * Constants.DEFAULT_NUM_INCOMING_NEURONS;
        return new AutoGrader(width, height,
            runNeuronsPerCycle,
            Constants.DEFAULT_NUM_INCOMING_NEURONS);
    }

    private void transferStateToNewGrader(AutoGrader newGrader) {
        newGrader.furthestProgress = this.furthestProgress;
        newGrader.currentProgress = "";
        newGrader.index = index;
        newGrader.timeSinceMeetingHighScore = 0;
        newGrader.lastAnswerDistance = Integer.MAX_VALUE; // Reset distance for new network
    }

    private void reinitializeDisplay(AutoGrader newGrader) {
        if (display != null) {
            display.reinitialize(newGrader);
        }
    }

    public String getFurthestProgress() {
        return "\"" + furthestProgress.length() + "\"";
    }

    public String getCurrentProgress() {
        return "\"" + currentProgress.length() + "\"";
    }

    /**
     * Punishes a neuron and its incoming neurons recursively up to a specified
     * depth.
     * The probability of punishment decreases exponentially with depth to account
     * for
     * the branching factor of the network.
     * <p>
     * At each depth level, the probability of calling changeOneThing() is:
     * 0.001 / (NUM_INCOMING_NEURONS ^ curDepth)
     * <p>
     * This ensures that as we traverse deeper into the network and encounter more
     * neurons due to branching, the overall expected number of mutations remains
     * reasonable. Without this adjustment, the exponential growth in the number of
     * neurons at each level would lead to excessive mutations.
     *
     * @param neuron   The neuron to punish (and start traversing from)
     * @param maxDepth The maximum depth to traverse (typically 5)
     * @param curDepth The current depth in the recursion (starts at 0)
     */
    private void punishByDepth(Neuron neuron, int maxDepth, int curDepth) {
        if (neuron == null || curDepth > maxDepth) {
            return;
        }

        // 1 in 1000 chance to change this neuron
        if (Math.random() < (0.001 / Math.pow(NUM_INCOMING_NEURONS, curDepth))) {
            neuron.changeOneThing();
        }

        // Recursively punish incoming neurons
        Neuron[] incomingNeurons = neuron.getPreviousNeuronLayer();
        if (incomingNeurons != null) {
            for (Neuron incoming : incomingNeurons) {
                punishByDepth(incoming, maxDepth, curDepth + 1);
            }
        }
    }
}
