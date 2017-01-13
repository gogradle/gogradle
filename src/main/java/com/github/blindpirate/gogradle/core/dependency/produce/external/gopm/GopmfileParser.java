package com.github.blindpirate.gogradle.core.dependency.produce.external.gopm;

import com.github.blindpirate.gogradle.util.IOUtils;
import com.github.blindpirate.gogradle.util.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.gradle.internal.impldep.org.apache.commons.collections.map.HashedMap;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser.DIR_KEY;
import static com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser.NAME_KEY;
import static com.github.blindpirate.gogradle.util.StringUtils.isNotBlank;
import static com.github.blindpirate.gogradle.util.StringUtils.splitAndTrim;
import static com.github.blindpirate.gogradle.vcs.git.GitNotationDependency.BRANCH_KEY;
import static com.github.blindpirate.gogradle.vcs.git.GitNotationDependency.COMMIT_KEY;
import static com.github.blindpirate.gogradle.vcs.git.GitNotationDependency.TAG_KEY;

/**
 * Parses .gopmfile and generates gogradle map notations.
 * It seems that .gopmfile supports git only.
 *
 * @see <a href="https://github.com/gpmgo/docs/blob/master/en-US/gopmfile.md" >.gopmfile</a>
 */
public class GopmfileParser {
    private static final String DEPS_SECTION = "[deps]";
    private static final String BRANCH_KEYWORD = "branch:";
    private static final String TAG_KEYWORD = "tag:";
    private static final String COMMIT_KEYWORD = "commit:";
    private static final String TARGET_SECTION = "[target]";
    private static final String RES_SECTION = "[res]";

    public List<Map<String, Object>> parse(File file) {

        List<String> lines = IOUtils.getLines(file)
                .stream()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());

        int targetLineIndex = findKeyworkIndex(lines, TARGET_SECTION);
        int depsLineIndex = findKeyworkIndex(lines, DEPS_SECTION);
        int resLineIndex = findKeyworkIndex(lines, RES_SECTION);

        if (depsLineIndex == -1 || depsLineIndex == lines.size() - 1) {
            return Collections.emptyList();
        }

        int nextSectionIndex = determineNextSectionIndex(lines, targetLineIndex, depsLineIndex, resLineIndex);

        List<String> depsLines = lines.subList(depsLineIndex + 1, nextSectionIndex);

        return parseLines(depsLines);
    }

    private int determineNextSectionIndex(List<String> lines,
                                          int targetLineIndex,
                                          int depsLineIndex,
                                          int resLineIndex) {
        int nextSectionIndex = lines.size();
        if (targetLineIndex > depsLineIndex || resLineIndex > depsLineIndex) {
            nextSectionIndex = Math.max(targetLineIndex, resLineIndex);
        }
        return nextSectionIndex;
    }

    private int findKeyworkIndex(List<String> lines, String keyword) {
        for (int i = 0; i < lines.size(); ++i) {
            if (lines.get(i).trim().startsWith(keyword)) {
                return i;
            }
        }
        return -1;
    }

    private List<Map<String, Object>> parseLines(List<String> deps) {
        return deps.stream()
                .map(this::parseLine)
                .filter(Objects::nonNull)
                .map(this::toNotation)
                .collect(Collectors.toList());
    }

    private Pair<String, String> parseLine(String line) {
        String[] nameAndValue = splitAndTrim(line, "=");
        String name = nameAndValue[0];
        String value = nameAndValue.length > 1 ? nameAndValue[1] : "";
        return Pair.of(name, value);

    }

    private Map<String, Object> toNotation(Pair<String, String> nameAndValue) {
        String name = nameAndValue.getLeft();
        String value = nameAndValue.getRight();

        Map<String, Object> ret = new HashedMap();
        ret.put(NAME_KEY, name);
        if (value.startsWith(BRANCH_KEYWORD)) {
            ret.put(BRANCH_KEY, value.substring(BRANCH_KEYWORD.length()));
        } else if (value.startsWith(TAG_KEYWORD)) {
            ret.put(TAG_KEY, value.substring(TAG_KEYWORD.length()));
        } else if (value.startsWith(COMMIT_KEYWORD)) {
            ret.put(COMMIT_KEY, value.substring(COMMIT_KEYWORD.length()));
        } else if (isNotBlank(value)) {
            // it will be treated as file path
            ret.put(DIR_KEY, value);
        }
        return ret;
    }
}
