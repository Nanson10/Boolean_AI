package nanson;

/**
 * Defined constants for the program
 *
 * @author Nanson Chen
 * @version December 11th, 2025
 */
public class Constants {
    public static final int DEFAULT_MATRIX_WIDTH = 7;
    public static final int DEFAULT_MATRIX_HEIGHT = 4;
    public static final int DEFAULT_NUM_INCOMING_NEURONS = 2;
    public static final int DEFAULT_LENGTH_OF_RESULTS = 7;
    public static final int DEFAULT_RUN_NEURONS_PER_CYCLE = 1;

    // Delimiters for update string format: "row,col,activation;row,col,activation"
    public static final String CELL_DELIMITER = ";";
    public static final String FIELD_DELIMITER = ",";
}
