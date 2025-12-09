package nanson;

public class AutoGrader extends Simulator {
    private String furthestProgress = "";
    private String currentProgress = "";

    int index = 0;

    @Override
    public boolean[] runCycle(int lengthOfResults) {
        boolean[] results = super.runCycle(lengthOfResults);
        char answer = Utilities.booleanArrayToChar(results);  // Validate conversion
        if (answer == (char)('A' + index)) {
            stimulate(true);
            currentProgress += answer;
            if (currentProgress.length() > furthestProgress.length()) {
                furthestProgress = currentProgress;
            }
            index++;
            if ('A' + index > 'Z') {
                index = 0;
            }
        }
        else {
            stimulate(false);
            currentProgress = "";
            index = 0;
        }
        return results;
    }

    public String getFurthestProgress() {
        return "\"" + furthestProgress.length() + "\"";
    }

    public String getCurrentProgress() {
        return "\"" + currentProgress.length() + "\"";
    }
}
