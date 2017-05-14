/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

// Generated from GolangBuildInfo.g4 by ANTLR 4.6
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
public class GolangBuildInfoLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.6", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, IDENTIFIER=11, STRING_LIT=12, LITTLE_U_VALUE=13, BIG_U_VALUE=14, 
		NEWLINE=15, WS=16, COMMENT=17, ANYOTHER=18;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
		"T__9", "IDENTIFIER", "STRING_LIT", "RAW_STRING_LIT", "INTERPRETED_STRING_LIT", 
		"STRING_CHAR", "RAW_STRING_CHAR", "LITTLE_U_VALUE", "BIG_U_VALUE", "BYTE_VALUE", 
		"OCTAL_BYTE_VALUE", "HEX_BYTE_VALUE", "ESCAPED_CHAR", "LETTER", "DECIMAL_DIGIT", 
		"OCTAL_DIGIT", "HEX_DIGIT", "NEWLINE", "UNICODE_CHAR", "UNICODE_DIGIT", 
		"UNICODE_LETTER", "WS", "COMMENT", "ANYOTHER"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "';'", "'package'", "'import'", "'('", "')'", "'.'", "'//'", "'+build'", 
		"','", "'!'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, null, null, null, null, "IDENTIFIER", 
		"STRING_LIT", "LITTLE_U_VALUE", "BIG_U_VALUE", "NEWLINE", "WS", "COMMENT", 
		"ANYOTHER"
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


	public GolangBuildInfoLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "GolangBuildInfo.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\24\u00e7\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t"+
		" \4!\t!\4\"\t\"\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3"+
		"\4\3\4\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t"+
		"\3\t\3\t\3\n\3\n\3\13\3\13\3\f\3\f\7\fm\n\f\f\f\16\fp\13\f\3\r\3\r\5\r"+
		"t\n\r\3\16\3\16\3\16\7\16y\n\16\f\16\16\16|\13\16\3\16\3\16\3\17\3\17"+
		"\3\17\7\17\u0083\n\17\f\17\16\17\u0086\13\17\3\17\3\17\3\20\3\20\3\20"+
		"\3\20\5\20\u008e\n\20\3\21\3\21\3\21\3\21\5\21\u0094\n\21\3\22\3\22\3"+
		"\22\3\22\3\22\3\22\3\22\3\22\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3"+
		"\23\3\23\3\23\3\23\3\24\3\24\5\24\u00ac\n\24\3\25\3\25\3\25\3\25\3\25"+
		"\3\26\3\26\3\26\3\26\3\26\3\27\3\27\3\27\3\30\3\30\5\30\u00bd\n\30\3\31"+
		"\3\31\3\32\3\32\3\33\3\33\3\34\3\34\3\35\3\35\3\36\5\36\u00ca\n\36\3\37"+
		"\5\37\u00cd\n\37\3 \6 \u00d0\n \r \16 \u00d1\3 \3 \3!\3!\3!\3!\7!\u00da"+
		"\n!\f!\16!\u00dd\13!\3!\3!\3!\3!\3!\3\"\3\"\3\"\3\"\3\u00db\2#\3\3\5\4"+
		"\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\2\35\2\37\2!\2#\17"+
		"%\20\'\2)\2+\2-\2/\2\61\2\63\2\65\2\67\219\2;\2=\2?\22A\23C\24\3\2\f\4"+
		"\2$$^^\3\2bb\13\2$$))^^cdhhppttvvxx\3\2\62;\3\2\629\5\2\62;CHch\3\2\f"+
		"\f\26\2\62;\u0662\u066b\u06f2\u06fb\u0968\u0971\u09e8\u09f1\u0a68\u0a71"+
		"\u0ae8\u0af1\u0b68\u0b71\u0be9\u0bf1\u0c68\u0c71\u0ce8\u0cf1\u0d68\u0d71"+
		"\u0e52\u0e5b\u0ed2\u0edb\u0f22\u0f2b\u1042\u104b\u136b\u1373\u17e2\u17eb"+
		"\u1812\u181b\uff12\uff1b\u0104\2C\\c|\u00ac\u00ac\u00b7\u00b7\u00bc\u00bc"+
		"\u00c2\u00d8\u00da\u00f8\u00fa\u0221\u0224\u0235\u0252\u02af\u02b2\u02ba"+
		"\u02bd\u02c3\u02d2\u02d3\u02e2\u02e6\u02f0\u02f0\u037c\u037c\u0388\u0388"+
		"\u038a\u038c\u038e\u038e\u0390\u03a3\u03a5\u03d0\u03d2\u03d9\u03dc\u03f5"+
		"\u0402\u0483\u048e\u04c6\u04c9\u04ca\u04cd\u04ce\u04d2\u04f7\u04fa\u04fb"+
		"\u0533\u0558\u055b\u055b\u0563\u0589\u05d2\u05ec\u05f2\u05f4\u0623\u063c"+
		"\u0642\u064c\u0673\u06d5\u06d7\u06d7\u06e7\u06e8\u06fc\u06fe\u0712\u0712"+
		"\u0714\u072e\u0782\u07a7\u0907\u093b\u093f\u093f\u0952\u0952\u095a\u0963"+
		"\u0987\u098e\u0991\u0992\u0995\u09aa\u09ac\u09b2\u09b4\u09b4\u09b8\u09bb"+
		"\u09de\u09df\u09e1\u09e3\u09f2\u09f3\u0a07\u0a0c\u0a11\u0a12\u0a15\u0a2a"+
		"\u0a2c\u0a32\u0a34\u0a35\u0a37\u0a38\u0a3a\u0a3b\u0a5b\u0a5e\u0a60\u0a60"+
		"\u0a74\u0a76\u0a87\u0a8d\u0a8f\u0a8f\u0a91\u0a93\u0a95\u0aaa\u0aac\u0ab2"+
		"\u0ab4\u0ab5\u0ab7\u0abb\u0abf\u0abf\u0ad2\u0ad2\u0ae2\u0ae2\u0b07\u0b0e"+
		"\u0b11\u0b12\u0b15\u0b2a\u0b2c\u0b32\u0b34\u0b35\u0b38\u0b3b\u0b3f\u0b3f"+
		"\u0b5e\u0b5f\u0b61\u0b63\u0b87\u0b8c\u0b90\u0b92\u0b94\u0b97\u0b9b\u0b9c"+
		"\u0b9e\u0b9e\u0ba0\u0ba1\u0ba5\u0ba6\u0baa\u0bac\u0bb0\u0bb7\u0bb9\u0bbb"+
		"\u0c07\u0c0e\u0c10\u0c12\u0c14\u0c2a\u0c2c\u0c35\u0c37\u0c3b\u0c62\u0c63"+
		"\u0c87\u0c8e\u0c90\u0c92\u0c94\u0caa\u0cac\u0cb5\u0cb7\u0cbb\u0ce0\u0ce0"+
		"\u0ce2\u0ce3\u0d07\u0d0e\u0d10\u0d12\u0d14\u0d2a\u0d2c\u0d3b\u0d62\u0d63"+
		"\u0d87\u0d98\u0d9c\u0db3\u0db5\u0dbd\u0dbf\u0dbf\u0dc2\u0dc8\u0e03\u0e32"+
		"\u0e34\u0e35\u0e42\u0e48\u0e83\u0e84\u0e86\u0e86\u0e89\u0e8a\u0e8c\u0e8c"+
		"\u0e8f\u0e8f\u0e96\u0e99\u0e9b\u0ea1\u0ea3\u0ea5\u0ea7\u0ea7\u0ea9\u0ea9"+
		"\u0eac\u0ead\u0eaf\u0eb2\u0eb4\u0eb5\u0ebf\u0ec6\u0ec8\u0ec8\u0ede\u0edf"+
		"\u0f02\u0f02\u0f42\u0f6c\u0f8a\u0f8d\u1002\u1023\u1025\u1029\u102b\u102c"+
		"\u1052\u1057\u10a2\u10c7\u10d2\u10f8\u1102\u115b\u1161\u11a4\u11aa\u11fb"+
		"\u1202\u1208\u120a\u1248\u124a\u124a\u124c\u124f\u1252\u1258\u125a\u125a"+
		"\u125c\u125f\u1262\u1288\u128a\u128a\u128c\u128f\u1292\u12b0\u12b2\u12b2"+
		"\u12b4\u12b7\u12ba\u12c0\u12c2\u12c2\u12c4\u12c7\u12ca\u12d0\u12d2\u12d8"+
		"\u12da\u12f0\u12f2\u1310\u1312\u1312\u1314\u1317\u131a\u1320\u1322\u1348"+
		"\u134a\u135c\u13a2\u13f6\u1403\u1678\u1683\u169c\u16a2\u16ec\u1782\u17b5"+
		"\u1822\u1879\u1882\u18aa\u1e02\u1e9d\u1ea2\u1efb\u1f02\u1f17\u1f1a\u1f1f"+
		"\u1f22\u1f47\u1f4a\u1f4f\u1f52\u1f59\u1f5b\u1f5b\u1f5d\u1f5d\u1f5f\u1f5f"+
		"\u1f61\u1f7f\u1f82\u1fb6\u1fb8\u1fbe\u1fc0\u1fc0\u1fc4\u1fc6\u1fc8\u1fce"+
		"\u1fd2\u1fd5\u1fd8\u1fdd\u1fe2\u1fee\u1ff4\u1ff6\u1ff8\u1ffe\u2081\u2081"+
		"\u2104\u2104\u2109\u2109\u210c\u2115\u2117\u2117\u211b\u211f\u2126\u2126"+
		"\u2128\u2128\u212a\u212a\u212c\u212f\u2131\u2133\u2135\u213b\u2162\u2185"+
		"\u3007\u3009\u3023\u302b\u3033\u3037\u303a\u303c\u3043\u3096\u309f\u30a0"+
		"\u30a3\u30fc\u30fe\u3100\u3107\u312e\u3133\u3190\u31a2\u31b9\u3402\u3402"+
		"\u4db7\u4db7\u4e02\u4e02\u9fa7\u9fa7\ua002\ua48e\uac02\uac02\ud7a5\ud7a5"+
		"\uf902\ufa2f\ufb02\ufb08\ufb15\ufb19\ufb1f\ufb1f\ufb21\ufb2a\ufb2c\ufb38"+
		"\ufb3a\ufb3e\ufb40\ufb40\ufb42\ufb43\ufb45\ufb46\ufb48\ufbb3\ufbd5\ufd3f"+
		"\ufd52\ufd91\ufd94\ufdc9\ufdf2\ufdfd\ufe72\ufe74\ufe76\ufe76\ufe78\ufefe"+
		"\uff23\uff3c\uff43\uff5c\uff68\uffc0\uffc4\uffc9\uffcc\uffd1\uffd4\uffd9"+
		"\uffdc\uffde\5\2\13\13\16\17\"\"\u00e8\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2"+
		"\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2"+
		"\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2#\3\2\2\2\2%\3\2\2"+
		"\2\2\67\3\2\2\2\2?\3\2\2\2\2A\3\2\2\2\2C\3\2\2\2\3E\3\2\2\2\5G\3\2\2\2"+
		"\7O\3\2\2\2\tV\3\2\2\2\13X\3\2\2\2\rZ\3\2\2\2\17\\\3\2\2\2\21_\3\2\2\2"+
		"\23f\3\2\2\2\25h\3\2\2\2\27n\3\2\2\2\31s\3\2\2\2\33u\3\2\2\2\35\177\3"+
		"\2\2\2\37\u008d\3\2\2\2!\u0093\3\2\2\2#\u0095\3\2\2\2%\u009d\3\2\2\2\'"+
		"\u00ab\3\2\2\2)\u00ad\3\2\2\2+\u00b2\3\2\2\2-\u00b7\3\2\2\2/\u00bc\3\2"+
		"\2\2\61\u00be\3\2\2\2\63\u00c0\3\2\2\2\65\u00c2\3\2\2\2\67\u00c4\3\2\2"+
		"\29\u00c6\3\2\2\2;\u00c9\3\2\2\2=\u00cc\3\2\2\2?\u00cf\3\2\2\2A\u00d5"+
		"\3\2\2\2C\u00e3\3\2\2\2EF\7=\2\2F\4\3\2\2\2GH\7r\2\2HI\7c\2\2IJ\7e\2\2"+
		"JK\7m\2\2KL\7c\2\2LM\7i\2\2MN\7g\2\2N\6\3\2\2\2OP\7k\2\2PQ\7o\2\2QR\7"+
		"r\2\2RS\7q\2\2ST\7t\2\2TU\7v\2\2U\b\3\2\2\2VW\7*\2\2W\n\3\2\2\2XY\7+\2"+
		"\2Y\f\3\2\2\2Z[\7\60\2\2[\16\3\2\2\2\\]\7\61\2\2]^\7\61\2\2^\20\3\2\2"+
		"\2_`\7-\2\2`a\7d\2\2ab\7w\2\2bc\7k\2\2cd\7n\2\2de\7f\2\2e\22\3\2\2\2f"+
		"g\7.\2\2g\24\3\2\2\2hi\7#\2\2i\26\3\2\2\2jm\5/\30\2km\5;\36\2lj\3\2\2"+
		"\2lk\3\2\2\2mp\3\2\2\2nl\3\2\2\2no\3\2\2\2o\30\3\2\2\2pn\3\2\2\2qt\5\33"+
		"\16\2rt\5\35\17\2sq\3\2\2\2sr\3\2\2\2t\32\3\2\2\2uz\7b\2\2vy\5!\21\2w"+
		"y\5\67\34\2xv\3\2\2\2xw\3\2\2\2y|\3\2\2\2zx\3\2\2\2z{\3\2\2\2{}\3\2\2"+
		"\2|z\3\2\2\2}~\7b\2\2~\34\3\2\2\2\177\u0084\7$\2\2\u0080\u0083\5\37\20"+
		"\2\u0081\u0083\5\'\24\2\u0082\u0080\3\2\2\2\u0082\u0081\3\2\2\2\u0083"+
		"\u0086\3\2\2\2\u0084\u0082\3\2\2\2\u0084\u0085\3\2\2\2\u0085\u0087\3\2"+
		"\2\2\u0086\u0084\3\2\2\2\u0087\u0088\7$\2\2\u0088\36\3\2\2\2\u0089\u008e"+
		"\n\2\2\2\u008a\u008e\5#\22\2\u008b\u008e\5%\23\2\u008c\u008e\5-\27\2\u008d"+
		"\u0089\3\2\2\2\u008d\u008a\3\2\2\2\u008d\u008b\3\2\2\2\u008d\u008c\3\2"+
		"\2\2\u008e \3\2\2\2\u008f\u0094\n\3\2\2\u0090\u0094\5#\22\2\u0091\u0094"+
		"\5%\23\2\u0092\u0094\5-\27\2\u0093\u008f\3\2\2\2\u0093\u0090\3\2\2\2\u0093"+
		"\u0091\3\2\2\2\u0093\u0092\3\2\2\2\u0094\"\3\2\2\2\u0095\u0096\7^\2\2"+
		"\u0096\u0097\7w\2\2\u0097\u0098\3\2\2\2\u0098\u0099\5\65\33\2\u0099\u009a"+
		"\5\65\33\2\u009a\u009b\5\65\33\2\u009b\u009c\5\65\33\2\u009c$\3\2\2\2"+
		"\u009d\u009e\7^\2\2\u009e\u009f\7W\2\2\u009f\u00a0\3\2\2\2\u00a0\u00a1"+
		"\5\65\33\2\u00a1\u00a2\5\65\33\2\u00a2\u00a3\5\65\33\2\u00a3\u00a4\5\65"+
		"\33\2\u00a4\u00a5\5\65\33\2\u00a5\u00a6\5\65\33\2\u00a6\u00a7\5\65\33"+
		"\2\u00a7\u00a8\5\65\33\2\u00a8&\3\2\2\2\u00a9\u00ac\5)\25\2\u00aa\u00ac"+
		"\5+\26\2\u00ab\u00a9\3\2\2\2\u00ab\u00aa\3\2\2\2\u00ac(\3\2\2\2\u00ad"+
		"\u00ae\7^\2\2\u00ae\u00af\5\63\32\2\u00af\u00b0\5\63\32\2\u00b0\u00b1"+
		"\5\63\32\2\u00b1*\3\2\2\2\u00b2\u00b3\7^\2\2\u00b3\u00b4\7z\2\2\u00b4"+
		"\u00b5\5\65\33\2\u00b5\u00b6\5\65\33\2\u00b6,\3\2\2\2\u00b7\u00b8\7^\2"+
		"\2\u00b8\u00b9\t\4\2\2\u00b9.\3\2\2\2\u00ba\u00bd\5=\37\2\u00bb\u00bd"+
		"\7a\2\2\u00bc\u00ba\3\2\2\2\u00bc\u00bb\3\2\2\2\u00bd\60\3\2\2\2\u00be"+
		"\u00bf\t\5\2\2\u00bf\62\3\2\2\2\u00c0\u00c1\t\6\2\2\u00c1\64\3\2\2\2\u00c2"+
		"\u00c3\t\7\2\2\u00c3\66\3\2\2\2\u00c4\u00c5\t\b\2\2\u00c58\3\2\2\2\u00c6"+
		"\u00c7\n\b\2\2\u00c7:\3\2\2\2\u00c8\u00ca\t\t\2\2\u00c9\u00c8\3\2\2\2"+
		"\u00ca<\3\2\2\2\u00cb\u00cd\t\n\2\2\u00cc\u00cb\3\2\2\2\u00cd>\3\2\2\2"+
		"\u00ce\u00d0\t\13\2\2\u00cf\u00ce\3\2\2\2\u00d0\u00d1\3\2\2\2\u00d1\u00cf"+
		"\3\2\2\2\u00d1\u00d2\3\2\2\2\u00d2\u00d3\3\2\2\2\u00d3\u00d4\b \2\2\u00d4"+
		"@\3\2\2\2\u00d5\u00d6\7\61\2\2\u00d6\u00d7\7,\2\2\u00d7\u00db\3\2\2\2"+
		"\u00d8\u00da\13\2\2\2\u00d9\u00d8\3\2\2\2\u00da\u00dd\3\2\2\2\u00db\u00dc"+
		"\3\2\2\2\u00db\u00d9\3\2\2\2\u00dc\u00de\3\2\2\2\u00dd\u00db\3\2\2\2\u00de"+
		"\u00df\7,\2\2\u00df\u00e0\7\61\2\2\u00e0\u00e1\3\2\2\2\u00e1\u00e2\b!"+
		"\2\2\u00e2B\3\2\2\2\u00e3\u00e4\13\2\2\2\u00e4\u00e5\3\2\2\2\u00e5\u00e6"+
		"\b\"\2\2\u00e6D\3\2\2\2\22\2lnsxz\u0082\u0084\u008d\u0093\u00ab\u00bc"+
		"\u00c9\u00cc\u00d1\u00db\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}