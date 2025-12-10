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

        for (int n = 0; n < NEURONS.length * NEURONS.length; n++) {
            for (int i = 0; i < NEURONS.length; i++) {
                for (int j = 0; j < NEURONS[0].length; j++) {
                    if ((int) (Math.random() * 10000) == 0)
                        NEURONS[i][j].changeOneThing();

                    // For second row (i == 1), pass the target bit to influence activation
                    boolean bit = (i == 1 && j < targetBits.length) ? targetBits[j] : false;
                    NEURONS[i][j].computeActivation(bit);
                    currentIteration++;
                }
            }
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
        } else {
            stimulate(false); // Punish for no improvement or getting worse
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

        return new int[] { newWidth, newHeight };
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
}
