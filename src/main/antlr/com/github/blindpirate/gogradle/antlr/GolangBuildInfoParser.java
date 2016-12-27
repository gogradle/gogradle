// Generated from GolangBuildInfo.g4 by ANTLR 4.6
package com.github.blindpirate.gogradle.antlr;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class GolangBuildInfoParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.6", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, IDENTIFIER=11, STRING_LIT=12, LITTLE_U_VALUE=13, BIG_U_VALUE=14, 
		WS=15, COMMENT=16, ANYOTHER=17;
	public static final int
		RULE_sourceFile = 0, RULE_packageClause = 1, RULE_importDecl = 2, RULE_importSpec = 3, 
		RULE_importPath = 4, RULE_commentLine = 5, RULE_buildTag = 6, RULE_buildOption = 7, 
		RULE_buildTerm = 8;
	public static final String[] ruleNames = {
		"sourceFile", "packageClause", "importDecl", "importSpec", "importPath", 
		"commentLine", "buildTag", "buildOption", "buildTerm"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "';'", "'package'", "'import'", "'('", "')'", "'.'", "'//'", "'+build'", 
		"','", "'!'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, null, null, null, null, "IDENTIFIER", 
		"STRING_LIT", "LITTLE_U_VALUE", "BIG_U_VALUE", "WS", "COMMENT", "ANYOTHER"
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

	@Override
	public String getGrammarFileName() { return "GolangBuildInfo.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public GolangBuildInfoParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class SourceFileContext extends ParserRuleContext {
		public PackageClauseContext packageClause() {
			return getRuleContext(PackageClauseContext.class,0);
		}
		public List<CommentLineContext> commentLine() {
			return getRuleContexts(CommentLineContext.class);
		}
		public CommentLineContext commentLine(int i) {
			return getRuleContext(CommentLineContext.class,i);
		}
		public List<ImportDeclContext> importDecl() {
			return getRuleContexts(ImportDeclContext.class);
		}
		public ImportDeclContext importDecl(int i) {
			return getRuleContext(ImportDeclContext.class,i);
		}
		public SourceFileContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sourceFile; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof GolangBuildInfoListener ) ((GolangBuildInfoListener)listener).enterSourceFile(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof GolangBuildInfoListener ) ((GolangBuildInfoListener)listener).exitSourceFile(this);
		}
	}

	public final SourceFileContext sourceFile() throws RecognitionException {
		SourceFileContext _localctx = new SourceFileContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_sourceFile);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(21);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__6) {
				{
				{
				setState(18);
				commentLine();
				}
				}
				setState(23);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(24);
			packageClause();
			setState(26);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				{
				setState(25);
				match(T__0);
				}
				break;
			}
			setState(34);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(28);
					importDecl();
					setState(30);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
					case 1:
						{
						setState(29);
						match(T__0);
						}
						break;
					}
					}
					} 
				}
				setState(36);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,3,_ctx);
			}
			setState(40);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__1) | (1L << T__2) | (1L << T__3) | (1L << T__4) | (1L << T__5) | (1L << T__6) | (1L << T__7) | (1L << T__8) | (1L << T__9) | (1L << IDENTIFIER) | (1L << STRING_LIT) | (1L << LITTLE_U_VALUE) | (1L << BIG_U_VALUE) | (1L << WS) | (1L << COMMENT) | (1L << ANYOTHER))) != 0)) {
				{
				{
				setState(37);
				matchWildcard();
				}
				}
				setState(42);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PackageClauseContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(GolangBuildInfoParser.IDENTIFIER, 0); }
		public PackageClauseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_packageClause; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof GolangBuildInfoListener ) ((GolangBuildInfoListener)listener).enterPackageClause(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof GolangBuildInfoListener ) ((GolangBuildInfoListener)listener).exitPackageClause(this);
		}
	}

	public final PackageClauseContext packageClause() throws RecognitionException {
		PackageClauseContext _localctx = new PackageClauseContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_packageClause);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(43);
			match(T__1);
			setState(44);
			match(IDENTIFIER);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ImportDeclContext extends ParserRuleContext {
		public List<ImportSpecContext> importSpec() {
			return getRuleContexts(ImportSpecContext.class);
		}
		public ImportSpecContext importSpec(int i) {
			return getRuleContext(ImportSpecContext.class,i);
		}
		public ImportDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_importDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof GolangBuildInfoListener ) ((GolangBuildInfoListener)listener).enterImportDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof GolangBuildInfoListener ) ((GolangBuildInfoListener)listener).exitImportDecl(this);
		}
	}

	public final ImportDeclContext importDecl() throws RecognitionException {
		ImportDeclContext _localctx = new ImportDeclContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_importDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(46);
			match(T__2);
			setState(59);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__5:
			case IDENTIFIER:
			case STRING_LIT:
				{
				setState(47);
				importSpec();
				}
				break;
			case T__3:
				{
				setState(48);
				match(T__3);
				setState(55);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__5) | (1L << IDENTIFIER) | (1L << STRING_LIT))) != 0)) {
					{
					{
					setState(49);
					importSpec();
					setState(51);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==T__0) {
						{
						setState(50);
						match(T__0);
						}
					}

					}
					}
					setState(57);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(58);
				match(T__4);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ImportSpecContext extends ParserRuleContext {
		public ImportPathContext importPath() {
			return getRuleContext(ImportPathContext.class,0);
		}
		public TerminalNode IDENTIFIER() { return getToken(GolangBuildInfoParser.IDENTIFIER, 0); }
		public ImportSpecContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_importSpec; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof GolangBuildInfoListener ) ((GolangBuildInfoListener)listener).enterImportSpec(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof GolangBuildInfoListener ) ((GolangBuildInfoListener)listener).exitImportSpec(this);
		}
	}

	public final ImportSpecContext importSpec() throws RecognitionException {
		ImportSpecContext _localctx = new ImportSpecContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_importSpec);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(62);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__5 || _la==IDENTIFIER) {
				{
				setState(61);
				_la = _input.LA(1);
				if ( !(_la==T__5 || _la==IDENTIFIER) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
			}

			setState(64);
			importPath();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ImportPathContext extends ParserRuleContext {
		public TerminalNode STRING_LIT() { return getToken(GolangBuildInfoParser.STRING_LIT, 0); }
		public ImportPathContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_importPath; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof GolangBuildInfoListener ) ((GolangBuildInfoListener)listener).enterImportPath(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof GolangBuildInfoListener ) ((GolangBuildInfoListener)listener).exitImportPath(this);
		}
	}

	public final ImportPathContext importPath() throws RecognitionException {
		ImportPathContext _localctx = new ImportPathContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_importPath);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(66);
			match(STRING_LIT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CommentLineContext extends ParserRuleContext {
		public BuildTagContext buildTag() {
			return getRuleContext(BuildTagContext.class,0);
		}
		public CommentLineContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_commentLine; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof GolangBuildInfoListener ) ((GolangBuildInfoListener)listener).enterCommentLine(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof GolangBuildInfoListener ) ((GolangBuildInfoListener)listener).exitCommentLine(this);
		}
	}

	public final CommentLineContext commentLine() throws RecognitionException {
		CommentLineContext _localctx = new CommentLineContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_commentLine);
		try {
			int _alt;
			setState(78);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(68);
				match(T__6);
				setState(69);
				match(T__7);
				setState(70);
				buildTag();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(71);
				match(T__6);
				setState(75);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,9,_ctx);
				while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(72);
						matchWildcard();
						}
						} 
					}
					setState(77);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,9,_ctx);
				}
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BuildTagContext extends ParserRuleContext {
		public List<BuildOptionContext> buildOption() {
			return getRuleContexts(BuildOptionContext.class);
		}
		public BuildOptionContext buildOption(int i) {
			return getRuleContext(BuildOptionContext.class,i);
		}
		public BuildTagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_buildTag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof GolangBuildInfoListener ) ((GolangBuildInfoListener)listener).enterBuildTag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof GolangBuildInfoListener ) ((GolangBuildInfoListener)listener).exitBuildTag(this);
		}
	}

	public final BuildTagContext buildTag() throws RecognitionException {
		BuildTagContext _localctx = new BuildTagContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_buildTag);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(80);
			buildOption();
			setState(84);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__9 || _la==IDENTIFIER) {
				{
				{
				setState(81);
				buildOption();
				}
				}
				setState(86);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BuildOptionContext extends ParserRuleContext {
		public List<BuildTermContext> buildTerm() {
			return getRuleContexts(BuildTermContext.class);
		}
		public BuildTermContext buildTerm(int i) {
			return getRuleContext(BuildTermContext.class,i);
		}
		public BuildOptionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_buildOption; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof GolangBuildInfoListener ) ((GolangBuildInfoListener)listener).enterBuildOption(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof GolangBuildInfoListener ) ((GolangBuildInfoListener)listener).exitBuildOption(this);
		}
	}

	public final BuildOptionContext buildOption() throws RecognitionException {
		BuildOptionContext _localctx = new BuildOptionContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_buildOption);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(87);
			buildTerm();
			setState(92);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__8) {
				{
				{
				setState(88);
				match(T__8);
				setState(89);
				buildTerm();
				}
				}
				setState(94);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BuildTermContext extends ParserRuleContext {
		public TerminalNode IDENTIFIER() { return getToken(GolangBuildInfoParser.IDENTIFIER, 0); }
		public BuildTermContext buildTerm() {
			return getRuleContext(BuildTermContext.class,0);
		}
		public BuildTermContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_buildTerm; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof GolangBuildInfoListener ) ((GolangBuildInfoListener)listener).enterBuildTerm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof GolangBuildInfoListener ) ((GolangBuildInfoListener)listener).exitBuildTerm(this);
		}
	}

	public final BuildTermContext buildTerm() throws RecognitionException {
		BuildTermContext _localctx = new BuildTermContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_buildTerm);
		try {
			setState(98);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENTIFIER:
				enterOuterAlt(_localctx, 1);
				{
				setState(95);
				match(IDENTIFIER);
				}
				break;
			case T__9:
				enterOuterAlt(_localctx, 2);
				{
				setState(96);
				match(T__9);
				setState(97);
				buildTerm();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\23g\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\3\2\7\2\26"+
		"\n\2\f\2\16\2\31\13\2\3\2\3\2\5\2\35\n\2\3\2\3\2\5\2!\n\2\7\2#\n\2\f\2"+
		"\16\2&\13\2\3\2\7\2)\n\2\f\2\16\2,\13\2\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3"+
		"\4\5\4\66\n\4\7\48\n\4\f\4\16\4;\13\4\3\4\5\4>\n\4\3\5\5\5A\n\5\3\5\3"+
		"\5\3\6\3\6\3\7\3\7\3\7\3\7\3\7\7\7L\n\7\f\7\16\7O\13\7\5\7Q\n\7\3\b\3"+
		"\b\7\bU\n\b\f\b\16\bX\13\b\3\t\3\t\3\t\7\t]\n\t\f\t\16\t`\13\t\3\n\3\n"+
		"\3\n\5\ne\n\n\3\n\3M\2\13\2\4\6\b\n\f\16\20\22\2\3\4\2\b\b\r\rk\2\27\3"+
		"\2\2\2\4-\3\2\2\2\6\60\3\2\2\2\b@\3\2\2\2\nD\3\2\2\2\fP\3\2\2\2\16R\3"+
		"\2\2\2\20Y\3\2\2\2\22d\3\2\2\2\24\26\5\f\7\2\25\24\3\2\2\2\26\31\3\2\2"+
		"\2\27\25\3\2\2\2\27\30\3\2\2\2\30\32\3\2\2\2\31\27\3\2\2\2\32\34\5\4\3"+
		"\2\33\35\7\3\2\2\34\33\3\2\2\2\34\35\3\2\2\2\35$\3\2\2\2\36 \5\6\4\2\37"+
		"!\7\3\2\2 \37\3\2\2\2 !\3\2\2\2!#\3\2\2\2\"\36\3\2\2\2#&\3\2\2\2$\"\3"+
		"\2\2\2$%\3\2\2\2%*\3\2\2\2&$\3\2\2\2\')\13\2\2\2(\'\3\2\2\2),\3\2\2\2"+
		"*(\3\2\2\2*+\3\2\2\2+\3\3\2\2\2,*\3\2\2\2-.\7\4\2\2./\7\r\2\2/\5\3\2\2"+
		"\2\60=\7\5\2\2\61>\5\b\5\2\629\7\6\2\2\63\65\5\b\5\2\64\66\7\3\2\2\65"+
		"\64\3\2\2\2\65\66\3\2\2\2\668\3\2\2\2\67\63\3\2\2\28;\3\2\2\29\67\3\2"+
		"\2\29:\3\2\2\2:<\3\2\2\2;9\3\2\2\2<>\7\7\2\2=\61\3\2\2\2=\62\3\2\2\2>"+
		"\7\3\2\2\2?A\t\2\2\2@?\3\2\2\2@A\3\2\2\2AB\3\2\2\2BC\5\n\6\2C\t\3\2\2"+
		"\2DE\7\16\2\2E\13\3\2\2\2FG\7\t\2\2GH\7\n\2\2HQ\5\16\b\2IM\7\t\2\2JL\13"+
		"\2\2\2KJ\3\2\2\2LO\3\2\2\2MN\3\2\2\2MK\3\2\2\2NQ\3\2\2\2OM\3\2\2\2PF\3"+
		"\2\2\2PI\3\2\2\2Q\r\3\2\2\2RV\5\20\t\2SU\5\20\t\2TS\3\2\2\2UX\3\2\2\2"+
		"VT\3\2\2\2VW\3\2\2\2W\17\3\2\2\2XV\3\2\2\2Y^\5\22\n\2Z[\7\13\2\2[]\5\22"+
		"\n\2\\Z\3\2\2\2]`\3\2\2\2^\\\3\2\2\2^_\3\2\2\2_\21\3\2\2\2`^\3\2\2\2a"+
		"e\7\r\2\2bc\7\f\2\2ce\5\22\n\2da\3\2\2\2db\3\2\2\2e\23\3\2\2\2\20\27\34"+
		" $*\659=@MPV^d";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}