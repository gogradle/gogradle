package com.github.blindpirate.gogradle.core.dependency.parse;

import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.MapUtils;

import javax.inject.Singleton;
import java.util.Map;

import static com.github.blindpirate.gogradle.core.dependency.parse.MapNotationParser.NAME_KEY;
import static com.github.blindpirate.gogradle.util.StringUtils.splitAndTrim;
import static com.github.blindpirate.gogradle.vcs.GitMercurialNotationDependency.COMMIT_KEY;
import static com.github.blindpirate.gogradle.vcs.GitMercurialNotationDependency.TAG_KEY;

// github.com/a/b
// github.com/a/b@v1.0.0
// github.com/a/b@tagName
// github.com/a/b@>=1.2.0
// github.com/a/b#commitId
// github.com/a/b@tag-with@
// github.com/a/b@tag-with#
// github.com/a/b@tag-contains@hahaha
// github.com/a/b@tag-contains#hahaha
// github.com/a/b@tag-contains@and#at-the-same-time

// sem version
@Singleton
public class GitMercurialNotationConverter implements NotationConverter {

    private static final String TAG_SEPERATOR = "@";
    private static final String COMMIT_SEPERATOR = "#";

    @Override
    public Map<String, Object> convert(String notation) {
        // Github doesn't allow '@' and '#' in repository name (and in user name either)
        // if there are multiple '@' and '#', they must be in tags.
        if (notation.contains(TAG_SEPERATOR)) {
            return buildByTag(notation);
        } else if (notation.contains(COMMIT_SEPERATOR)) {
            return buildByCommit(notation);
        } else {
            return buildByName(notation);
        }
    }


    private Map<String, Object> buildByName(String notation) {
        return MapUtils.asMap(NAME_KEY, notation);
    }

    private Map<String, Object> buildByCommit(String notation) {
        String[] array = splitAndTrim(notation, COMMIT_SEPERATOR);

        Assert.isTrue(array.length == 2, "Invalid notation:" + notation);

        String name = array[0];
        String commit = array[1];

        return MapUtils.asMap(
                NAME_KEY, name,
                COMMIT_KEY, commit);
    }

    private Map<String, Object> buildByTag(String notation) {
        int indexOfAt = notation.indexOf(TAG_SEPERATOR);

        String name = notation.substring(0, indexOfAt);
        String tag = notation.substring(indexOfAt + 1);

        return MapUtils.asMap(
                NAME_KEY, name,
                TAG_KEY, tag);
    }
}
