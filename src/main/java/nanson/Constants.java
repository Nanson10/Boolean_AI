package nanson;

/**
 * Defined constants for the program
 *
 * @author Nanson Chen
 * @version 2.0
 */
public class Constants {
    /**
     * Default display width (number of columns).
     */
    public static final int DEFAULT_MATRIX_WIDTH = 7;
    /**
     * Default display height (number of rows).
     */
    public static final int DEFAULT_MATRIX_HEIGHT = 4;
    /**
     * Default number of incoming neurons per activation neuron.
     */
    public static final int DEFAULT_NUM_INCOMING_NEURONS = 2;
    /**
     * Default number of result bits to collect from the network.
     */
    public static final int DEFAULT_LENGTH_OF_RESULTS = 7;
    /**
     * Default number of neurons to run per cycle.
     */
    public static final int DEFAULT_RUN_NEURONS_PER_CYCLE = 1;
    /**
     * Delimiter between cell updates in a changes string.
     */
    public static final String CELL_DELIMITER = ";";

    // Delimiters for update string format: "row,col,activation;row,col,activation"
    /**
     * Delimiter between fields inside a single cell update.
     */
    public static final String FIELD_DELIMITER = ",";

    /**
     * Constructs an instance of Constants
     */
    public Constants() {
    }
}
