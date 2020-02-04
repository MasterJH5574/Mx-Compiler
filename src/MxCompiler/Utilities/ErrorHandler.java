package MxCompiler.Utilities;

import java.io.PrintStream;

public class ErrorHandler {
    private PrintStream printStream;
    private int errorCnt;
    private int warningCnt;

    public ErrorHandler() {
        printStream = System.err;
        errorCnt = 0;
        warningCnt = 0;
    }

    public void error(String msg) {
        errorCnt++;
        printStream.println("Error: " + msg);
    }

    public void error(Location location, String msg) {
        error(location.toString() + " " + msg);
    }

    public void warning(String msg) {
        warningCnt++;
        printStream.println("Warning: " + msg);
    }

    public void warning(Location location, String msg) {
        warning(location.toString() + " " + msg);
    }

    public boolean hasError() {
        return errorCnt > 0;
    }

    public void print() {
        printStream.println(errorCnt + " error(s), " +
                warningCnt + " warning(s) in total.");
    }
}
