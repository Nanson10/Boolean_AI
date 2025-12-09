package nanson;

/**
 * Demo application for BooleanMatrixDisplay
 */
public class App {
    public static void main(String[] args) {
        new Simulator(10, 10, 1000, 5).runCycle(5);
    }
}
