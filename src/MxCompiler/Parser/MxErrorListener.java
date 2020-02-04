package MxCompiler.Parser;

import MxCompiler.Utilities.ErrorHandler;
import MxCompiler.Utilities.Location;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

public class MxErrorListener extends BaseErrorListener {
    private ErrorHandler errorHandler;

    public MxErrorListener(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                            Object offendingSymbol,
                            int line,
                            int charPositionInLine,
                            String msg,
                            RecognitionException e) {
        errorHandler.error(new Location(line, charPositionInLine), msg);
        //super.syntaxError(recognizer, offendingSymbol, line, charPositionInLine, msg, e);
    }
}
