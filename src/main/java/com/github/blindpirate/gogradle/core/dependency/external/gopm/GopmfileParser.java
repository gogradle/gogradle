package com.github.blindpirate.gogradle.core.dependency.external.gopm;

import com.github.blindpirate.gogradle.util.IOUtils;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;
import org.gradle.internal.impldep.org.apache.commons.collections.map.HashedMap;

import javax.inject.Singleton;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.github.blindpirate.gogradle.core.dependency.GitDependency.BRANCH_KEY;
import static com.github.blindpirate.gogradle.core.dependency.GitDependency.COMMIT_KEY;
import static com.github.blindpirate.gogradle.core.dependency.GitDependency.TAG_KEY;
import static com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser.DIR_KEY;
import static com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser.NAME_KEY;
import static com.github.blindpirate.gogradle.util.StringUtils.isBlank;
import static com.github.blindpirate.gogradle.util.StringUtils.isNotBlank;
import static com.github.blindpirate.gogradle.util.StringUtils.splitAndTrim;
import static com.github.blindpirate.gogradle.util.StringUtils.splitToLines;

/**
 * @see <a href="https://github.com/gpmgo/docs/blob/master/en-US/gopmfile.md" >./.gopmfile</a>
 * <p>
 * [target]
 * path = github.com/gogits/gogs
 * <p>
 * [deps]
 * github.com/bradfitz/gomemcache = commit:fb1f79c
 * github.com/urfave/cli = commit:1efa31f
 * github.com/go-macaron/binding = commit:9440f33
 * github.com/go-macaron/cache = commit:5617353
 * github.com/go-macaron/captcha = commit:8aa5919
 * github.com/go-macaron/csrf = commit:6a9a7df
 * github.com/go-macaron/gzip = commit:cad1c65
 * github.com/go-macaron/i18n = commit:ef57533
 * github.com/go-macaron/inject = commit:c5ab7bf
 * github.com/go-macaron/session = commit:66031fc
 * github.com/go-macaron/toolbox = commit:82b5115
 * github.com/go-sql-driver/mysql = commit:0b58b37
 * github.com/go-xorm/builder = commit:cd42e83
 * github.com/go-xorm/core = commit:2ec3936
 * github.com/go-xorm/xorm = commit:311abf2
 * github.com/gogits/chardet = commit:2404f77
 * github.com/gogits/cron = commit:7f3990a
 * github.com/gogits/git-module = commit:e59143d
 * github.com/gogits/go-gogs-client = commit:c52f7ee
 * github.com/gogits/go-libravatar = commit:cd1abbd
 * github.com/issue9/identicon = commit:d36b545
 * github.com/jaytaylor/html2text = commit:52d9b78
 * github.com/kardianos/minwinsvc = commit:cad6b2b
 * github.com/klauspost/compress = commit:14eb9c4
 * github.com/klauspost/cpuid = commit:09cded8
 * github.com/klauspost/crc32 = commit:19b0b33
 * github.com/lib/pq = commit:80f8150
 * github.com/mattn/go-sqlite3 = commit:e118d44
 * github.com/mcuadros/go-version = commit:d52711f
 * github.com/microcosm-cc/bluemonday = commit:9dc1992
 * github.com/msteinert/pam = commit:02ccfbf
 * github.com/nfnt/resize = commit:891127d
 * github.com/russross/blackfriday = commit:93622da
 * github.com/satori/go.uuid = commit:0aa62d5
 * github.com/sergi/go-diff = commit:ec7fdbb
 * github.com/shurcooL/sanitized_anchor_name = commit:10ef21a
 * github.com/Unknwon/cae = commit:7f5e046
 * github.com/Unknwon/com = commit:28b053d
 * github.com/Unknwon/i18n = commit:39d6f27
 * github.com/Unknwon/paginater = commit:7748a72
 * golang.org/x/crypto = commit:bc89c49
 * golang.org/x/net = commit:57bfaa8
 * golang.org/x/sys = commit:a646d33
 * golang.org/x/text = commit:2910a50
 * gopkg.in/alexcesaro/quotedprintable.v3 = commit:2caba25
 * gopkg.in/asn1-ber.v1 = commit:4e86f43
 * gopkg.in/bufio.v1 = commit:567b2bf
 * gopkg.in/editorconfig/editorconfig-core-go.v1 = commit:a872f05
 * gopkg.in/gomail.v2 = commit:81ebce5
 * gopkg.in/ini.v1 = commit:cf53f92
 * gopkg.in/ldap.v2 = commit:d0a5ced
 * gopkg.in/macaron.v1 = commit:826ddf1
 * gopkg.in/redis.v2 = commit:e617904
 * <p>
 * [res]
 * include = public|scripts|templates
 */
// TODO
// FileBasedConfig in JGit can't parse this since it sees the key 'github/x/y' as invalid key (unfortunately, it is)
// current parser is simple, we should use ANTLR to do the parsing.
@Singleton
public class GopmfileParser {
    private static final String DEPS_SECTION = "[deps]";
    private static final String BRANCH_KEYWORD = "branch:";
    private static final String TAG_KEYWORD = "tag:";
    private static final String COMMIT_KEYWORD = "commit:";
    private static final String TARGET_SECTION = "[target]";
    private static final String RES_SECTION = "[res]";

    public List<Map<String, Object>> parse(File file) {

        List<String> lines = splitToLines(IOUtils.toString(file));

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
        List<Map<String, Object>> ret = new ArrayList<>();
        for (String line : deps) {
            Optional<Pair<String, String>> nameAndValue = parseLine(line);
            if (nameAndValue.isPresent()) {
                ret.add(toNotation(nameAndValue.get().getLeft(), nameAndValue.get().getRight()));
            }
        }
        return ret;
    }

    private Optional<Pair<String, String>> parseLine(String line) {
        if (isBlank(line)) {
            return Optional.empty();
        }
        String[] nameAndValue = splitAndTrim(line, "=");
        String name = nameAndValue[0];
        String value = nameAndValue.length > 1 ? nameAndValue[1] : "";
        return Optional.of(Pair.of(name, value));

    }

    private Map<String, Object> toNotation(String name, String value) {
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
