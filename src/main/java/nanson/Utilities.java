package nanson;

/**
 * Helpful methods
 *
 * @author Nanson Chen
 * @version 2.0
 */
public class Utilities {
    /**
     * Constructs an instance of Utilities.
     */
    public Utilities() {
    }

    /**
     * Converts a boolean array representing bits to a char.
     * The first element of the array is treated as the most-significant bit.
     *
     * @param arr bit array (length 1..16)
     * @return character represented by the bits
     * @throws IllegalArgumentException if the array is empty or longer than 16
     */
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

    /**
     * Converts a char to a boolean array of the requested bit length.
     * The returned array has the most-significant bit first.
     *
     * @param c      character to convert
     * @param length bit length to return (1..16)
     * @return boolean array representing the character
     * @throws IllegalArgumentException if length is not in the valid range
     */
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
