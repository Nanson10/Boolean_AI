package nanson;

public class Utilities {
    public static char booleanArrayToChar(boolean[] arr) {
        if (arr.length == 0) throw new IllegalArgumentException("Boolean array cannot be empty");
        if (arr.length > 16) throw new IllegalArgumentException("Boolean array cannot exceed 16 bits");
        
        int value = 0;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i]) value |= (1 << (arr.length - 1 - i));
        }
        return (char) value;
    }
}
