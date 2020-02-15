package MxCompiler.Utilities;

public class Aligner {
    static public int align(int size, int base) {
        if (base == 0) // Special case for "align(size, max)" when max == 0.
            return 0;
        if (size % base == 0)
            return size;
        else
            return size + (base - (size % base));
    }
}