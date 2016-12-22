// Generated from GolangImport.g4 by ANTLR 4.6
package com.github.blindpirate.gogradle.antlr;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link GolangImportParser}.
 */
public interface GolangImportListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link GolangImportParser#sourceFile}.
	 * @param ctx the parse tree
	 */
	void enterSourceFile(GolangImportParser.SourceFileContext ctx);
	/**
	 * Exit a parse tree produced by {@link GolangImportParser#sourceFile}.
	 * @param ctx the parse tree
	 */
	void exitSourceFile(GolangImportParser.SourceFileContext ctx);
	/**
	 * Enter a parse tree produced by {@link GolangImportParser#packageClause}.
	 * @param ctx the parse tree
	 */
	void enterPackageClause(GolangImportParser.PackageClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link GolangImportParser#packageClause}.
	 * @param ctx the parse tree
	 */
	void exitPackageClause(GolangImportParser.PackageClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link GolangImportParser#importDecl}.
	 * @param ctx the parse tree
	 */
	void enterImportDecl(GolangImportParser.ImportDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link GolangImportParser#importDecl}.
	 * @param ctx the parse tree
	 */
	void exitImportDecl(GolangImportParser.ImportDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link GolangImportParser#importSpec}.
	 * @param ctx the parse tree
	 */
	void enterImportSpec(GolangImportParser.ImportSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link GolangImportParser#importSpec}.
	 * @param ctx the parse tree
	 */
	void exitImportSpec(GolangImportParser.ImportSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link GolangImportParser#importPath}.
	 * @param ctx the parse tree
	 */
	void enterImportPath(GolangImportParser.ImportPathContext ctx);
	/**
	 * Exit a parse tree produced by {@link GolangImportParser#importPath}.
	 * @param ctx the parse tree
	 */
	void exitImportPath(GolangImportParser.ImportPathContext ctx);
}