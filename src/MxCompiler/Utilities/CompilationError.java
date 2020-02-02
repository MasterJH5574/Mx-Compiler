package MxCompiler.Utilities;

public class CompilationError extends Exception {
    private Location location;

    public CompilationError() {
    }

    public CompilationError(String message, Location location) {
        super(message);
        this.location = location;
    }

    @Override
    public String getMessage() {
        return "Compilation Error: " + location.toString() + " " + super.getMessage();
    }
}
