package com.github.blindpirate.gogradle.core.dependency.produce;

import com.github.blindpirate.gogradle.antlr.GolangBuildInfoBaseListener;
import com.github.blindpirate.gogradle.antlr.GolangBuildInfoLexer;
import com.github.blindpirate.gogradle.antlr.GolangBuildInfoParser;
import com.github.blindpirate.gogradle.build.Configuration;
import com.github.blindpirate.gogradle.common.GoSourceCodeFilter;
import com.github.blindpirate.gogradle.core.BuildConstraintManager;
import com.github.blindpirate.gogradle.util.IOUtils;
import com.github.blindpirate.gogradle.util.StringUtils;
import com.google.common.collect.ImmutableMap;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.github.blindpirate.gogradle.antlr.GolangBuildInfoParser.BuildOptionContext;
import static com.github.blindpirate.gogradle.antlr.GolangBuildInfoParser.BuildTagContext;
import static com.github.blindpirate.gogradle.antlr.GolangBuildInfoParser.BuildTermContext;
import static com.github.blindpirate.gogradle.antlr.GolangBuildInfoParser.ImportPathContext;

@Singleton
public class GoImportExtractor {
    private final BuildConstraintManager buildConstraintManager;

    private static final Map<Configuration, GoSourceCodeFilter> FILTERS = ImmutableMap.of(
            Configuration.BUILD, GoSourceCodeFilter.BUILD_GO_FILTER,
            Configuration.TEST, GoSourceCodeFilter.TEST_GO_FILTER
    );

    @Inject
    public GoImportExtractor(BuildConstraintManager buildConstraintManager) {
        this.buildConstraintManager = buildConstraintManager;
    }

    public Set<String> getImportPaths(File dir, Configuration configuration) {
        Collection<File> files = IOUtils.filterFilesRecursively(dir, FILTERS.get(configuration));

        return files.stream().map(IOUtils::toString)
                .map(this::extract)
                .collect(HashSet::new, HashSet::addAll, HashSet::addAll);
    }

    private List<String> extract(String sourceFileContent) {
        GolangBuildInfoLexer lexer = new GolangBuildInfoLexer(new ANTLRInputStream(sourceFileContent));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        GolangBuildInfoParser parser = new GolangBuildInfoParser(tokens);
        ParseTree tree = parser.sourceFile();
        ParseTreeWalker walker = new ParseTreeWalker();
        ImportListener listener = new ImportListener();
        walker.walk(listener, tree);

        if (shouldBeIncluded(listener)) {
            return listener.importPaths;
        } else {
            return Collections.emptyList();
        }
    }

    private boolean shouldBeIncluded(ImportListener listener) {
        return listener.buildTags.eval(buildConstraintManager.getAllConstraints());
    }

    private static class ImportListener extends GolangBuildInfoBaseListener {
        private List<String> importPaths = new ArrayList<>();
        private BuildTags buildTags = new BuildTags();

        @Override
        public void enterImportPath(ImportPathContext ctx) {
            String importPath = StringUtils.substring(ctx.STRING_LIT().getText(), 1, -1);
            importPaths.add(importPath);
        }

        @Override
        public void enterBuildTag(BuildTagContext ctx) {
            buildTags.addBuildTag(new BuildTag());
        }

        @Override
        public void enterBuildOption(BuildOptionContext ctx) {
            buildTags.getLastBuildTag().addBuildOption(new BuildOption());
        }

        @Override
        public void enterBuildTerm(BuildTermContext ctx) {
            if (parentIsOption(ctx)) {
                BuildTerm term = getTerm(ctx);
                buildTags.getLastBuildTag().getLastBuildOption().addBuildTerm(term);
            }
        }

        private BuildTerm getTerm(BuildTermContext ctx) {
            if (sonAndGrandsonBothHaveExclamation(ctx)) {
                throw new IllegalStateException("!!term is not supported!");
            } else if (sonHasExclamation(ctx)) {
                return BuildTerm.no(getTermNameOfSon(ctx));
            } else {
                return BuildTerm.yes(getTermName(ctx));
            }
        }

        private String getTermName(BuildTermContext ctx) {
            return ctx.IDENTIFIER().getText();
        }

        private String getTermNameOfSon(BuildTermContext ctx) {
            return ctx.getChild(BuildTermContext.class, 0).IDENTIFIER().getText();
        }

        private boolean sonHasExclamation(BuildTermContext ctx) {
            return "!".equals(ctx.getChild(0).getText());
        }

        private boolean parentIsOption(BuildTermContext ctx) {
            return ctx.getParent() instanceof BuildOptionContext;
        }

        private boolean sonAndGrandsonBothHaveExclamation(BuildTermContext ctx) {
            return "!".equals(ctx.getChild(0).getText())
                    && "!".equals(ctx.getChild(1).getChild(0).getText());
        }
    }

    // a BuildTags instance represents multiple build tag lines
    private static final class BuildTags {
        private List<BuildTag> buildTags = new ArrayList<>();

        void addBuildTag(BuildTag tag) {
            this.buildTags.add(tag);
        }

        BuildTag getLastBuildTag() {
            return buildTags.get(buildTags.size() - 1);
        }

        boolean eval(Set<String> ctx) {
            return buildTags.stream()
                    .allMatch(tag -> tag.eval(ctx));
        }
    }

    // a BuildTag instance represents a build tag line, i.e. many BuildOptions separated by space
    // A build constraint(tag) is evaluated as the OR of space-separated options
    private static final class BuildTag {
        private List<BuildOption> buildOptions = new ArrayList<>();

        public boolean eval(Set<String> ctx) {
            return buildOptions.stream()
                    .anyMatch(option -> option.eval(ctx));
        }

        public void addBuildOption(BuildOption buildOption) {
            this.buildOptions.add(buildOption);
        }

        BuildOption getLastBuildOption() {
            return buildOptions.get(buildOptions.size() - 1);
        }
    }

    // a BuildOption instance represents an option, i.e. many BuildTerms separated by comma
    // each option evaluates as the AND of its comma-separated terms
    private static final class BuildOption {
        private List<BuildTerm> buildTerms = new ArrayList<>();

        public boolean eval(Set<String> ctx) {
            return buildTerms.stream()
                    .allMatch(term -> term.eval(ctx));
        }

        public void addBuildTerm(BuildTerm buildTerm) {
            this.buildTerms.add(buildTerm);
        }
    }

    private static final class BuildTerm {
        private String name;
        private boolean precededByExclamation;

        private BuildTerm(String name, boolean precededByExclamation) {
            this.name = name;
            this.precededByExclamation = precededByExclamation;
        }

        static BuildTerm yes(String name) {
            return new BuildTerm(name, false);
        }

        static BuildTerm no(String name) {
            return new BuildTerm(name, true);
        }

        public boolean eval(Set<String> ctx) {
            if (precededByExclamation) {
                return !ctx.contains(name);
            } else {
                return ctx.contains(name);
            }
        }
    }
}
