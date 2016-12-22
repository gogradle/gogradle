// Generated from GolangImport.g4 by ANTLR 4.6
package com.github.blindpirate.gogradle.antlr;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class GolangImportLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.6", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, IDENTIFIER=7, STRING_LIT=8, 
		LITTLE_U_VALUE=9, BIG_U_VALUE=10, ANYOTHER=11, WS=12, COMMENT=13, LINE_COMMENT=14;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "IDENTIFIER", "STRING_LIT", 
		"RAW_STRING_LIT", "INTERPRETED_STRING_LIT", "STRING_CHAR", "RAW_STRING_CHAR", 
		"LITTLE_U_VALUE", "BIG_U_VALUE", "BYTE_VALUE", "OCTAL_BYTE_VALUE", "HEX_BYTE_VALUE", 
		"ESCAPED_CHAR", "LETTER", "DECIMAL_DIGIT", "OCTAL_DIGIT", "HEX_DIGIT", 
		"NEWLINE", "UNICODE_CHAR", "UNICODE_DIGIT", "UNICODE_LETTER", "ANYOTHER", 
		"WS", "COMMENT", "LINE_COMMENT"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "';'", "'package'", "'import'", "'('", "')'", "'.'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, "IDENTIFIER", "STRING_LIT", 
		"LITTLE_U_VALUE", "BIG_U_VALUE", "ANYOTHER", "WS", "COMMENT", "LINE_COMMENT"
	};
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


	public GolangImportLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "GolangImport.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\20\u00df\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\3\2"+
		"\3\2\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\5\3"+
		"\5\3\6\3\6\3\7\3\7\3\b\3\b\3\b\7\bZ\n\b\f\b\16\b]\13\b\3\t\3\t\5\ta\n"+
		"\t\3\n\3\n\3\n\7\nf\n\n\f\n\16\ni\13\n\3\n\3\n\3\13\3\13\3\13\7\13p\n"+
		"\13\f\13\16\13s\13\13\3\13\3\13\3\f\3\f\3\f\3\f\5\f{\n\f\3\r\3\r\3\r\3"+
		"\r\5\r\u0081\n\r\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\17\3\17\3\17"+
		"\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\20\3\20\5\20\u0099\n\20"+
		"\3\21\3\21\3\21\3\21\3\21\3\22\3\22\3\22\3\22\3\22\3\23\3\23\3\23\3\24"+
		"\3\24\5\24\u00aa\n\24\3\25\3\25\3\26\3\26\3\27\3\27\3\30\3\30\3\31\3\31"+
		"\3\32\5\32\u00b7\n\32\3\33\5\33\u00ba\n\33\3\34\3\34\3\34\3\34\3\35\6"+
		"\35\u00c1\n\35\r\35\16\35\u00c2\3\35\3\35\3\36\3\36\3\36\3\36\7\36\u00cb"+
		"\n\36\f\36\16\36\u00ce\13\36\3\36\3\36\3\36\3\36\3\36\3\37\3\37\3\37\3"+
		"\37\7\37\u00d9\n\37\f\37\16\37\u00dc\13\37\3\37\3\37\3\u00cc\2 \3\3\5"+
		"\4\7\5\t\6\13\7\r\b\17\t\21\n\23\2\25\2\27\2\31\2\33\13\35\f\37\2!\2#"+
		"\2%\2\'\2)\2+\2-\2/\2\61\2\63\2\65\2\67\r9\16;\17=\20\3\2\r\4\2$$^^\3"+
		"\2bb\13\2$$))^^cdhhppttvvxx\3\2\62;\3\2\629\5\2\62;CHch\3\2\f\f\26\2\62"+
		";\u0662\u066b\u06f2\u06fb\u0968\u0971\u09e8\u09f1\u0a68\u0a71\u0ae8\u0af1"+
		"\u0b68\u0b71\u0be9\u0bf1\u0c68\u0c71\u0ce8\u0cf1\u0d68\u0d71\u0e52\u0e5b"+
		"\u0ed2\u0edb\u0f22\u0f2b\u1042\u104b\u136b\u1373\u17e2\u17eb\u1812\u181b"+
		"\uff12\uff1b\u0104\2C\\c|\u00ac\u00ac\u00b7\u00b7\u00bc\u00bc\u00c2\u00d8"+
		"\u00da\u00f8\u00fa\u0221\u0224\u0235\u0252\u02af\u02b2\u02ba\u02bd\u02c3"+
		"\u02d2\u02d3\u02e2\u02e6\u02f0\u02f0\u037c\u037c\u0388\u0388\u038a\u038c"+
		"\u038e\u038e\u0390\u03a3\u03a5\u03d0\u03d2\u03d9\u03dc\u03f5\u0402\u0483"+
		"\u048e\u04c6\u04c9\u04ca\u04cd\u04ce\u04d2\u04f7\u04fa\u04fb\u0533\u0558"+
		"\u055b\u055b\u0563\u0589\u05d2\u05ec\u05f2\u05f4\u0623\u063c\u0642\u064c"+
		"\u0673\u06d5\u06d7\u06d7\u06e7\u06e8\u06fc\u06fe\u0712\u0712\u0714\u072e"+
		"\u0782\u07a7\u0907\u093b\u093f\u093f\u0952\u0952\u095a\u0963\u0987\u098e"+
		"\u0991\u0992\u0995\u09aa\u09ac\u09b2\u09b4\u09b4\u09b8\u09bb\u09de\u09df"+
		"\u09e1\u09e3\u09f2\u09f3\u0a07\u0a0c\u0a11\u0a12\u0a15\u0a2a\u0a2c\u0a32"+
		"\u0a34\u0a35\u0a37\u0a38\u0a3a\u0a3b\u0a5b\u0a5e\u0a60\u0a60\u0a74\u0a76"+
		"\u0a87\u0a8d\u0a8f\u0a8f\u0a91\u0a93\u0a95\u0aaa\u0aac\u0ab2\u0ab4\u0ab5"+
		"\u0ab7\u0abb\u0abf\u0abf\u0ad2\u0ad2\u0ae2\u0ae2\u0b07\u0b0e\u0b11\u0b12"+
		"\u0b15\u0b2a\u0b2c\u0b32\u0b34\u0b35\u0b38\u0b3b\u0b3f\u0b3f\u0b5e\u0b5f"+
		"\u0b61\u0b63\u0b87\u0b8c\u0b90\u0b92\u0b94\u0b97\u0b9b\u0b9c\u0b9e\u0b9e"+
		"\u0ba0\u0ba1\u0ba5\u0ba6\u0baa\u0bac\u0bb0\u0bb7\u0bb9\u0bbb\u0c07\u0c0e"+
		"\u0c10\u0c12\u0c14\u0c2a\u0c2c\u0c35\u0c37\u0c3b\u0c62\u0c63\u0c87\u0c8e"+
		"\u0c90\u0c92\u0c94\u0caa\u0cac\u0cb5\u0cb7\u0cbb\u0ce0\u0ce0\u0ce2\u0ce3"+
		"\u0d07\u0d0e\u0d10\u0d12\u0d14\u0d2a\u0d2c\u0d3b\u0d62\u0d63\u0d87\u0d98"+
		"\u0d9c\u0db3\u0db5\u0dbd\u0dbf\u0dbf\u0dc2\u0dc8\u0e03\u0e32\u0e34\u0e35"+
		"\u0e42\u0e48\u0e83\u0e84\u0e86\u0e86\u0e89\u0e8a\u0e8c\u0e8c\u0e8f\u0e8f"+
		"\u0e96\u0e99\u0e9b\u0ea1\u0ea3\u0ea5\u0ea7\u0ea7\u0ea9\u0ea9\u0eac\u0ead"+
		"\u0eaf\u0eb2\u0eb4\u0eb5\u0ebf\u0ec6\u0ec8\u0ec8\u0ede\u0edf\u0f02\u0f02"+
		"\u0f42\u0f6c\u0f8a\u0f8d\u1002\u1023\u1025\u1029\u102b\u102c\u1052\u1057"+
		"\u10a2\u10c7\u10d2\u10f8\u1102\u115b\u1161\u11a4\u11aa\u11fb\u1202\u1208"+
		"\u120a\u1248\u124a\u124a\u124c\u124f\u1252\u1258\u125a\u125a\u125c\u125f"+
		"\u1262\u1288\u128a\u128a\u128c\u128f\u1292\u12b0\u12b2\u12b2\u12b4\u12b7"+
		"\u12ba\u12c0\u12c2\u12c2\u12c4\u12c7\u12ca\u12d0\u12d2\u12d8\u12da\u12f0"+
		"\u12f2\u1310\u1312\u1312\u1314\u1317\u131a\u1320\u1322\u1348\u134a\u135c"+
		"\u13a2\u13f6\u1403\u1678\u1683\u169c\u16a2\u16ec\u1782\u17b5\u1822\u1879"+
		"\u1882\u18aa\u1e02\u1e9d\u1ea2\u1efb\u1f02\u1f17\u1f1a\u1f1f\u1f22\u1f47"+
		"\u1f4a\u1f4f\u1f52\u1f59\u1f5b\u1f5b\u1f5d\u1f5d\u1f5f\u1f5f\u1f61\u1f7f"+
		"\u1f82\u1fb6\u1fb8\u1fbe\u1fc0\u1fc0\u1fc4\u1fc6\u1fc8\u1fce\u1fd2\u1fd5"+
		"\u1fd8\u1fdd\u1fe2\u1fee\u1ff4\u1ff6\u1ff8\u1ffe\u2081\u2081\u2104\u2104"+
		"\u2109\u2109\u210c\u2115\u2117\u2117\u211b\u211f\u2126\u2126\u2128\u2128"+
		"\u212a\u212a\u212c\u212f\u2131\u2133\u2135\u213b\u2162\u2185\u3007\u3009"+
		"\u3023\u302b\u3033\u3037\u303a\u303c\u3043\u3096\u309f\u30a0\u30a3\u30fc"+
		"\u30fe\u3100\u3107\u312e\u3133\u3190\u31a2\u31b9\u3402\u3402\u4db7\u4db7"+
		"\u4e02\u4e02\u9fa7\u9fa7\ua002\ua48e\uac02\uac02\ud7a5\ud7a5\uf902\ufa2f"+
		"\ufb02\ufb08\ufb15\ufb19\ufb1f\ufb1f\ufb21\ufb2a\ufb2c\ufb38\ufb3a\ufb3e"+
		"\ufb40\ufb40\ufb42\ufb43\ufb45\ufb46\ufb48\ufbb3\ufbd5\ufd3f\ufd52\ufd91"+
		"\ufd94\ufdc9\ufdf2\ufdfd\ufe72\ufe74\ufe76\ufe76\ufe78\ufefe\uff23\uff3c"+
		"\uff43\uff5c\uff68\uffc0\uffc4\uffc9\uffcc\uffd1\uffd4\uffd9\uffdc\uffde"+
		"\5\2\13\f\16\17\"\"\4\2\f\f\17\17\u00e0\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3"+
		"\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2"+
		"\2\33\3\2\2\2\2\35\3\2\2\2\2\67\3\2\2\2\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2"+
		"\2\3?\3\2\2\2\5A\3\2\2\2\7I\3\2\2\2\tP\3\2\2\2\13R\3\2\2\2\rT\3\2\2\2"+
		"\17V\3\2\2\2\21`\3\2\2\2\23b\3\2\2\2\25l\3\2\2\2\27z\3\2\2\2\31\u0080"+
		"\3\2\2\2\33\u0082\3\2\2\2\35\u008a\3\2\2\2\37\u0098\3\2\2\2!\u009a\3\2"+
		"\2\2#\u009f\3\2\2\2%\u00a4\3\2\2\2\'\u00a9\3\2\2\2)\u00ab\3\2\2\2+\u00ad"+
		"\3\2\2\2-\u00af\3\2\2\2/\u00b1\3\2\2\2\61\u00b3\3\2\2\2\63\u00b6\3\2\2"+
		"\2\65\u00b9\3\2\2\2\67\u00bb\3\2\2\29\u00c0\3\2\2\2;\u00c6\3\2\2\2=\u00d4"+
		"\3\2\2\2?@\7=\2\2@\4\3\2\2\2AB\7r\2\2BC\7c\2\2CD\7e\2\2DE\7m\2\2EF\7c"+
		"\2\2FG\7i\2\2GH\7g\2\2H\6\3\2\2\2IJ\7k\2\2JK\7o\2\2KL\7r\2\2LM\7q\2\2"+
		"MN\7t\2\2NO\7v\2\2O\b\3\2\2\2PQ\7*\2\2Q\n\3\2\2\2RS\7+\2\2S\f\3\2\2\2"+
		"TU\7\60\2\2U\16\3\2\2\2V[\5\'\24\2WZ\5\'\24\2XZ\5\63\32\2YW\3\2\2\2YX"+
		"\3\2\2\2Z]\3\2\2\2[Y\3\2\2\2[\\\3\2\2\2\\\20\3\2\2\2][\3\2\2\2^a\5\23"+
		"\n\2_a\5\25\13\2`^\3\2\2\2`_\3\2\2\2a\22\3\2\2\2bg\7b\2\2cf\5\31\r\2d"+
		"f\5/\30\2ec\3\2\2\2ed\3\2\2\2fi\3\2\2\2ge\3\2\2\2gh\3\2\2\2hj\3\2\2\2"+
		"ig\3\2\2\2jk\7b\2\2k\24\3\2\2\2lq\7$\2\2mp\5\27\f\2np\5\37\20\2om\3\2"+
		"\2\2on\3\2\2\2ps\3\2\2\2qo\3\2\2\2qr\3\2\2\2rt\3\2\2\2sq\3\2\2\2tu\7$"+
		"\2\2u\26\3\2\2\2v{\n\2\2\2w{\5\33\16\2x{\5\35\17\2y{\5%\23\2zv\3\2\2\2"+
		"zw\3\2\2\2zx\3\2\2\2zy\3\2\2\2{\30\3\2\2\2|\u0081\n\3\2\2}\u0081\5\33"+
		"\16\2~\u0081\5\35\17\2\177\u0081\5%\23\2\u0080|\3\2\2\2\u0080}\3\2\2\2"+
		"\u0080~\3\2\2\2\u0080\177\3\2\2\2\u0081\32\3\2\2\2\u0082\u0083\7^\2\2"+
		"\u0083\u0084\7w\2\2\u0084\u0085\3\2\2\2\u0085\u0086\5-\27\2\u0086\u0087"+
		"\5-\27\2\u0087\u0088\5-\27\2\u0088\u0089\5-\27\2\u0089\34\3\2\2\2\u008a"+
		"\u008b\7^\2\2\u008b\u008c\7W\2\2\u008c\u008d\3\2\2\2\u008d\u008e\5-\27"+
		"\2\u008e\u008f\5-\27\2\u008f\u0090\5-\27\2\u0090\u0091\5-\27\2\u0091\u0092"+
		"\5-\27\2\u0092\u0093\5-\27\2\u0093\u0094\5-\27\2\u0094\u0095\5-\27\2\u0095"+
		"\36\3\2\2\2\u0096\u0099\5!\21\2\u0097\u0099\5#\22\2\u0098\u0096\3\2\2"+
		"\2\u0098\u0097\3\2\2\2\u0099 \3\2\2\2\u009a\u009b\7^\2\2\u009b\u009c\5"+
		"+\26\2\u009c\u009d\5+\26\2\u009d\u009e\5+\26\2\u009e\"\3\2\2\2\u009f\u00a0"+
		"\7^\2\2\u00a0\u00a1\7z\2\2\u00a1\u00a2\5-\27\2\u00a2\u00a3\5-\27\2\u00a3"+
		"$\3\2\2\2\u00a4\u00a5\7^\2\2\u00a5\u00a6\t\4\2\2\u00a6&\3\2\2\2\u00a7"+
		"\u00aa\5\65\33\2\u00a8\u00aa\7a\2\2\u00a9\u00a7\3\2\2\2\u00a9\u00a8\3"+
		"\2\2\2\u00aa(\3\2\2\2\u00ab\u00ac\t\5\2\2\u00ac*\3\2\2\2\u00ad\u00ae\t"+
		"\6\2\2\u00ae,\3\2\2\2\u00af\u00b0\t\7\2\2\u00b0.\3\2\2\2\u00b1\u00b2\t"+
		"\b\2\2\u00b2\60\3\2\2\2\u00b3\u00b4\n\b\2\2\u00b4\62\3\2\2\2\u00b5\u00b7"+
		"\t\t\2\2\u00b6\u00b5\3\2\2\2\u00b7\64\3\2\2\2\u00b8\u00ba\t\n\2\2\u00b9"+
		"\u00b8\3\2\2\2\u00ba\66\3\2\2\2\u00bb\u00bc\13\2\2\2\u00bc\u00bd\3\2\2"+
		"\2\u00bd\u00be\b\34\2\2\u00be8\3\2\2\2\u00bf\u00c1\t\13\2\2\u00c0\u00bf"+
		"\3\2\2\2\u00c1\u00c2\3\2\2\2\u00c2\u00c0\3\2\2\2\u00c2\u00c3\3\2\2\2\u00c3"+
		"\u00c4\3\2\2\2\u00c4\u00c5\b\35\2\2\u00c5:\3\2\2\2\u00c6\u00c7\7\61\2"+
		"\2\u00c7\u00c8\7,\2\2\u00c8\u00cc\3\2\2\2\u00c9\u00cb\13\2\2\2\u00ca\u00c9"+
		"\3\2\2\2\u00cb\u00ce\3\2\2\2\u00cc\u00cd\3\2\2\2\u00cc\u00ca\3\2\2\2\u00cd"+
		"\u00cf\3\2\2\2\u00ce\u00cc\3\2\2\2\u00cf\u00d0\7,\2\2\u00d0\u00d1\7\61"+
		"\2\2\u00d1\u00d2\3\2\2\2\u00d2\u00d3\b\36\2\2\u00d3<\3\2\2\2\u00d4\u00d5"+
		"\7\61\2\2\u00d5\u00d6\7\61\2\2\u00d6\u00da\3\2\2\2\u00d7\u00d9\n\f\2\2"+
		"\u00d8\u00d7\3\2\2\2\u00d9\u00dc\3\2\2\2\u00da\u00d8\3\2\2\2\u00da\u00db"+
		"\3\2\2\2\u00db\u00dd\3\2\2\2\u00dc\u00da\3\2\2\2\u00dd\u00de\b\37\2\2"+
		"\u00de>\3\2\2\2\23\2Y[`egoqz\u0080\u0098\u00a9\u00b6\u00b9\u00c2\u00cc"+
		"\u00da\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}