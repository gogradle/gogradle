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
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link GolangBuildInfoParser}.
 */
public interface GolangBuildInfoListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link GolangBuildInfoParser#sourceFile}.
	 * @param ctx the parse tree
	 */
	void enterSourceFile(GolangBuildInfoParser.SourceFileContext ctx);
	/**
	 * Exit a parse tree produced by {@link GolangBuildInfoParser#sourceFile}.
	 * @param ctx the parse tree
	 */
	void exitSourceFile(GolangBuildInfoParser.SourceFileContext ctx);
	/**
	 * Enter a parse tree produced by {@link GolangBuildInfoParser#packageClause}.
	 * @param ctx the parse tree
	 */
	void enterPackageClause(GolangBuildInfoParser.PackageClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link GolangBuildInfoParser#packageClause}.
	 * @param ctx the parse tree
	 */
	void exitPackageClause(GolangBuildInfoParser.PackageClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link GolangBuildInfoParser#importDecl}.
	 * @param ctx the parse tree
	 */
	void enterImportDecl(GolangBuildInfoParser.ImportDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link GolangBuildInfoParser#importDecl}.
	 * @param ctx the parse tree
	 */
	void exitImportDecl(GolangBuildInfoParser.ImportDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link GolangBuildInfoParser#importSpec}.
	 * @param ctx the parse tree
	 */
	void enterImportSpec(GolangBuildInfoParser.ImportSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link GolangBuildInfoParser#importSpec}.
	 * @param ctx the parse tree
	 */
	void exitImportSpec(GolangBuildInfoParser.ImportSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link GolangBuildInfoParser#importPath}.
	 * @param ctx the parse tree
	 */
	void enterImportPath(GolangBuildInfoParser.ImportPathContext ctx);
	/**
	 * Exit a parse tree produced by {@link GolangBuildInfoParser#importPath}.
	 * @param ctx the parse tree
	 */
	void exitImportPath(GolangBuildInfoParser.ImportPathContext ctx);
	/**
	 * Enter a parse tree produced by {@link GolangBuildInfoParser#commentLine}.
	 * @param ctx the parse tree
	 */
	void enterCommentLine(GolangBuildInfoParser.CommentLineContext ctx);
	/**
	 * Exit a parse tree produced by {@link GolangBuildInfoParser#commentLine}.
	 * @param ctx the parse tree
	 */
	void exitCommentLine(GolangBuildInfoParser.CommentLineContext ctx);
	/**
	 * Enter a parse tree produced by {@link GolangBuildInfoParser#buildTag}.
	 * @param ctx the parse tree
	 */
	void enterBuildTag(GolangBuildInfoParser.BuildTagContext ctx);
	/**
	 * Exit a parse tree produced by {@link GolangBuildInfoParser#buildTag}.
	 * @param ctx the parse tree
	 */
	void exitBuildTag(GolangBuildInfoParser.BuildTagContext ctx);
	/**
	 * Enter a parse tree produced by {@link GolangBuildInfoParser#buildOption}.
	 * @param ctx the parse tree
	 */
	void enterBuildOption(GolangBuildInfoParser.BuildOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link GolangBuildInfoParser#buildOption}.
	 * @param ctx the parse tree
	 */
	void exitBuildOption(GolangBuildInfoParser.BuildOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link GolangBuildInfoParser#buildTerm}.
	 * @param ctx the parse tree
	 */
	void enterBuildTerm(GolangBuildInfoParser.BuildTermContext ctx);
	/**
	 * Exit a parse tree produced by {@link GolangBuildInfoParser#buildTerm}.
	 * @param ctx the parse tree
	 */
	void exitBuildTerm(GolangBuildInfoParser.BuildTermContext ctx);
}