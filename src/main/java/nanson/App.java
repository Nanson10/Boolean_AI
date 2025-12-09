package nanson;

/**
 * Demo application for BooleanMatrixDisplay
 */
public class App {
    public static void main(String[] args) {
        // Example boolean matrix
        boolean[][] matrix = {
            {true, false, true, false},
            {false, true, false, true},
            {true, true, false, false},
            {false, false, true, true}
        };
        
        // Display the matrix
        BooleanMatrixDisplay.displayMatrix(matrix);
    }
}
