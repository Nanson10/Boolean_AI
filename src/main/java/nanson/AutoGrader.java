package nanson;

public class AutoGrader extends Simulator {
    private String furthestProgress = "";
    private String currentProgress = "";

    private int index = 0;
    private int timeSinceBeatingHighScore = 0;

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
    public boolean[] runCycle(int lengthOfResults) {
        boolean[] results = super.runCycle(lengthOfResults);
        char answer = Utilities.booleanArrayToChar(results);

        if (isCorrectAnswer(answer)) {
            handleCorrectAnswer(answer);
        } else {
            handleIncorrectAnswer();
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
        if (currentProgress.length() > furthestProgress.length()) {
            furthestProgress = currentProgress;
            timeSinceBeatingHighScore = 0;
        }
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

    private void handleIncorrectAnswer() {
        stimulate(false);
        resetProgress();
        timeSinceBeatingHighScore++;

        if (shouldExpandNetwork()) {
            expandNetwork();
        }
    }

    private void resetProgress() {
        currentProgress = "";
        index = 0;
    }

    private boolean shouldExpandNetwork() {
        return timeSinceBeatingHighScore >= 100;
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
        return new AutoGrader(width, height,
                width * height * Constants.DEFAULT_NUM_INCOMING_NEURONS,
                Constants.DEFAULT_NUM_INCOMING_NEURONS);
    }

    private void transferStateToNewGrader(AutoGrader newGrader) {
        newGrader.furthestProgress = this.furthestProgress;
        newGrader.currentProgress = "";
        newGrader.index = 0;
        newGrader.timeSinceBeatingHighScore = 0;
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
