package com.github.blindpirate.gogradle.core.dependency.provider;

import com.github.blindpirate.gogradle.core.dependency.GitDependency;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.blindpirate.gogradle.core.dependency.resolve.DependencyResolutionException;
import com.github.blindpirate.gogradle.util.Assert;
import com.github.blindpirate.gogradle.util.StringUtils;

import java.util.Arrays;
import java.util.List;

public class GithubNotationParser implements NotationParser<String> {

    // TODO there are too many "github.com"s in the code
    private static final String GITHUB_HOST = "github.com";

    private static final String TAG_SEPERATOR = "@";
    private static final String COMMIT_SEPERATOR = "#";

    @Override
    public boolean accept(String name) {
        return name.startsWith(GITHUB_HOST);
    }

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


    @Override
    public GolangDependency produce(String notation) {
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

    private GolangDependency buildByName(String notation) {
        String url = buildUrl(notation);
        return GitDependency.builder()
                .withName(notation)
                .withUrl(url)
                .withNewestCommit()
                .build();
    }

    private GolangDependency buildByCommit(String notation) {
        String[] array = StringUtils.splitAndTrim(notation, COMMIT_SEPERATOR);

        Assert.isTrue(array.length == 2, "Invalid notation:" + notation);

        String name = array[0];
        String url = buildUrl(name);
        String commit = array[1];
        return GitDependency.builder()
                .withName(name)
                .withUrl(url)
                .withCommit(commit)
                .build();
    }

    private String buildUrl(String name) {
        return "https://" + name + ".git";
    }

    private GolangDependency buildByTag(String notation) {
        int indexOfAt = notation.indexOf(TAG_SEPERATOR);

        String name = notation.substring(0, indexOfAt);
        String url = buildUrl(name);
        String tag = notation.substring(indexOfAt + 1);

        return GitDependency.builder()
                .withName(name)
                .withUrl(url)
                .withTag(tag)
                .build();
    }
}
