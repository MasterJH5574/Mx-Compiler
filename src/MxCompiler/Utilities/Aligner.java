package MxCompiler.Utilities;

public class Aligner {
    static public int align(int size, int base) {
        if (size % base == 0)
            return size;
        else
            return size + (base - (size % base));
    }
}