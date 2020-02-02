// Generated from C:/Users/lairu/Documents/GitHub/Mx-Compiler/src/MxCompiler/Parser\Mx.g4 by ANTLR 4.7.2

package MxCompiler.Parser;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class MxLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		T__17=18, T__18=19, T__19=20, T__20=21, T__21=22, T__22=23, T__23=24, 
		T__24=25, T__25=26, T__26=27, T__27=28, T__28=29, T__29=30, T__30=31, 
		T__31=32, BoolLITERAL=33, IntegerLITERAL=34, StringLITERAL=35, INT=36, 
		BOOL=37, STRING=38, NULL=39, VOID=40, TRUE=41, FALSE=42, IF=43, ELSE=44, 
		FOR=45, WHILE=46, BREAK=47, CONTINUE=48, RETURN=49, NEW=50, CLASS=51, 
		THIS=52, IDENTIFIER=53, Whitespace=54, Newline=55, BlockComment=56, LineComment=57;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
			"T__9", "T__10", "T__11", "T__12", "T__13", "T__14", "T__15", "T__16", 
			"T__17", "T__18", "T__19", "T__20", "T__21", "T__22", "T__23", "T__24", 
			"T__25", "T__26", "T__27", "T__28", "T__29", "T__30", "T__31", "BoolLITERAL", 
			"IntegerLITERAL", "StringLITERAL", "ESC", "INT", "BOOL", "STRING", "NULL", 
			"VOID", "TRUE", "FALSE", "IF", "ELSE", "FOR", "WHILE", "BREAK", "CONTINUE", 
			"RETURN", "NEW", "CLASS", "THIS", "IDENTIFIER", "Whitespace", "Newline", 
			"BlockComment", "LineComment"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "';'", "'('", "')'", "'{'", "'}'", "'['", "']'", "','", "'='", 
			"'++'", "'--'", "'.'", "'+'", "'-'", "'!'", "'~'", "'*'", "'/'", "'%'", 
			"'<<'", "'>>'", "'<'", "'>'", "'<='", "'>='", "'=='", "'!='", "'&'", 
			"'^'", "'|'", "'&&'", "'||'", null, null, null, "'int'", "'bool'", "'string'", 
			"'null'", "'void'", "'true'", "'false'", "'if'", "'else'", "'for'", "'while'", 
			"'break'", "'continue'", "'return'", "'new'", "'class'", "'this'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, "BoolLITERAL", 
			"IntegerLITERAL", "StringLITERAL", "INT", "BOOL", "STRING", "NULL", "VOID", 
			"TRUE", "FALSE", "IF", "ELSE", "FOR", "WHILE", "BREAK", "CONTINUE", "RETURN", 
			"NEW", "CLASS", "THIS", "IDENTIFIER", "Whitespace", "Newline", "BlockComment", 
			"LineComment"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public MxLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Mx.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2;\u016d\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64\t"+
		"\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\4:\t:\4;\t;\3\2\3\2\3\3"+
		"\3\3\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3\13\3\13"+
		"\3\13\3\f\3\f\3\f\3\r\3\r\3\16\3\16\3\17\3\17\3\20\3\20\3\21\3\21\3\22"+
		"\3\22\3\23\3\23\3\24\3\24\3\25\3\25\3\25\3\26\3\26\3\26\3\27\3\27\3\30"+
		"\3\30\3\31\3\31\3\31\3\32\3\32\3\32\3\33\3\33\3\33\3\34\3\34\3\34\3\35"+
		"\3\35\3\36\3\36\3\37\3\37\3 \3 \3 \3!\3!\3!\3\"\3\"\5\"\u00c4\n\"\3#\3"+
		"#\3#\7#\u00c9\n#\f#\16#\u00cc\13#\5#\u00ce\n#\3$\3$\3$\7$\u00d3\n$\f$"+
		"\16$\u00d6\13$\3$\3$\3%\3%\3%\3%\3%\3%\5%\u00e0\n%\3&\3&\3&\3&\3\'\3\'"+
		"\3\'\3\'\3\'\3(\3(\3(\3(\3(\3(\3(\3)\3)\3)\3)\3)\3*\3*\3*\3*\3*\3+\3+"+
		"\3+\3+\3+\3,\3,\3,\3,\3,\3,\3-\3-\3-\3.\3.\3.\3.\3.\3/\3/\3/\3/\3\60\3"+
		"\60\3\60\3\60\3\60\3\60\3\61\3\61\3\61\3\61\3\61\3\61\3\62\3\62\3\62\3"+
		"\62\3\62\3\62\3\62\3\62\3\62\3\63\3\63\3\63\3\63\3\63\3\63\3\63\3\64\3"+
		"\64\3\64\3\64\3\65\3\65\3\65\3\65\3\65\3\65\3\66\3\66\3\66\3\66\3\66\3"+
		"\67\3\67\7\67\u0140\n\67\f\67\16\67\u0143\13\67\38\68\u0146\n8\r8\168"+
		"\u0147\38\38\39\39\59\u014e\n9\39\59\u0151\n9\39\39\3:\3:\3:\3:\7:\u0159"+
		"\n:\f:\16:\u015c\13:\3:\3:\3:\3:\3:\3;\3;\3;\3;\7;\u0167\n;\f;\16;\u016a"+
		"\13;\3;\3;\4\u00d4\u015a\2<\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25"+
		"\f\27\r\31\16\33\17\35\20\37\21!\22#\23%\24\'\25)\26+\27-\30/\31\61\32"+
		"\63\33\65\34\67\359\36;\37= ?!A\"C#E$G%I\2K&M\'O(Q)S*U+W,Y-[.]/_\60a\61"+
		"c\62e\63g\64i\65k\66m\67o8q9s:u;\3\2\b\3\2\63;\3\2\62;\4\2C\\c|\6\2\62"+
		";C\\aac|\5\2\13\f\17\17\"\"\4\2\f\f\17\17\2\u0178\2\3\3\2\2\2\2\5\3\2"+
		"\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21"+
		"\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2"+
		"\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3"+
		"\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3"+
		"\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3"+
		"\2\2\2\2A\3\2\2\2\2C\3\2\2\2\2E\3\2\2\2\2G\3\2\2\2\2K\3\2\2\2\2M\3\2\2"+
		"\2\2O\3\2\2\2\2Q\3\2\2\2\2S\3\2\2\2\2U\3\2\2\2\2W\3\2\2\2\2Y\3\2\2\2\2"+
		"[\3\2\2\2\2]\3\2\2\2\2_\3\2\2\2\2a\3\2\2\2\2c\3\2\2\2\2e\3\2\2\2\2g\3"+
		"\2\2\2\2i\3\2\2\2\2k\3\2\2\2\2m\3\2\2\2\2o\3\2\2\2\2q\3\2\2\2\2s\3\2\2"+
		"\2\2u\3\2\2\2\3w\3\2\2\2\5y\3\2\2\2\7{\3\2\2\2\t}\3\2\2\2\13\177\3\2\2"+
		"\2\r\u0081\3\2\2\2\17\u0083\3\2\2\2\21\u0085\3\2\2\2\23\u0087\3\2\2\2"+
		"\25\u0089\3\2\2\2\27\u008c\3\2\2\2\31\u008f\3\2\2\2\33\u0091\3\2\2\2\35"+
		"\u0093\3\2\2\2\37\u0095\3\2\2\2!\u0097\3\2\2\2#\u0099\3\2\2\2%\u009b\3"+
		"\2\2\2\'\u009d\3\2\2\2)\u009f\3\2\2\2+\u00a2\3\2\2\2-\u00a5\3\2\2\2/\u00a7"+
		"\3\2\2\2\61\u00a9\3\2\2\2\63\u00ac\3\2\2\2\65\u00af\3\2\2\2\67\u00b2\3"+
		"\2\2\29\u00b5\3\2\2\2;\u00b7\3\2\2\2=\u00b9\3\2\2\2?\u00bb\3\2\2\2A\u00be"+
		"\3\2\2\2C\u00c3\3\2\2\2E\u00cd\3\2\2\2G\u00cf\3\2\2\2I\u00df\3\2\2\2K"+
		"\u00e1\3\2\2\2M\u00e5\3\2\2\2O\u00ea\3\2\2\2Q\u00f1\3\2\2\2S\u00f6\3\2"+
		"\2\2U\u00fb\3\2\2\2W\u0100\3\2\2\2Y\u0106\3\2\2\2[\u0109\3\2\2\2]\u010e"+
		"\3\2\2\2_\u0112\3\2\2\2a\u0118\3\2\2\2c\u011e\3\2\2\2e\u0127\3\2\2\2g"+
		"\u012e\3\2\2\2i\u0132\3\2\2\2k\u0138\3\2\2\2m\u013d\3\2\2\2o\u0145\3\2"+
		"\2\2q\u0150\3\2\2\2s\u0154\3\2\2\2u\u0162\3\2\2\2wx\7=\2\2x\4\3\2\2\2"+
		"yz\7*\2\2z\6\3\2\2\2{|\7+\2\2|\b\3\2\2\2}~\7}\2\2~\n\3\2\2\2\177\u0080"+
		"\7\177\2\2\u0080\f\3\2\2\2\u0081\u0082\7]\2\2\u0082\16\3\2\2\2\u0083\u0084"+
		"\7_\2\2\u0084\20\3\2\2\2\u0085\u0086\7.\2\2\u0086\22\3\2\2\2\u0087\u0088"+
		"\7?\2\2\u0088\24\3\2\2\2\u0089\u008a\7-\2\2\u008a\u008b\7-\2\2\u008b\26"+
		"\3\2\2\2\u008c\u008d\7/\2\2\u008d\u008e\7/\2\2\u008e\30\3\2\2\2\u008f"+
		"\u0090\7\60\2\2\u0090\32\3\2\2\2\u0091\u0092\7-\2\2\u0092\34\3\2\2\2\u0093"+
		"\u0094\7/\2\2\u0094\36\3\2\2\2\u0095\u0096\7#\2\2\u0096 \3\2\2\2\u0097"+
		"\u0098\7\u0080\2\2\u0098\"\3\2\2\2\u0099\u009a\7,\2\2\u009a$\3\2\2\2\u009b"+
		"\u009c\7\61\2\2\u009c&\3\2\2\2\u009d\u009e\7\'\2\2\u009e(\3\2\2\2\u009f"+
		"\u00a0\7>\2\2\u00a0\u00a1\7>\2\2\u00a1*\3\2\2\2\u00a2\u00a3\7@\2\2\u00a3"+
		"\u00a4\7@\2\2\u00a4,\3\2\2\2\u00a5\u00a6\7>\2\2\u00a6.\3\2\2\2\u00a7\u00a8"+
		"\7@\2\2\u00a8\60\3\2\2\2\u00a9\u00aa\7>\2\2\u00aa\u00ab\7?\2\2\u00ab\62"+
		"\3\2\2\2\u00ac\u00ad\7@\2\2\u00ad\u00ae\7?\2\2\u00ae\64\3\2\2\2\u00af"+
		"\u00b0\7?\2\2\u00b0\u00b1\7?\2\2\u00b1\66\3\2\2\2\u00b2\u00b3\7#\2\2\u00b3"+
		"\u00b4\7?\2\2\u00b48\3\2\2\2\u00b5\u00b6\7(\2\2\u00b6:\3\2\2\2\u00b7\u00b8"+
		"\7`\2\2\u00b8<\3\2\2\2\u00b9\u00ba\7~\2\2\u00ba>\3\2\2\2\u00bb\u00bc\7"+
		"(\2\2\u00bc\u00bd\7(\2\2\u00bd@\3\2\2\2\u00be\u00bf\7~\2\2\u00bf\u00c0"+
		"\7~\2\2\u00c0B\3\2\2\2\u00c1\u00c4\5U+\2\u00c2\u00c4\5W,\2\u00c3\u00c1"+
		"\3\2\2\2\u00c3\u00c2\3\2\2\2\u00c4D\3\2\2\2\u00c5\u00ce\7\62\2\2\u00c6"+
		"\u00ca\t\2\2\2\u00c7\u00c9\t\3\2\2\u00c8\u00c7\3\2\2\2\u00c9\u00cc\3\2"+
		"\2\2\u00ca\u00c8\3\2\2\2\u00ca\u00cb\3\2\2\2\u00cb\u00ce\3\2\2\2\u00cc"+
		"\u00ca\3\2\2\2\u00cd\u00c5\3\2\2\2\u00cd\u00c6\3\2\2\2\u00ceF\3\2\2\2"+
		"\u00cf\u00d4\7$\2\2\u00d0\u00d3\5I%\2\u00d1\u00d3\13\2\2\2\u00d2\u00d0"+
		"\3\2\2\2\u00d2\u00d1\3\2\2\2\u00d3\u00d6\3\2\2\2\u00d4\u00d5\3\2\2\2\u00d4"+
		"\u00d2\3\2\2\2\u00d5\u00d7\3\2\2\2\u00d6\u00d4\3\2\2\2\u00d7\u00d8\7$"+
		"\2\2\u00d8H\3\2\2\2\u00d9\u00da\7^\2\2\u00da\u00e0\7$\2\2\u00db\u00dc"+
		"\7^\2\2\u00dc\u00e0\7p\2\2\u00dd\u00de\7^\2\2\u00de\u00e0\7^\2\2\u00df"+
		"\u00d9\3\2\2\2\u00df\u00db\3\2\2\2\u00df\u00dd\3\2\2\2\u00e0J\3\2\2\2"+
		"\u00e1\u00e2\7k\2\2\u00e2\u00e3\7p\2\2\u00e3\u00e4\7v\2\2\u00e4L\3\2\2"+
		"\2\u00e5\u00e6\7d\2\2\u00e6\u00e7\7q\2\2\u00e7\u00e8\7q\2\2\u00e8\u00e9"+
		"\7n\2\2\u00e9N\3\2\2\2\u00ea\u00eb\7u\2\2\u00eb\u00ec\7v\2\2\u00ec\u00ed"+
		"\7t\2\2\u00ed\u00ee\7k\2\2\u00ee\u00ef\7p\2\2\u00ef\u00f0\7i\2\2\u00f0"+
		"P\3\2\2\2\u00f1\u00f2\7p\2\2\u00f2\u00f3\7w\2\2\u00f3\u00f4\7n\2\2\u00f4"+
		"\u00f5\7n\2\2\u00f5R\3\2\2\2\u00f6\u00f7\7x\2\2\u00f7\u00f8\7q\2\2\u00f8"+
		"\u00f9\7k\2\2\u00f9\u00fa\7f\2\2\u00faT\3\2\2\2\u00fb\u00fc\7v\2\2\u00fc"+
		"\u00fd\7t\2\2\u00fd\u00fe\7w\2\2\u00fe\u00ff\7g\2\2\u00ffV\3\2\2\2\u0100"+
		"\u0101\7h\2\2\u0101\u0102\7c\2\2\u0102\u0103\7n\2\2\u0103\u0104\7u\2\2"+
		"\u0104\u0105\7g\2\2\u0105X\3\2\2\2\u0106\u0107\7k\2\2\u0107\u0108\7h\2"+
		"\2\u0108Z\3\2\2\2\u0109\u010a\7g\2\2\u010a\u010b\7n\2\2\u010b\u010c\7"+
		"u\2\2\u010c\u010d\7g\2\2\u010d\\\3\2\2\2\u010e\u010f\7h\2\2\u010f\u0110"+
		"\7q\2\2\u0110\u0111\7t\2\2\u0111^\3\2\2\2\u0112\u0113\7y\2\2\u0113\u0114"+
		"\7j\2\2\u0114\u0115\7k\2\2\u0115\u0116\7n\2\2\u0116\u0117\7g\2\2\u0117"+
		"`\3\2\2\2\u0118\u0119\7d\2\2\u0119\u011a\7t\2\2\u011a\u011b\7g\2\2\u011b"+
		"\u011c\7c\2\2\u011c\u011d\7m\2\2\u011db\3\2\2\2\u011e\u011f\7e\2\2\u011f"+
		"\u0120\7q\2\2\u0120\u0121\7p\2\2\u0121\u0122\7v\2\2\u0122\u0123\7k\2\2"+
		"\u0123\u0124\7p\2\2\u0124\u0125\7w\2\2\u0125\u0126\7g\2\2\u0126d\3\2\2"+
		"\2\u0127\u0128\7t\2\2\u0128\u0129\7g\2\2\u0129\u012a\7v\2\2\u012a\u012b"+
		"\7w\2\2\u012b\u012c\7t\2\2\u012c\u012d\7p\2\2\u012df\3\2\2\2\u012e\u012f"+
		"\7p\2\2\u012f\u0130\7g\2\2\u0130\u0131\7y\2\2\u0131h\3\2\2\2\u0132\u0133"+
		"\7e\2\2\u0133\u0134\7n\2\2\u0134\u0135\7c\2\2\u0135\u0136\7u\2\2\u0136"+
		"\u0137\7u\2\2\u0137j\3\2\2\2\u0138\u0139\7v\2\2\u0139\u013a\7j\2\2\u013a"+
		"\u013b\7k\2\2\u013b\u013c\7u\2\2\u013cl\3\2\2\2\u013d\u0141\t\4\2\2\u013e"+
		"\u0140\t\5\2\2\u013f\u013e\3\2\2\2\u0140\u0143\3\2\2\2\u0141\u013f\3\2"+
		"\2\2\u0141\u0142\3\2\2\2\u0142n\3\2\2\2\u0143\u0141\3\2\2\2\u0144\u0146"+
		"\t\6\2\2\u0145\u0144\3\2\2\2\u0146\u0147\3\2\2\2\u0147\u0145\3\2\2\2\u0147"+
		"\u0148\3\2\2\2\u0148\u0149\3\2\2\2\u0149\u014a\b8\2\2\u014ap\3\2\2\2\u014b"+
		"\u014d\7\17\2\2\u014c\u014e\7\f\2\2\u014d\u014c\3\2\2\2\u014d\u014e\3"+
		"\2\2\2\u014e\u0151\3\2\2\2\u014f\u0151\7\f\2\2\u0150\u014b\3\2\2\2\u0150"+
		"\u014f\3\2\2\2\u0151\u0152\3\2\2\2\u0152\u0153\b9\2\2\u0153r\3\2\2\2\u0154"+
		"\u0155\7\61\2\2\u0155\u0156\7,\2\2\u0156\u015a\3\2\2\2\u0157\u0159\13"+
		"\2\2\2\u0158\u0157\3\2\2\2\u0159\u015c\3\2\2\2\u015a\u015b\3\2\2\2\u015a"+
		"\u0158\3\2\2\2\u015b\u015d\3\2\2\2\u015c\u015a\3\2\2\2\u015d\u015e\7,"+
		"\2\2\u015e\u015f\7\61\2\2\u015f\u0160\3\2\2\2\u0160\u0161\b:\2\2\u0161"+
		"t\3\2\2\2\u0162\u0163\7\61\2\2\u0163\u0164\7\61\2\2\u0164\u0168\3\2\2"+
		"\2\u0165\u0167\n\7\2\2\u0166\u0165\3\2\2\2\u0167\u016a\3\2\2\2\u0168\u0166"+
		"\3\2\2\2\u0168\u0169\3\2\2\2\u0169\u016b\3\2\2\2\u016a\u0168\3\2\2\2\u016b"+
		"\u016c\b;\2\2\u016cv\3\2\2\2\17\2\u00c3\u00ca\u00cd\u00d2\u00d4\u00df"+
		"\u0141\u0147\u014d\u0150\u015a\u0168\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}