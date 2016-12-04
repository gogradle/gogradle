package com.github.blindpirate.gogradle.core.dependency.provider;

import com.github.blindpirate.gogradle.core.dependency.GitDependency;
import com.github.blindpirate.gogradle.core.dependency.GolangDependency;
import com.github.zafarkhaja.semver.UnexpectedCharacterException;
import com.github.zafarkhaja.semver.Version;

public class GithubPackageProvider implements PackageProvider {

    private static final String GITHUB_HOST = "github.com";

    @Override
    public boolean accept(String name) {
        return name.startsWith(GITHUB_HOST);
    }

    // github.com/a/b@v1.0.0
    // github.com/a/b@tagName
    // github.com/a/b@>=1.2.0
    // github.com/a/b#commitId
    @Override
    public GolangDependency parse(String name) {

        // TODO not precise
        if (name.contains("@")) {
            String[] nameAndTag = name.split("@");
            return buildByNameAndTag(nameAndTag[0], nameAndTag[1]);
        } else if (name.contains("#")) {
            String[] nameAndCommit = name.split("#");
            return buildByNameAndCommit(nameAndCommit[0], nameAndCommit[1]);
        } else {
            return GitDependency.builder()
                    .withUrl(buildUrl(name))
                    .withName(name)
                    .build();
        }
    }

    private GolangDependency buildByNameAndCommit(String name, String commit) {
        String url = buildUrl(name);
        return GitDependency.builder()
                .withName(name)
                .withUrl(url)
                .withCommit(commit)
                .build();
    }

    private String buildUrl(String name) {
        return "https://" + name + ".git";
    }

    private GolangDependency buildByNameAndTag(String name, String tag) {
        Version semVersion = null;
        String url = buildUrl(name);
        try {
            semVersion = Version.valueOf(tag);
        } catch (UnexpectedCharacterException e) {
            // ok to ignore
        }
        return GitDependency.builder()
                .withName(name)
                .withUrl(url)
                .withSemVersion(semVersion)
                .withTag(tag)
                .build();
    }
}
