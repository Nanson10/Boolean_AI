package nanson;

/**
 * Helpful methods
 *
 * @author Nanson Chen
 * @version December 11th, 2025
 */
public class Utilities {
    public static char booleanArrayToChar(boolean[] arr) {
        if (arr.length == 0)
            throw new IllegalArgumentException("Boolean array cannot be empty");
        if (arr.length > 16)
            throw new IllegalArgumentException("Boolean array cannot exceed 16 bits");

        int value = 0;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i])
                value |= (1 << (arr.length - 1 - i));
        }
        return (char) value;
    }

    public static boolean[] charToBooleanArray(char c, int length) {
        if (length <= 0)
            throw new IllegalArgumentException("Length must be positive");
        if (length > 16)
            throw new IllegalArgumentException("Length cannot exceed 16 bits");

        boolean[] result = new boolean[length];
        for (int i = 0; i < length; i++) {
            result[i] = ((c >> (length - 1 - i)) & 1) == 1;
        }
        return result;
    }
}
