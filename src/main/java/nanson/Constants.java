package nanson;

public class Constants {
    public static final int DEFAULT_MATRIX_WIDTH = 7;
    public static final int DEFAULT_MATRIX_HEIGHT = 2;
    public static final int DEFAULT_NUM_INCOMING_NEURONS = 2;
    public static final int DEFAULT_LENGTH_OF_RESULTS = 7;
    public static final int DEFAULT_RUN_NEURONS_PER_CYCLE = DEFAULT_MATRIX_WIDTH * DEFAULT_MATRIX_HEIGHT
            * DEFAULT_NUM_INCOMING_NEURONS;

    // Delimiters for update string format: "row,col,activation;row,col,activation"
    public static final String CELL_DELIMITER = ";";
    public static final String FIELD_DELIMITER = ",";
}
