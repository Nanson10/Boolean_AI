package nanson;

public class AutoGrader extends Simulator {
    private String furthestProgress = "";
    private String currentProgress = "";
    public static void main(String[] args) {
        new BooleanMatrixDisplay(new AutoGrader());
    }

    private int index = 0;

    @Override
    public boolean[] runCycle(int lengthOfResults) {
        boolean[] results = super.runCycle(lengthOfResults);
        char answer = Utilities.booleanArrayToChar(results);  // Validate conversion
        if (answer != ('A' + index)) {
            stimulate(false);
            currentProgress = "";
        }
        else {
            stimulate(true);
            currentProgress += answer;
            if (currentProgress.length() > furthestProgress.length()) {
                furthestProgress = currentProgress;
            }
            index++;
            if (index + 'A' > 'Z')
                index = 0;
        }
        return results;
    }

    public String getFurthestProgress() {
        return "\"" + furthestProgress + "\"";
    }

    public String getCurrentProgress() {
        return "\"" + currentProgress + "\"";
    }
}
