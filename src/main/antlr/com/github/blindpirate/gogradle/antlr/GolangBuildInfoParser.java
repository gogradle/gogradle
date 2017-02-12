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
		NEWLINE=15, WS=16, COMMENT=17, ANYOTHER=18;
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
			_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(18);
					commentLine();
					}
					} 
				}
				setState(23);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
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
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__1) | (1L << T__2) | (1L << T__3) | (1L << T__4) | (1L << T__5) | (1L << T__6) | (1L << T__7) | (1L << T__8) | (1L << T__9) | (1L << IDENTIFIER) | (1L << STRING_LIT) | (1L << LITTLE_U_VALUE) | (1L << BIG_U_VALUE) | (1L << NEWLINE) | (1L << WS) | (1L << COMMENT) | (1L << ANYOTHER))) != 0)) {
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
		public List<TerminalNode> NEWLINE() { return getTokens(GolangBuildInfoParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(GolangBuildInfoParser.NEWLINE, i);
		}
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
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(46);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(43);
				match(NEWLINE);
				}
				}
				setState(48);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(49);
			match(T__1);
			setState(50);
			match(IDENTIFIER);
			setState(54);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,6,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(51);
					match(NEWLINE);
					}
					} 
				}
				setState(56);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,6,_ctx);
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

	public static class ImportDeclContext extends ParserRuleContext {
		public List<ImportSpecContext> importSpec() {
			return getRuleContexts(ImportSpecContext.class);
		}
		public ImportSpecContext importSpec(int i) {
			return getRuleContext(ImportSpecContext.class,i);
		}
		public List<TerminalNode> NEWLINE() { return getTokens(GolangBuildInfoParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(GolangBuildInfoParser.NEWLINE, i);
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
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(60);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(57);
				match(NEWLINE);
				}
				}
				setState(62);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(63);
			match(T__2);
			setState(76);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__5:
			case IDENTIFIER:
			case STRING_LIT:
			case NEWLINE:
				{
				setState(64);
				importSpec();
				}
				break;
			case T__3:
				{
				setState(65);
				match(T__3);
				setState(72);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__5) | (1L << IDENTIFIER) | (1L << STRING_LIT) | (1L << NEWLINE))) != 0)) {
					{
					{
					setState(66);
					importSpec();
					setState(68);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==T__0) {
						{
						setState(67);
						match(T__0);
						}
					}

					}
					}
					setState(74);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(75);
				match(T__4);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(81);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(78);
					match(NEWLINE);
					}
					} 
				}
				setState(83);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
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
		public List<TerminalNode> NEWLINE() { return getTokens(GolangBuildInfoParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(GolangBuildInfoParser.NEWLINE, i);
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
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(87);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(84);
				match(NEWLINE);
				}
				}
				setState(89);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(91);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__5 || _la==IDENTIFIER) {
				{
				setState(90);
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

			setState(93);
			importPath();
			setState(97);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(94);
					match(NEWLINE);
					}
					} 
				}
				setState(99);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
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
			setState(100);
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
		public List<TerminalNode> NEWLINE() { return getTokens(GolangBuildInfoParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(GolangBuildInfoParser.NEWLINE, i);
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
		int _la;
		try {
			int _alt;
			setState(145);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,20,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(105);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==NEWLINE) {
					{
					{
					setState(102);
					match(NEWLINE);
					}
					}
					setState(107);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(108);
				match(T__6);
				setState(109);
				match(T__7);
				setState(110);
				buildTag();
				setState(111);
				match(NEWLINE);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(116);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==NEWLINE) {
					{
					{
					setState(113);
					match(NEWLINE);
					}
					}
					setState(118);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(119);
				match(T__6);
				setState(120);
				match(T__7);
				setState(121);
				buildTag();
				setState(122);
				match(T__6);
				setState(126);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,17,_ctx);
				while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(123);
						matchWildcard();
						}
						} 
					}
					setState(128);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,17,_ctx);
				}
				setState(129);
				match(NEWLINE);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(134);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==NEWLINE) {
					{
					{
					setState(131);
					match(NEWLINE);
					}
					}
					setState(136);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(137);
				match(T__6);
				setState(141);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
				while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(138);
						matchWildcard();
						}
						} 
					}
					setState(143);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
				}
				setState(144);
				match(NEWLINE);
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
			setState(147);
			buildOption();
			setState(151);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__9 || _la==IDENTIFIER) {
				{
				{
				setState(148);
				buildOption();
				}
				}
				setState(153);
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
			setState(154);
			buildTerm();
			setState(159);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__8) {
				{
				{
				setState(155);
				match(T__8);
				setState(156);
				buildTerm();
				}
				}
				setState(161);
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
			setState(165);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case IDENTIFIER:
				enterOuterAlt(_localctx, 1);
				{
				setState(162);
				match(IDENTIFIER);
				}
				break;
			case T__9:
				enterOuterAlt(_localctx, 2);
				{
				setState(163);
				match(T__9);
				setState(164);
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
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\24\u00aa\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\3\2\7"+
		"\2\26\n\2\f\2\16\2\31\13\2\3\2\3\2\5\2\35\n\2\3\2\3\2\5\2!\n\2\7\2#\n"+
		"\2\f\2\16\2&\13\2\3\2\7\2)\n\2\f\2\16\2,\13\2\3\3\7\3/\n\3\f\3\16\3\62"+
		"\13\3\3\3\3\3\3\3\7\3\67\n\3\f\3\16\3:\13\3\3\4\7\4=\n\4\f\4\16\4@\13"+
		"\4\3\4\3\4\3\4\3\4\3\4\5\4G\n\4\7\4I\n\4\f\4\16\4L\13\4\3\4\5\4O\n\4\3"+
		"\4\7\4R\n\4\f\4\16\4U\13\4\3\5\7\5X\n\5\f\5\16\5[\13\5\3\5\5\5^\n\5\3"+
		"\5\3\5\7\5b\n\5\f\5\16\5e\13\5\3\6\3\6\3\7\7\7j\n\7\f\7\16\7m\13\7\3\7"+
		"\3\7\3\7\3\7\3\7\3\7\7\7u\n\7\f\7\16\7x\13\7\3\7\3\7\3\7\3\7\3\7\7\7\177"+
		"\n\7\f\7\16\7\u0082\13\7\3\7\3\7\3\7\7\7\u0087\n\7\f\7\16\7\u008a\13\7"+
		"\3\7\3\7\7\7\u008e\n\7\f\7\16\7\u0091\13\7\3\7\5\7\u0094\n\7\3\b\3\b\7"+
		"\b\u0098\n\b\f\b\16\b\u009b\13\b\3\t\3\t\3\t\7\t\u00a0\n\t\f\t\16\t\u00a3"+
		"\13\t\3\n\3\n\3\n\5\n\u00a8\n\n\3\n\4\u0080\u008f\2\13\2\4\6\b\n\f\16"+
		"\20\22\2\3\4\2\b\b\r\r\u00b9\2\27\3\2\2\2\4\60\3\2\2\2\6>\3\2\2\2\bY\3"+
		"\2\2\2\nf\3\2\2\2\f\u0093\3\2\2\2\16\u0095\3\2\2\2\20\u009c\3\2\2\2\22"+
		"\u00a7\3\2\2\2\24\26\5\f\7\2\25\24\3\2\2\2\26\31\3\2\2\2\27\25\3\2\2\2"+
		"\27\30\3\2\2\2\30\32\3\2\2\2\31\27\3\2\2\2\32\34\5\4\3\2\33\35\7\3\2\2"+
		"\34\33\3\2\2\2\34\35\3\2\2\2\35$\3\2\2\2\36 \5\6\4\2\37!\7\3\2\2 \37\3"+
		"\2\2\2 !\3\2\2\2!#\3\2\2\2\"\36\3\2\2\2#&\3\2\2\2$\"\3\2\2\2$%\3\2\2\2"+
		"%*\3\2\2\2&$\3\2\2\2\')\13\2\2\2(\'\3\2\2\2),\3\2\2\2*(\3\2\2\2*+\3\2"+
		"\2\2+\3\3\2\2\2,*\3\2\2\2-/\7\21\2\2.-\3\2\2\2/\62\3\2\2\2\60.\3\2\2\2"+
		"\60\61\3\2\2\2\61\63\3\2\2\2\62\60\3\2\2\2\63\64\7\4\2\2\648\7\r\2\2\65"+
		"\67\7\21\2\2\66\65\3\2\2\2\67:\3\2\2\28\66\3\2\2\289\3\2\2\29\5\3\2\2"+
		"\2:8\3\2\2\2;=\7\21\2\2<;\3\2\2\2=@\3\2\2\2><\3\2\2\2>?\3\2\2\2?A\3\2"+
		"\2\2@>\3\2\2\2AN\7\5\2\2BO\5\b\5\2CJ\7\6\2\2DF\5\b\5\2EG\7\3\2\2FE\3\2"+
		"\2\2FG\3\2\2\2GI\3\2\2\2HD\3\2\2\2IL\3\2\2\2JH\3\2\2\2JK\3\2\2\2KM\3\2"+
		"\2\2LJ\3\2\2\2MO\7\7\2\2NB\3\2\2\2NC\3\2\2\2OS\3\2\2\2PR\7\21\2\2QP\3"+
		"\2\2\2RU\3\2\2\2SQ\3\2\2\2ST\3\2\2\2T\7\3\2\2\2US\3\2\2\2VX\7\21\2\2W"+
		"V\3\2\2\2X[\3\2\2\2YW\3\2\2\2YZ\3\2\2\2Z]\3\2\2\2[Y\3\2\2\2\\^\t\2\2\2"+
		"]\\\3\2\2\2]^\3\2\2\2^_\3\2\2\2_c\5\n\6\2`b\7\21\2\2a`\3\2\2\2be\3\2\2"+
		"\2ca\3\2\2\2cd\3\2\2\2d\t\3\2\2\2ec\3\2\2\2fg\7\16\2\2g\13\3\2\2\2hj\7"+
		"\21\2\2ih\3\2\2\2jm\3\2\2\2ki\3\2\2\2kl\3\2\2\2ln\3\2\2\2mk\3\2\2\2no"+
		"\7\t\2\2op\7\n\2\2pq\5\16\b\2qr\7\21\2\2r\u0094\3\2\2\2su\7\21\2\2ts\3"+
		"\2\2\2ux\3\2\2\2vt\3\2\2\2vw\3\2\2\2wy\3\2\2\2xv\3\2\2\2yz\7\t\2\2z{\7"+
		"\n\2\2{|\5\16\b\2|\u0080\7\t\2\2}\177\13\2\2\2~}\3\2\2\2\177\u0082\3\2"+
		"\2\2\u0080\u0081\3\2\2\2\u0080~\3\2\2\2\u0081\u0083\3\2\2\2\u0082\u0080"+
		"\3\2\2\2\u0083\u0084\7\21\2\2\u0084\u0094\3\2\2\2\u0085\u0087\7\21\2\2"+
		"\u0086\u0085\3\2\2\2\u0087\u008a\3\2\2\2\u0088\u0086\3\2\2\2\u0088\u0089"+
		"\3\2\2\2\u0089\u008b\3\2\2\2\u008a\u0088\3\2\2\2\u008b\u008f\7\t\2\2\u008c"+
		"\u008e\13\2\2\2\u008d\u008c\3\2\2\2\u008e\u0091\3\2\2\2\u008f\u0090\3"+
		"\2\2\2\u008f\u008d\3\2\2\2\u0090\u0092\3\2\2\2\u0091\u008f\3\2\2\2\u0092"+
		"\u0094\7\21\2\2\u0093k\3\2\2\2\u0093v\3\2\2\2\u0093\u0088\3\2\2\2\u0094"+
		"\r\3\2\2\2\u0095\u0099\5\20\t\2\u0096\u0098\5\20\t\2\u0097\u0096\3\2\2"+
		"\2\u0098\u009b\3\2\2\2\u0099\u0097\3\2\2\2\u0099\u009a\3\2\2\2\u009a\17"+
		"\3\2\2\2\u009b\u0099\3\2\2\2\u009c\u00a1\5\22\n\2\u009d\u009e\7\13\2\2"+
		"\u009e\u00a0\5\22\n\2\u009f\u009d\3\2\2\2\u00a0\u00a3\3\2\2\2\u00a1\u009f"+
		"\3\2\2\2\u00a1\u00a2\3\2\2\2\u00a2\21\3\2\2\2\u00a3\u00a1\3\2\2\2\u00a4"+
		"\u00a8\7\r\2\2\u00a5\u00a6\7\f\2\2\u00a6\u00a8\5\22\n\2\u00a7\u00a4\3"+
		"\2\2\2\u00a7\u00a5\3\2\2\2\u00a8\23\3\2\2\2\32\27\34 $*\608>FJNSY]ckv"+
		"\u0080\u0088\u008f\u0093\u0099\u00a1\u00a7";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}