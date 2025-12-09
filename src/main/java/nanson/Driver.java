package nanson;

import java.util.Scanner;

public class Driver {
    public static void main(String[] args) {
        System.out.println("Select mode:");
        System.out.println("1: Simulator");
        System.out.println("2: AutoGrader");
        Scanner scanner = new Scanner(System.in);
        int choice;
        do {
            System.out.print("Select mode (1-2): ");
            while (!scanner.hasNextInt()) {
                System.out.print("Select mode (1-2): ");
                scanner.next();
            }
            choice = scanner.nextInt();
        } while (choice < 1 || choice > 2);
        scanner.close();
        if (choice == 1) {
            new BooleanMatrixDisplay(new Simulator());
        } else if (choice == 2) {
            new BooleanMatrixDisplay(new AutoGrader());
        }
    }
}