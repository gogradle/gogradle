package com.github.blindpirate.gogradle.core.dependency.produce;

import com.github.blindpirate.gogradle.antlr.GolangImportBaseListener;
import com.github.blindpirate.gogradle.antlr.GolangImportLexer;
import com.github.blindpirate.gogradle.antlr.GolangImportParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.util.ArrayList;
import java.util.List;

public class GoImportExtractor {
    public List<String> extract(String sourceFileContent) {
        GolangImportLexer lexer = new GolangImportLexer(new ANTLRInputStream(sourceFileContent));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        GolangImportParser parser = new GolangImportParser(tokens);
        ParseTree tree = parser.sourceFile();
        ParseTreeWalker walker = new ParseTreeWalker();
        ImportListener listener = new ImportListener();
        walker.walk(listener, tree);

        return listener.importPaths;
    }

    private static class ImportListener extends GolangImportBaseListener {
        private List<String> importPaths = new ArrayList<>();

        @Override
        public void enterImportPath(GolangImportParser.ImportPathContext ctx) {
            String importPathWithQuote = ctx.STRING_LIT().getText();
            String importPath = importPathWithQuote.substring(1, importPathWithQuote.length() - 1);
            importPaths.add(importPath);
        }
    }
}
